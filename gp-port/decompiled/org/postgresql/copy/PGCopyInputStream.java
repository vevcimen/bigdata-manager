/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.copy;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyOut;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class PGCopyInputStream
extends InputStream
implements CopyOut {
    private @Nullable CopyOut op;
    private byte @Nullable [] buf;
    private int at;
    private int len;

    public PGCopyInputStream(PGConnection connection, String sql) throws SQLException {
        this(connection.getCopyAPI().copyOut(sql));
    }

    public PGCopyInputStream(CopyOut op) {
        this.op = op;
    }

    private CopyOut getOp() {
        return Nullness.castNonNull(this.op);
    }

    private byte @Nullable [] fillBuffer() throws IOException {
        if (this.at >= this.len) {
            try {
                this.buf = this.getOp().readFromCopy();
            }
            catch (SQLException sqle) {
                throw new IOException(GT.tr("Copying from database failed: {0}", sqle.getMessage()), sqle);
            }
            if (this.buf == null) {
                this.at = -1;
            } else {
                this.at = 0;
                this.len = this.buf.length;
            }
        }
        return this.buf;
    }

    private void checkClosed() throws IOException {
        if (this.op == null) {
            throw new IOException(GT.tr("This copy stream is closed.", new Object[0]));
        }
    }

    @Override
    public int available() throws IOException {
        this.checkClosed();
        return this.buf != null ? this.len - this.at : 0;
    }

    @Override
    public int read() throws IOException {
        this.checkClosed();
        byte[] buf = this.fillBuffer();
        return buf != null ? buf[this.at++] & 0xFF : -1;
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return this.read(buf, 0, buf.length);
    }

    @Override
    public int read(byte[] buf, int off, int siz) throws IOException {
        int got;
        int length;
        this.checkClosed();
        byte[] data = this.fillBuffer();
        for (got = 0; got < siz && data != null; got += length) {
            length = Math.min(siz - got, this.len - this.at);
            System.arraycopy(data, this.at, buf, off + got, length);
            this.at += length;
            data = this.fillBuffer();
        }
        return got == 0 && data == null ? -1 : got;
    }

    @Override
    public byte @Nullable [] readFromCopy() throws SQLException {
        byte[] result = null;
        try {
            byte[] buf = this.fillBuffer();
            if (buf != null) {
                result = this.at > 0 || this.len < buf.length ? Arrays.copyOfRange(buf, this.at, this.len) : buf;
                this.at = this.len;
            }
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("Read from copy failed.", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)ioe);
        }
        return result;
    }

    @Override
    public byte @Nullable [] readFromCopy(boolean block) throws SQLException {
        return this.readFromCopy();
    }

    @Override
    public void close() throws IOException {
        if (this.op == null) {
            return;
        }
        if (this.op.isActive()) {
            try {
                this.op.cancelCopy();
            }
            catch (SQLException se) {
                throw new IOException("Failed to close copy reader.", se);
            }
        }
        this.op = null;
    }

    @Override
    public void cancelCopy() throws SQLException {
        this.getOp().cancelCopy();
    }

    @Override
    public int getFormat() {
        return this.getOp().getFormat();
    }

    @Override
    public int getFieldFormat(int field) {
        return this.getOp().getFieldFormat(field);
    }

    @Override
    public int getFieldCount() {
        return this.getOp().getFieldCount();
    }

    @Override
    public boolean isActive() {
        return this.op != null && this.op.isActive();
    }

    @Override
    public long getHandledRowCount() {
        return this.getOp().getHandledRowCount();
    }
}

