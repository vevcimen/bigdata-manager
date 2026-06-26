/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.ServerVersion;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public abstract class AbstractBlobClob {
    protected BaseConnection conn;
    private @Nullable LargeObject currentLo;
    private boolean currentLoIsWriteable;
    private boolean support64bit;
    private @Nullable ArrayList<LargeObject> subLOs = new ArrayList();
    private final long oid;

    public AbstractBlobClob(BaseConnection conn, long oid) throws SQLException {
        this.conn = conn;
        this.oid = oid;
        this.currentLoIsWriteable = false;
        this.support64bit = conn.haveMinimumServerVersion(90300);
    }

    public synchronized void free() throws SQLException {
        if (this.currentLo != null) {
            this.currentLo.close();
            this.currentLo = null;
            this.currentLoIsWriteable = false;
        }
        if (this.subLOs != null) {
            for (LargeObject subLO : this.subLOs) {
                subLO.close();
            }
        }
        this.subLOs = null;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public synchronized void truncate(long len) throws SQLException {
        this.checkFreed();
        if (!this.conn.haveMinimumServerVersion(ServerVersion.v8_3)) {
            throw new PSQLException(GT.tr("Truncation of large objects is only implemented in 8.3 and later servers.", new Object[0]), PSQLState.NOT_IMPLEMENTED);
        }
        if (len < 0L) {
            throw new PSQLException(GT.tr("Cannot truncate LOB to a negative length.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (len > Integer.MAX_VALUE) {
            if (!this.support64bit) throw new PSQLException(GT.tr("PostgreSQL LOBs can only index to: {0}", Integer.MAX_VALUE), PSQLState.INVALID_PARAMETER_VALUE);
            this.getLo(true).truncate64(len);
            return;
        } else {
            this.getLo(true).truncate((int)len);
        }
    }

    public synchronized long length() throws SQLException {
        this.checkFreed();
        if (this.support64bit) {
            return this.getLo(false).size64();
        }
        return this.getLo(false).size();
    }

    public synchronized byte[] getBytes(long pos, int length) throws SQLException {
        this.assertPosition(pos);
        this.getLo(false).seek((int)(pos - 1L), 0);
        return this.getLo(false).read(length);
    }

    public synchronized InputStream getBinaryStream() throws SQLException {
        this.checkFreed();
        LargeObject subLO = this.getLo(false).copy();
        this.addSubLO(subLO);
        subLO.seek(0, 0);
        return subLO.getInputStream();
    }

    public synchronized OutputStream setBinaryStream(long pos) throws SQLException {
        this.assertPosition(pos);
        LargeObject subLO = this.getLo(true).copy();
        this.addSubLO(subLO);
        subLO.seek((int)(pos - 1L));
        return subLO.getOutputStream();
    }

    public synchronized long position(byte[] pattern, long start) throws SQLException {
        this.assertPosition(start, pattern.length);
        int position = 1;
        int patternIdx = 0;
        long result = -1L;
        int tmpPosition = 1;
        LOIterator i = new LOIterator(start - 1L);
        while (i.hasNext()) {
            byte b = i.next();
            if (b == pattern[patternIdx]) {
                if (patternIdx == 0) {
                    tmpPosition = position;
                }
                if (++patternIdx == pattern.length) {
                    result = tmpPosition;
                    break;
                }
            } else {
                patternIdx = 0;
            }
            ++position;
        }
        return result;
    }

    public synchronized long position(Blob pattern, long start) throws SQLException {
        return this.position(pattern.getBytes(1L, (int)pattern.length()), start);
    }

    protected void assertPosition(long pos) throws SQLException {
        this.assertPosition(pos, 0L);
    }

    protected void assertPosition(long pos, long len) throws SQLException {
        this.checkFreed();
        if (pos < 1L) {
            throw new PSQLException(GT.tr("LOB positioning offsets start at 1.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (pos + len - 1L > Integer.MAX_VALUE) {
            throw new PSQLException(GT.tr("PostgreSQL LOBs can only index to: {0}", Integer.MAX_VALUE), PSQLState.INVALID_PARAMETER_VALUE);
        }
    }

    protected void checkFreed() throws SQLException {
        if (this.subLOs == null) {
            throw new PSQLException(GT.tr("free() was called on this LOB previously", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
    }

    protected synchronized LargeObject getLo(boolean forWrite) throws SQLException {
        LargeObject currentLo = this.currentLo;
        if (currentLo != null) {
            if (forWrite && !this.currentLoIsWriteable) {
                int currentPos = currentLo.tell();
                LargeObjectManager lom = this.conn.getLargeObjectAPI();
                LargeObject newLo = lom.open(this.oid, 393216);
                Nullness.castNonNull(this.subLOs).add(currentLo);
                this.currentLo = currentLo = newLo;
                if (currentPos != 0) {
                    currentLo.seek(currentPos);
                }
            }
            return currentLo;
        }
        LargeObjectManager lom = this.conn.getLargeObjectAPI();
        this.currentLo = currentLo = lom.open(this.oid, forWrite ? 393216 : 262144);
        this.currentLoIsWriteable = forWrite;
        return currentLo;
    }

    protected void addSubLO(LargeObject subLO) {
        Nullness.castNonNull(this.subLOs).add(subLO);
    }

    private class LOIterator {
        private static final int BUFFER_SIZE = 8096;
        private final byte[] buffer = new byte[8096];
        private int idx = 8096;
        private int numBytes = 8096;

        LOIterator(long start) throws SQLException {
            AbstractBlobClob.this.getLo(false).seek((int)start);
        }

        public boolean hasNext() throws SQLException {
            boolean result;
            if (this.idx < this.numBytes) {
                result = true;
            } else {
                this.numBytes = AbstractBlobClob.this.getLo(false).read(this.buffer, 0, 8096);
                this.idx = 0;
                result = this.numBytes > 0;
            }
            return result;
        }

        private byte next() {
            return this.buffer[this.idx++];
        }
    }
}

