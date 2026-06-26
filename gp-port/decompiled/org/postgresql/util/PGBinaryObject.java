/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.sql.SQLException;

public interface PGBinaryObject {
    public void setByteValue(byte[] var1, int var2) throws SQLException;

    public int lengthInBytes();

    public void toBytes(byte[] var1, int var2);
}

