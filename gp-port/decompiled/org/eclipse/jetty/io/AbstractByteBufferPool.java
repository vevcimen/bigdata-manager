/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.annotation.ManagedOperation;

@ManagedObject
abstract class AbstractByteBufferPool
implements ByteBufferPool {
    private final int _factor;
    private final int _maxQueueLength;
    private final long _maxHeapMemory;
    private final AtomicLong _heapMemory = new AtomicLong();
    private final long _maxDirectMemory;
    private final AtomicLong _directMemory = new AtomicLong();

    protected AbstractByteBufferPool(int factor, int maxQueueLength, long maxHeapMemory, long maxDirectMemory) {
        this._factor = factor <= 0 ? 1024 : factor;
        this._maxQueueLength = maxQueueLength;
        this._maxHeapMemory = maxHeapMemory;
        this._maxDirectMemory = maxDirectMemory;
    }

    protected int getCapacityFactor() {
        return this._factor;
    }

    protected int getMaxQueueLength() {
        return this._maxQueueLength;
    }

    protected void decrementMemory(ByteBuffer buffer) {
        this.updateMemory(buffer, false);
    }

    protected void incrementMemory(ByteBuffer buffer) {
        this.updateMemory(buffer, true);
    }

    private void updateMemory(ByteBuffer buffer, boolean addOrSub) {
        AtomicLong memory = buffer.isDirect() ? this._directMemory : this._heapMemory;
        int capacity = buffer.capacity();
        memory.addAndGet(addOrSub ? (long)capacity : (long)(-capacity));
    }

    protected void releaseExcessMemory(boolean direct, Consumer<Boolean> clearFn) {
        long maxMemory;
        long l = maxMemory = direct ? this._maxDirectMemory : this._maxHeapMemory;
        if (maxMemory > 0L) {
            while (this.getMemory(direct) > maxMemory) {
                clearFn.accept(direct);
            }
        }
    }

    @ManagedAttribute(value="The bytes retained by direct ByteBuffers")
    public long getDirectMemory() {
        return this.getMemory(true);
    }

    @ManagedAttribute(value="The bytes retained by heap ByteBuffers")
    public long getHeapMemory() {
        return this.getMemory(false);
    }

    public long getMemory(boolean direct) {
        AtomicLong memory = direct ? this._directMemory : this._heapMemory;
        return memory.get();
    }

    @ManagedOperation(value="Clears this ByteBufferPool", impact="ACTION")
    public void clear() {
        this._heapMemory.set(0L);
        this._directMemory.set(0L);
    }
}

