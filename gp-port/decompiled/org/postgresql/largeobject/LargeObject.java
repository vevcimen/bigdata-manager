/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.largeobject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.BaseConnection;
import org.postgresql.fastpath.Fastpath;
import org.postgresql.fastpath.FastpathArg;
import org.postgresql.largeobject.BlobInputStream;
import org.postgresql.largeobject.BlobOutputStream;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class LargeObject
implements AutoCloseable {
    public static final int SEEK_SET = 0;
    public static final int SEEK_CUR = 1;
    public static final int SEEK_END = 2;
    private final Fastpath fp;
    private final long oid;
    private final int mode;
    private final int fd;
    private @Nullable BlobOutputStream os;
    private boolean closed = false;
    private @Nullable BaseConnection conn;
    private final boolean commitOnClose;

    protected LargeObject(Fastpath fp, long oid, int mode, @Nullable BaseConnection conn, boolean commitOnClose) throws SQLException {
        this.fp = fp;
        this.oid = oid;
        this.mode = mode;
        if (commitOnClose) {
            this.commitOnClose = true;
            this.conn = conn;
        } else {
            this.commitOnClose = false;
        }
        FastpathArg[] args = new FastpathArg[]{Fastpath.createOIDArg(oid), new FastpathArg(mode)};
        this.fd = fp.getInteger("lo_open", args);
    }

    protected LargeObject(Fastpath fp, long oid, int mode) throws SQLException {
        this(fp, oid, mode, null, false);
    }

    public LargeObject copy() throws SQLException {
        return new LargeObject(this.fp, this.oid, this.mode);
    }

    @Deprecated
    public int getOID() {
        return (int)this.oid;
    }

    public long getLongOID() {
        return this.oid;
    }

    @Override
    public void close() throws SQLException {
        if (!this.closed) {
            if (this.os != null) {
                try {
                    this.os.flush();
                }
                catch (IOException ioe) {
                    throw new PSQLException("Exception flushing output stream", PSQLState.DATA_ERROR, (Throwable)ioe);
                }
                finally {
                    this.os = null;
                }
            }
            FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.fd)};
            this.fp.fastpath("lo_close", args);
            this.closed = true;
            BaseConnection conn = this.conn;
            if (this.commitOnClose && conn != null) {
                conn.commit();
            }
        }
    }

    public byte[] read(int len) throws SQLException {
        FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.fd), new FastpathArg(len)};
        return Nullness.castNonNull(this.fp.getData("loread", args));
    }

    public int read(byte[] buf, int off, int len) throws SQLException {
        byte[] b = this.read(len);
        if (b == null) {
            return 0;
        }
        if (b.length < len) {
            len = b.length;
        }
        System.arraycopy(b, 0, buf, off, len);
        return len;
    }

    public void write(byte[] buf) throws SQLException {
        FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.fd), new FastpathArg(buf)};
        this.fp.fastpath("lowrite", args);
    }

    public void write(byte[] buf, int off, int len) throws SQLException {
        FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.fd), new FastpathArg(buf, off, len)};
        this.fp.fastpath("lowrite", args);
    }

    public void seek(int pos, int ref) throws SQLException {
        FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.fd), new FastpathArg(pos), new FastpathArg(ref)};
        this.fp.fastpath("lo_lseek", args);
    }

    public void seek64(long pos, int ref) throws SQLException {
        FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.fd), new FastpathArg(pos), new FastpathArg(ref)};
        this.fp.fastpath("lo_lseek64", args);
    }

    public void seek(int pos) throws SQLException {
        this.seek(pos, 0);
    }

    public int tell() throws SQLException {
        FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.fd)};
        return this.fp.getInteger("lo_tell", args);
    }

    public long tell64() throws SQLException {
        FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.fd)};
        return this.fp.getLong("lo_tell64", args);
    }

    public int size() throws SQLException {
        int cp = this.tell();
        this.seek(0, 2);
        int sz = this.tell();
        this.seek(cp, 0);
        return sz;
    }

    public long size64() throws SQLException {
        long cp = this.tell64();
        this.seek64(0L, 2);
        long sz = this.tell64();
        this.seek64(cp, 0);
        return sz;
    }

    public void truncate(int len) throws SQLException {
        FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.fd), new FastpathArg(len)};
        this.fp.getInteger("lo_truncate", args);
    }

    public void truncate64(long len) throws SQLException {
        FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.fd), new FastpathArg(len)};
        this.fp.getInteger("lo_truncate64", args);
    }

    public InputStream getInputStream() throws SQLException {
        return new BlobInputStream(this, 4096);
    }

    public InputStream getInputStream(long limit) throws SQLException {
        return new BlobInputStream(this, 4096, limit);
    }

    public OutputStream getOutputStream() throws SQLException {
        if (this.os == null) {
            this.os = new BlobOutputStream(this, 4096);
        }
        return this.os;
    }
}

