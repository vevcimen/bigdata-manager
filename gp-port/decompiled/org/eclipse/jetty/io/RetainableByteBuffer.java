/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Retainable;

public class RetainableByteBuffer
implements Retainable {
    private final ByteBufferPool pool;
    private final ByteBuffer buffer;
    private final AtomicInteger references;

    public RetainableByteBuffer(ByteBufferPool pool, int size) {
        this(pool, size, false);
    }

    public RetainableByteBuffer(ByteBufferPool pool, int size, boolean direct) {
        this.pool = pool;
        this.buffer = pool.acquire(size, direct);
        this.references = new AtomicInteger(1);
    }

    public ByteBuffer getBuffer() {
        return this.buffer;
    }

    public int getReferences() {
        return this.references.get();
    }

    @Override
    public void retain() {
        int r;
        do {
            if ((r = this.references.get()) != 0) continue;
            throw new IllegalStateException("released " + this);
        } while (!this.references.compareAndSet(r, r + 1));
    }

    public int release() {
        int ref = this.references.decrementAndGet();
        if (ref == 0) {
            this.pool.release(this.buffer);
        } else if (ref < 0) {
            throw new IllegalStateException("already released " + this);
        }
        return ref;
    }

    public int remaining() {
        return this.buffer.remaining();
    }

    public boolean hasRemaining() {
        return this.remaining() > 0;
    }

    public boolean isEmpty() {
        return !this.hasRemaining();
    }

    public void clear() {
        BufferUtil.clear(this.buffer);
    }

    public String toString() {
        return String.format("%s@%x{%s,r=%d}", this.getClass().getSimpleName(), this.hashCode(), BufferUtil.toDetailString(this.buffer), this.getReferences());
    }
}

