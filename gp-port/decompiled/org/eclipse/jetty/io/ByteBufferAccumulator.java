/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.NullByteBufferPool;
import org.eclipse.jetty.util.BufferUtil;

public class ByteBufferAccumulator
implements AutoCloseable {
    private final List<ByteBuffer> _buffers = new ArrayList<ByteBuffer>();
    private final ByteBufferPool _bufferPool;
    private final boolean _direct;

    public ByteBufferAccumulator() {
        this(null, false);
    }

    public ByteBufferAccumulator(ByteBufferPool bufferPool, boolean direct) {
        this._bufferPool = bufferPool == null ? new NullByteBufferPool() : bufferPool;
        this._direct = direct;
    }

    public int getLength() {
        int length = 0;
        for (ByteBuffer buffer : this._buffers) {
            length = Math.addExact(length, buffer.remaining());
        }
        return length;
    }

    public ByteBufferPool getByteBufferPool() {
        return this._bufferPool;
    }

    public ByteBuffer ensureBuffer(int minAllocationSize) {
        return this.ensureBuffer(1, minAllocationSize);
    }

    public ByteBuffer ensureBuffer(int minSize, int minAllocationSize) {
        ByteBuffer buffer;
        ByteBuffer byteBuffer = buffer = this._buffers.isEmpty() ? BufferUtil.EMPTY_BUFFER : this._buffers.get(this._buffers.size() - 1);
        if (BufferUtil.space(buffer) < minSize) {
            buffer = this._bufferPool.acquire(minAllocationSize, this._direct);
            this._buffers.add(buffer);
        }
        return buffer;
    }

    public void copyBytes(byte[] buf, int offset, int length) {
        this.copyBuffer(BufferUtil.toBuffer(buf, offset, length));
    }

    public void copyBuffer(ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            ByteBuffer b = this.ensureBuffer(buffer.remaining());
            int pos = BufferUtil.flipToFill(b);
            BufferUtil.put(buffer, b);
            BufferUtil.flipToFlush(b, pos);
        }
    }

    public ByteBuffer takeByteBuffer() {
        if (this._buffers.size() == 1) {
            ByteBuffer combinedBuffer = this._buffers.get(0);
            this._buffers.clear();
            return combinedBuffer;
        }
        int length = this.getLength();
        ByteBuffer combinedBuffer = this._bufferPool.acquire(length, this._direct);
        BufferUtil.clearToFill(combinedBuffer);
        for (ByteBuffer buffer : this._buffers) {
            combinedBuffer.put(buffer);
            this._bufferPool.release(buffer);
        }
        BufferUtil.flipToFlush(combinedBuffer, 0);
        this._buffers.clear();
        return combinedBuffer;
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer combinedBuffer = this.takeByteBuffer();
        this._buffers.add(combinedBuffer);
        return combinedBuffer;
    }

    public byte[] toByteArray() {
        int length = this.getLength();
        if (length == 0) {
            return new byte[0];
        }
        byte[] bytes = new byte[length];
        ByteBuffer buffer = BufferUtil.toBuffer(bytes);
        BufferUtil.clear(buffer);
        this.writeTo(buffer);
        return bytes;
    }

    public void writeTo(ByteBuffer buffer) {
        int pos = BufferUtil.flipToFill(buffer);
        for (ByteBuffer bb : this._buffers) {
            buffer.put(bb.slice());
        }
        BufferUtil.flipToFlush(buffer, pos);
    }

    public void writeTo(OutputStream out) throws IOException {
        for (ByteBuffer bb : this._buffers) {
            BufferUtil.writeTo(bb.slice(), out);
        }
    }

    @Override
    public void close() {
        this._buffers.forEach(this._bufferPool::release);
        this._buffers.clear();
    }
}

