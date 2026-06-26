/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3;

import java.sql.SQLException;
import org.postgresql.copy.CopyIn;
import org.postgresql.core.v3.CopyOperationImpl;
import org.postgresql.util.ByteStreamWriter;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class CopyInImpl
extends CopyOperationImpl
implements CopyIn {
    @Override
    public void writeToCopy(byte[] data, int off, int siz) throws SQLException {
        this.getQueryExecutor().writeToCopy(this, data, off, siz);
    }

    @Override
    public void writeToCopy(ByteStreamWriter from) throws SQLException {
        this.getQueryExecutor().writeToCopy(this, from);
    }

    @Override
    public void flushCopy() throws SQLException {
        this.getQueryExecutor().flushCopy(this);
    }

    @Override
    public long endCopy() throws SQLException {
        return this.getQueryExecutor().endCopy(this);
    }

    @Override
    protected void handleCopydata(byte[] data) throws PSQLException {
        throw new PSQLException(GT.tr("CopyIn copy direction can't receive data", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
    }
}

