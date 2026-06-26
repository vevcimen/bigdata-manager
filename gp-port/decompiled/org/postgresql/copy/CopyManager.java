/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.copy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import org.postgresql.copy.CopyDual;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyOperation;
import org.postgresql.copy.CopyOut;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Encoding;
import org.postgresql.core.QueryExecutor;
import org.postgresql.util.ByteStreamWriter;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class CopyManager {
    static final int DEFAULT_BUFFER_SIZE = 65536;
    private final Encoding encoding;
    private final QueryExecutor queryExecutor;
    private final BaseConnection connection;

    public CopyManager(BaseConnection connection) throws SQLException {
        this.encoding = connection.getEncoding();
        this.queryExecutor = connection.getQueryExecutor();
        this.connection = connection;
    }

    public CopyIn copyIn(String sql) throws SQLException {
        CopyOperation op = this.queryExecutor.startCopy(sql, this.connection.getAutoCommit());
        if (op == null || op instanceof CopyIn) {
            return (CopyIn)op;
        }
        op.cancelCopy();
        throw new PSQLException(GT.tr("Requested CopyIn but got {0}", op.getClass().getName()), PSQLState.WRONG_OBJECT_TYPE);
    }

    public CopyOut copyOut(String sql) throws SQLException {
        CopyOperation op = this.queryExecutor.startCopy(sql, this.connection.getAutoCommit());
        if (op == null || op instanceof CopyOut) {
            return (CopyOut)op;
        }
        op.cancelCopy();
        throw new PSQLException(GT.tr("Requested CopyOut but got {0}", op.getClass().getName()), PSQLState.WRONG_OBJECT_TYPE);
    }

    public CopyDual copyDual(String sql) throws SQLException {
        CopyOperation op = this.queryExecutor.startCopy(sql, this.connection.getAutoCommit());
        if (op == null || op instanceof CopyDual) {
            return (CopyDual)op;
        }
        op.cancelCopy();
        throw new PSQLException(GT.tr("Requested CopyDual but got {0}", op.getClass().getName()), PSQLState.WRONG_OBJECT_TYPE);
    }

    public long copyOut(String sql, Writer to) throws SQLException, IOException {
        CopyOut cp = this.copyOut(sql);
        try {
            byte[] buf;
            while ((buf = cp.readFromCopy()) != null) {
                to.write(this.encoding.decode(buf));
            }
            long l = cp.getHandledRowCount();
            return l;
        }
        catch (IOException ioEX) {
            if (cp.isActive()) {
                cp.cancelCopy();
            }
            try {
                byte[] buf;
                while ((buf = cp.readFromCopy()) != null) {
                }
            }
            catch (SQLException sQLException) {
                // empty catch block
            }
            throw ioEX;
        }
        finally {
            if (cp.isActive()) {
                cp.cancelCopy();
            }
        }
    }

    public long copyOut(String sql, OutputStream to) throws SQLException, IOException {
        CopyOut cp = this.copyOut(sql);
        try {
            byte[] buf;
            while ((buf = cp.readFromCopy()) != null) {
                to.write(buf);
            }
            long l = cp.getHandledRowCount();
            return l;
        }
        catch (IOException ioEX) {
            if (cp.isActive()) {
                cp.cancelCopy();
            }
            try {
                byte[] buf;
                while ((buf = cp.readFromCopy()) != null) {
                }
            }
            catch (SQLException sQLException) {
                // empty catch block
            }
            throw ioEX;
        }
        finally {
            if (cp.isActive()) {
                cp.cancelCopy();
            }
        }
    }

    public long copyIn(String sql, Reader from) throws SQLException, IOException {
        return this.copyIn(sql, from, 65536);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long copyIn(String sql, Reader from, int bufferSize) throws SQLException, IOException {
        char[] cbuf = new char[bufferSize];
        CopyIn cp = this.copyIn(sql);
        try {
            int len;
            while ((len = from.read(cbuf)) >= 0) {
                if (len <= 0) continue;
                byte[] buf = this.encoding.encode(new String(cbuf, 0, len));
                cp.writeToCopy(buf, 0, buf.length);
            }
            long l = cp.endCopy();
            return l;
        }
        finally {
            if (cp.isActive()) {
                cp.cancelCopy();
            }
        }
    }

    public long copyIn(String sql, InputStream from) throws SQLException, IOException {
        return this.copyIn(sql, from, 65536);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long copyIn(String sql, InputStream from, int bufferSize) throws SQLException, IOException {
        byte[] buf = new byte[bufferSize];
        CopyIn cp = this.copyIn(sql);
        try {
            int len;
            while ((len = from.read(buf)) >= 0) {
                if (len <= 0) continue;
                cp.writeToCopy(buf, 0, len);
            }
            long l = cp.endCopy();
            return l;
        }
        finally {
            if (cp.isActive()) {
                cp.cancelCopy();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long copyIn(String sql, ByteStreamWriter from) throws SQLException, IOException {
        CopyIn cp = this.copyIn(sql);
        try {
            cp.writeToCopy(from);
            long l = cp.endCopy();
            return l;
        }
        finally {
            if (cp.isActive()) {
                cp.cancelCopy();
            }
        }
    }
}

