/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.eclipse.jetty.io.AbstractByteBufferPool;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

@ManagedObject
public class MappedByteBufferPool
extends AbstractByteBufferPool {
    private static final Logger LOG = Log.getLogger(MappedByteBufferPool.class);
    private final ConcurrentMap<Integer, ByteBufferPool.Bucket> _directBuffers = new ConcurrentHashMap<Integer, ByteBufferPool.Bucket>();
    private final ConcurrentMap<Integer, ByteBufferPool.Bucket> _heapBuffers = new ConcurrentHashMap<Integer, ByteBufferPool.Bucket>();
    private final Function<Integer, ByteBufferPool.Bucket> _newBucket;

    public MappedByteBufferPool() {
        this(-1);
    }

    public MappedByteBufferPool(int factor) {
        this(factor, -1);
    }

    public MappedByteBufferPool(int factor, int maxQueueLength) {
        this(factor, maxQueueLength, null);
    }

    public MappedByteBufferPool(int factor, int maxQueueLength, Function<Integer, ByteBufferPool.Bucket> newBucket) {
        this(factor, maxQueueLength, newBucket, -1L, -1L);
    }

    public MappedByteBufferPool(int factor, int maxQueueLength, Function<Integer, ByteBufferPool.Bucket> newBucket, long maxHeapMemory, long maxDirectMemory) {
        super(factor, maxQueueLength, maxHeapMemory, maxDirectMemory);
        this._newBucket = newBucket != null ? newBucket : this::newBucket;
    }

    private ByteBufferPool.Bucket newBucket(int key) {
        return new ByteBufferPool.Bucket(this, key * this.getCapacityFactor(), this.getMaxQueueLength());
    }

    @Override
    public ByteBuffer acquire(int size, boolean direct) {
        int b = this.bucketFor(size);
        int capacity = b * this.getCapacityFactor();
        ConcurrentMap<Integer, ByteBufferPool.Bucket> buffers = this.bucketsFor(direct);
        ByteBufferPool.Bucket bucket = (ByteBufferPool.Bucket)buffers.get(b);
        if (bucket == null) {
            return this.newByteBuffer(capacity, direct);
        }
        ByteBuffer buffer = bucket.acquire();
        if (buffer == null) {
            return this.newByteBuffer(capacity, direct);
        }
        this.decrementMemory(buffer);
        return buffer;
    }

    @Override
    public void release(ByteBuffer buffer) {
        if (buffer == null) {
            return;
        }
        int capacity = buffer.capacity();
        if (capacity % this.getCapacityFactor() != 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("ByteBuffer {} does not belong to this pool, discarding it", BufferUtil.toDetailString(buffer));
            }
            return;
        }
        int b = this.bucketFor(capacity);
        boolean direct = buffer.isDirect();
        ConcurrentMap<Integer, ByteBufferPool.Bucket> buckets = this.bucketsFor(direct);
        ByteBufferPool.Bucket bucket = buckets.computeIfAbsent(b, this._newBucket);
        bucket.release(buffer);
        this.incrementMemory(buffer);
        this.releaseExcessMemory(direct, this::clearOldestBucket);
    }

    @Override
    public void clear() {
        super.clear();
        this._directBuffers.values().forEach(ByteBufferPool.Bucket::clear);
        this._directBuffers.clear();
        this._heapBuffers.values().forEach(ByteBufferPool.Bucket::clear);
        this._heapBuffers.clear();
    }

    private void clearOldestBucket(boolean direct) {
        ByteBufferPool.Bucket bucket;
        long oldest = Long.MAX_VALUE;
        int index = -1;
        ConcurrentMap<Integer, ByteBufferPool.Bucket> buckets = this.bucketsFor(direct);
        for (Map.Entry entry : buckets.entrySet()) {
            ByteBufferPool.Bucket bucket2 = (ByteBufferPool.Bucket)entry.getValue();
            long lastUpdate = bucket2.getLastUpdate();
            if (lastUpdate >= oldest) continue;
            oldest = lastUpdate;
            index = (Integer)entry.getKey();
        }
        if (index >= 0 && (bucket = (ByteBufferPool.Bucket)buckets.remove(index)) != null) {
            bucket.clear(this::decrementMemory);
        }
    }

    private int bucketFor(int size) {
        int factor = this.getCapacityFactor();
        int bucket = size / factor;
        if (bucket * factor != size) {
            ++bucket;
        }
        return bucket;
    }

    @ManagedAttribute(value="The number of pooled direct ByteBuffers")
    public long getDirectByteBufferCount() {
        return this.getByteBufferCount(true);
    }

    @ManagedAttribute(value="The number of pooled heap ByteBuffers")
    public long getHeapByteBufferCount() {
        return this.getByteBufferCount(false);
    }

    private long getByteBufferCount(boolean direct) {
        return this.bucketsFor(direct).values().stream().mapToLong(ByteBufferPool.Bucket::size).sum();
    }

    ConcurrentMap<Integer, ByteBufferPool.Bucket> bucketsFor(boolean direct) {
        return direct ? this._directBuffers : this._heapBuffers;
    }

    public static class Tagged
    extends MappedByteBufferPool {
        private final AtomicInteger tag = new AtomicInteger();

        @Override
        public ByteBuffer newByteBuffer(int capacity, boolean direct) {
            ByteBuffer buffer = super.newByteBuffer(capacity + 4, direct);
            buffer.limit(buffer.capacity());
            buffer.putInt(this.tag.incrementAndGet());
            ByteBuffer slice = buffer.slice();
            BufferUtil.clear(slice);
            return slice;
        }
    }
}

