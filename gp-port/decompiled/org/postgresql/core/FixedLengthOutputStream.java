/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.io.IOException;
import java.io.OutputStream;

public class FixedLengthOutputStream
extends OutputStream {
    private final int size;
    private final OutputStream target;
    private int written;

    public FixedLengthOutputStream(int size, OutputStream target) {
        this.size = size;
        this.target = target;
    }

    @Override
    public void write(int b) throws IOException {
        this.verifyAllowed(1);
        ++this.written;
        this.target.write(b);
    }

    @Override
    public void write(byte[] buf, int offset, int len) throws IOException {
        if (offset < 0 || len < 0 || offset + len > buf.length) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        this.verifyAllowed(len);
        this.target.write(buf, offset, len);
        this.written += len;
    }

    public int remaining() {
        return this.size - this.written;
    }

    private void verifyAllowed(int wanted) throws IOException {
        if (this.remaining() < wanted) {
            throw new IOException("Attempt to write more than the specified " + this.size + " bytes");
        }
    }
}

