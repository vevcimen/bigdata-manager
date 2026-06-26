/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import org.postgresql.core.BaseConnection;
import org.postgresql.jdbc.AbstractBlobClob;
import org.postgresql.largeobject.LargeObject;

public class PgBlob
extends AbstractBlobClob
implements Blob {
    public PgBlob(BaseConnection conn, long oid) throws SQLException {
        super(conn, oid);
    }

    @Override
    public synchronized InputStream getBinaryStream(long pos, long length) throws SQLException {
        this.checkFreed();
        LargeObject subLO = this.getLo(false).copy();
        this.addSubLO(subLO);
        if (pos > Integer.MAX_VALUE) {
            subLO.seek64(pos - 1L, 0);
        } else {
            subLO.seek((int)pos - 1, 0);
        }
        return subLO.getInputStream(length);
    }

    @Override
    public synchronized int setBytes(long pos, byte[] bytes) throws SQLException {
        return this.setBytes(pos, bytes, 0, bytes.length);
    }

    @Override
    public synchronized int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
        this.assertPosition(pos);
        this.getLo(true).seek((int)(pos - 1L));
        this.getLo(true).write(bytes, offset, len);
        return len;
    }
}

