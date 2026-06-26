/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.copy;

import java.sql.SQLException;
import org.postgresql.copy.CopyOperation;
import org.postgresql.util.ByteStreamWriter;

public interface CopyIn
extends CopyOperation {
    public void writeToCopy(byte[] var1, int var2, int var3) throws SQLException;

    public void writeToCopy(ByteStreamWriter var1) throws SQLException;

    public void flushCopy() throws SQLException;

    public long endCopy() throws SQLException;
}

