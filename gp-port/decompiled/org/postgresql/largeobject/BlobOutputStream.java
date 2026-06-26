/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.largeobject;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.largeobject.LargeObject;

public class BlobOutputStream
extends OutputStream {
    private @Nullable LargeObject lo;
    private byte[] buf;
    private int bsize;
    private int bpos;

    public BlobOutputStream(LargeObject lo) {
        this(lo, 1024);
    }

    public BlobOutputStream(LargeObject lo, int bsize) {
        this.lo = lo;
        this.bsize = bsize;
        this.buf = new byte[bsize];
        this.bpos = 0;
    }

    @Override
    public void write(int b) throws IOException {
        LargeObject lo = this.checkClosed();
        try {
            if (this.bpos >= this.bsize) {
                lo.write(this.buf);
                this.bpos = 0;
            }
            this.buf[this.bpos++] = (byte)b;
        }
        catch (SQLException se) {
            throw new IOException(se.toString());
        }
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        LargeObject lo = this.checkClosed();
        try {
            if (this.bpos > 0) {
                this.flush();
            }
            if (off == 0 && len == buf.length) {
                lo.write(buf);
            } else {
                lo.write(buf, off, len);
            }
        }
        catch (SQLException se) {
            throw new IOException(se.toString());
        }
    }

    @Override
    public void flush() throws IOException {
        LargeObject lo = this.checkClosed();
        try {
            if (this.bpos > 0) {
                lo.write(this.buf, 0, this.bpos);
            }
            this.bpos = 0;
        }
        catch (SQLException se) {
            throw new IOException(se.toString());
        }
    }

    @Override
    public void close() throws IOException {
        LargeObject lo = this.lo;
        if (lo != null) {
            try {
                this.flush();
                lo.close();
                this.lo = null;
            }
            catch (SQLException se) {
                throw new IOException(se.toString());
            }
        }
    }

    private LargeObject checkClosed() throws IOException {
        if (this.lo == null) {
            throw new IOException("BlobOutputStream is closed");
        }
        return this.lo;
    }
}

