/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Clob;
import java.sql.SQLException;
import org.postgresql.Driver;
import org.postgresql.core.BaseConnection;
import org.postgresql.jdbc.AbstractBlobClob;
import org.postgresql.largeobject.LargeObject;

public class PgClob
extends AbstractBlobClob
implements Clob {
    public PgClob(BaseConnection conn, long oid) throws SQLException {
        super(conn, oid);
    }

    @Override
    public synchronized Reader getCharacterStream(long pos, long length) throws SQLException {
        this.checkFreed();
        throw Driver.notImplemented(this.getClass(), "getCharacterStream(long, long)");
    }

    @Override
    public synchronized int setString(long pos, String str) throws SQLException {
        this.checkFreed();
        throw Driver.notImplemented(this.getClass(), "setString(long,str)");
    }

    @Override
    public synchronized int setString(long pos, String str, int offset, int len) throws SQLException {
        this.checkFreed();
        throw Driver.notImplemented(this.getClass(), "setString(long,String,int,int)");
    }

    @Override
    public synchronized OutputStream setAsciiStream(long pos) throws SQLException {
        this.checkFreed();
        throw Driver.notImplemented(this.getClass(), "setAsciiStream(long)");
    }

    @Override
    public synchronized Writer setCharacterStream(long pos) throws SQLException {
        this.checkFreed();
        throw Driver.notImplemented(this.getClass(), "setCharacteStream(long)");
    }

    @Override
    public synchronized InputStream getAsciiStream() throws SQLException {
        return this.getBinaryStream();
    }

    @Override
    public synchronized Reader getCharacterStream() throws SQLException {
        Charset connectionCharset = Charset.forName(this.conn.getEncoding().name());
        return new InputStreamReader(this.getBinaryStream(), connectionCharset);
    }

    @Override
    public synchronized String getSubString(long i, int j) throws SQLException {
        this.assertPosition(i, j);
        LargeObject lo = this.getLo(false);
        lo.seek((int)i - 1);
        return new String(lo.read(j));
    }

    @Override
    public synchronized long position(String pattern, long start) throws SQLException {
        this.checkFreed();
        throw Driver.notImplemented(this.getClass(), "position(String,long)");
    }

    @Override
    public synchronized long position(Clob pattern, long start) throws SQLException {
        this.checkFreed();
        throw Driver.notImplemented(this.getClass(), "position(Clob,start)");
    }
}

