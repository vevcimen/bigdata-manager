/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.eclipse.jetty.io.ByteBufferAccumulator;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.NullByteBufferPool;

public class ByteBufferOutputStream2
extends OutputStream {
    private final ByteBufferAccumulator _accumulator;
    private int _size = 0;

    public ByteBufferOutputStream2() {
        this(null, false);
    }

    public ByteBufferOutputStream2(ByteBufferPool bufferPool, boolean direct) {
        this._accumulator = new ByteBufferAccumulator(bufferPool == null ? new NullByteBufferPool() : bufferPool, direct);
    }

    public ByteBufferPool getByteBufferPool() {
        return this._accumulator.getByteBufferPool();
    }

    public ByteBuffer takeByteBuffer() {
        return this._accumulator.takeByteBuffer();
    }

    public ByteBuffer toByteBuffer() {
        return this._accumulator.toByteBuffer();
    }

    public byte[] toByteArray() {
        return this._accumulator.toByteArray();
    }

    public int size() {
        return this._size;
    }

    @Override
    public void write(int b) {
        this.write(new byte[]{(byte)b}, 0, 1);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        this._size += len;
        this._accumulator.copyBytes(b, off, len);
    }

    public void write(ByteBuffer buffer) {
        this._size += buffer.remaining();
        this._accumulator.copyBuffer(buffer);
    }

    public void writeTo(ByteBuffer buffer) {
        this._accumulator.writeTo(buffer);
    }

    public void writeTo(OutputStream out) throws IOException {
        this._accumulator.writeTo(out);
    }

    @Override
    public void close() {
        this._accumulator.close();
        this._size = 0;
    }

    public synchronized String toString() {
        return String.format("%s@%x{size=%d, byteAccumulator=%s}", this.getClass().getSimpleName(), this.hashCode(), this._size, this._accumulator);
    }
}

