/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.eclipse.jetty.util.BufferUtil;

public class ByteBufferOutputStream
extends OutputStream {
    final ByteBuffer _buffer;

    public ByteBufferOutputStream(ByteBuffer buffer) {
        this._buffer = buffer;
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }

    @Override
    public void write(byte[] b) {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        BufferUtil.append(this._buffer, b, off, len);
    }

    @Override
    public void write(int b) {
        BufferUtil.append(this._buffer, (byte)b);
    }
}

