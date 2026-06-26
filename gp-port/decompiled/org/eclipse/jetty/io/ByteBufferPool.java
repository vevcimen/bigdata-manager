/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.eclipse.jetty.util.BufferUtil;

public interface ByteBufferPool {
    public ByteBuffer acquire(int var1, boolean var2);

    public void release(ByteBuffer var1);

    default public void remove(ByteBuffer buffer) {
    }

    default public ByteBuffer newByteBuffer(int capacity, boolean direct) {
        return direct ? BufferUtil.allocateDirect(capacity) : BufferUtil.allocate(capacity);
    }

    public static class Bucket {
        private final Deque<ByteBuffer> _queue = new ConcurrentLinkedDeque<ByteBuffer>();
        private final ByteBufferPool _pool;
        private final int _capacity;
        private final int _maxSize;
        private final AtomicInteger _size;
        private final AtomicLong _lastUpdate = new AtomicLong(System.nanoTime());

        public Bucket(ByteBufferPool pool, int capacity, int maxSize) {
            this._pool = pool;
            this._capacity = capacity;
            this._maxSize = maxSize;
            this._size = maxSize > 0 ? new AtomicInteger() : null;
        }

        public ByteBuffer acquire() {
            ByteBuffer buffer = this.queuePoll();
            if (buffer == null) {
                return null;
            }
            if (this._size != null) {
                this._size.decrementAndGet();
            }
            return buffer;
        }

        @Deprecated
        public ByteBuffer acquire(boolean direct) {
            ByteBuffer buffer = this.queuePoll();
            if (buffer == null) {
                return this._pool.newByteBuffer(this._capacity, direct);
            }
            if (this._size != null) {
                this._size.decrementAndGet();
            }
            return buffer;
        }

        public void release(ByteBuffer buffer) {
            this._lastUpdate.lazySet(System.nanoTime());
            BufferUtil.clear(buffer);
            if (this._size == null) {
                this.queueOffer(buffer);
            } else if (this._size.incrementAndGet() <= this._maxSize) {
                this.queueOffer(buffer);
            } else {
                this._size.decrementAndGet();
            }
        }

        public void clear() {
            this.clear(null);
        }

        void clear(Consumer<ByteBuffer> memoryFn) {
            ByteBuffer buffer;
            int size;
            int n = size = this._size == null ? 0 : this._size.get() - 1;
            while (size >= 0 && (buffer = this.queuePoll()) != null) {
                if (memoryFn != null) {
                    memoryFn.accept(buffer);
                }
                if (this._size == null) continue;
                this._size.decrementAndGet();
                --size;
            }
        }

        private void queueOffer(ByteBuffer buffer) {
            this._queue.offerFirst(buffer);
        }

        private ByteBuffer queuePoll() {
            return this._queue.poll();
        }

        boolean isEmpty() {
            return this._queue.isEmpty();
        }

        int size() {
            return this._queue.size();
        }

        long getLastUpdate() {
            return this._lastUpdate.get();
        }

        public String toString() {
            return String.format("%s@%x{%d/%d@%d}", this.getClass().getSimpleName(), this.hashCode(), this.size(), this._maxSize, this._capacity);
        }
    }

    public static class Lease {
        private final ByteBufferPool byteBufferPool;
        private final List<ByteBuffer> buffers;
        private final List<Boolean> recycles;

        public Lease(ByteBufferPool byteBufferPool) {
            this.byteBufferPool = byteBufferPool;
            this.buffers = new ArrayList<ByteBuffer>();
            this.recycles = new ArrayList<Boolean>();
        }

        public ByteBuffer acquire(int capacity, boolean direct) {
            ByteBuffer buffer = this.byteBufferPool.acquire(capacity, direct);
            BufferUtil.clearToFill(buffer);
            return buffer;
        }

        public void append(ByteBuffer buffer, boolean recycle) {
            this.buffers.add(buffer);
            this.recycles.add(recycle);
        }

        public void insert(int index, ByteBuffer buffer, boolean recycle) {
            this.buffers.add(index, buffer);
            this.recycles.add(index, recycle);
        }

        public List<ByteBuffer> getByteBuffers() {
            return this.buffers;
        }

        public long getTotalLength() {
            long length = 0L;
            for (ByteBuffer buffer : this.buffers) {
                length += (long)buffer.remaining();
            }
            return length;
        }

        public int getSize() {
            return this.buffers.size();
        }

        public void recycle() {
            for (int i = 0; i < this.buffers.size(); ++i) {
                ByteBuffer buffer = this.buffers.get(i);
                if (!this.recycles.get(i).booleanValue()) continue;
                this.release(buffer);
            }
            this.buffers.clear();
            this.recycles.clear();
        }

        public void release(ByteBuffer buffer) {
            this.byteBufferPool.release(buffer);
        }
    }
}

