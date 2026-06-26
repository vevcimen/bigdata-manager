/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;
import org.eclipse.jetty.io.AbstractByteBufferPool;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.MappedByteBufferPool;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

@ManagedObject
public class ArrayByteBufferPool
extends AbstractByteBufferPool {
    private static final Logger LOG = Log.getLogger(MappedByteBufferPool.class);
    private final int _minCapacity;
    private final ByteBufferPool.Bucket[] _direct;
    private final ByteBufferPool.Bucket[] _indirect;

    public ArrayByteBufferPool() {
        this(-1, -1, -1);
    }

    public ArrayByteBufferPool(int minCapacity, int factor, int maxCapacity) {
        this(minCapacity, factor, maxCapacity, -1, -1L, -1L);
    }

    public ArrayByteBufferPool(int minCapacity, int factor, int maxCapacity, int maxQueueLength) {
        this(minCapacity, factor, maxCapacity, maxQueueLength, -1L, -1L);
    }

    public ArrayByteBufferPool(int minCapacity, int factor, int maxCapacity, int maxQueueLength, long maxHeapMemory, long maxDirectMemory) {
        super(factor, maxQueueLength, maxHeapMemory, maxDirectMemory);
        factor = this.getCapacityFactor();
        if (minCapacity <= 0) {
            minCapacity = 0;
        }
        if (maxCapacity <= 0) {
            maxCapacity = 65536;
        }
        if (maxCapacity % factor != 0 || factor >= maxCapacity) {
            throw new IllegalArgumentException("The capacity factor must be a divisor of maxCapacity");
        }
        this._minCapacity = minCapacity;
        int length = maxCapacity / factor;
        this._direct = new ByteBufferPool.Bucket[length];
        this._indirect = new ByteBufferPool.Bucket[length];
    }

    @Override
    public ByteBuffer acquire(int size, boolean direct) {
        int capacity = size < this._minCapacity ? size : (this.bucketFor(size) + 1) * this.getCapacityFactor();
        ByteBufferPool.Bucket bucket = this.bucketFor(size, direct, null);
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
        boolean direct = buffer.isDirect();
        ByteBufferPool.Bucket bucket = this.bucketFor(capacity, direct, this::newBucket);
        if (bucket != null) {
            bucket.release(buffer);
            this.incrementMemory(buffer);
            this.releaseExcessMemory(direct, this::clearOldestBucket);
        }
    }

    private ByteBufferPool.Bucket newBucket(int key) {
        return new ByteBufferPool.Bucket(this, key * this.getCapacityFactor(), this.getMaxQueueLength());
    }

    @Override
    public void clear() {
        super.clear();
        for (int i = 0; i < this._direct.length; ++i) {
            ByteBufferPool.Bucket bucket = this._direct[i];
            if (bucket != null) {
                bucket.clear();
            }
            this._direct[i] = null;
            bucket = this._indirect[i];
            if (bucket != null) {
                bucket.clear();
            }
            this._indirect[i] = null;
        }
    }

    private void clearOldestBucket(boolean direct) {
        long oldest = Long.MAX_VALUE;
        int index = -1;
        ByteBufferPool.Bucket[] buckets = this.bucketsFor(direct);
        for (int i = 0; i < buckets.length; ++i) {
            long lastUpdate;
            ByteBufferPool.Bucket bucket = buckets[i];
            if (bucket == null || (lastUpdate = bucket.getLastUpdate()) >= oldest) continue;
            oldest = lastUpdate;
            index = i;
        }
        if (index >= 0) {
            ByteBufferPool.Bucket bucket = buckets[index];
            buckets[index] = null;
            if (bucket != null) {
                bucket.clear(this::decrementMemory);
            }
        }
    }

    private int bucketFor(int capacity) {
        return (capacity - 1) / this.getCapacityFactor();
    }

    private ByteBufferPool.Bucket bucketFor(int capacity, boolean direct, IntFunction<ByteBufferPool.Bucket> newBucket) {
        if (capacity < this._minCapacity) {
            return null;
        }
        int b = this.bucketFor(capacity);
        if (b >= this._direct.length) {
            return null;
        }
        ByteBufferPool.Bucket[] buckets = this.bucketsFor(direct);
        ByteBufferPool.Bucket bucket = buckets[b];
        if (bucket == null && newBucket != null) {
            buckets[b] = bucket = newBucket.apply(b + 1);
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
        return Arrays.stream(this.bucketsFor(direct)).filter(Objects::nonNull).mapToLong(ByteBufferPool.Bucket::size).sum();
    }

    ByteBufferPool.Bucket[] bucketsFor(boolean direct) {
        return direct ? this._direct : this._indirect;
    }
}

