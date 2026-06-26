/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql;

import java.sql.SQLException;

public interface PGResultSetMetaData {
    public String getBaseColumnName(int var1) throws SQLException;

    public String getBaseTableName(int var1) throws SQLException;

    public String getBaseSchemaName(int var1) throws SQLException;

    public int getFormat(int var1) throws SQLException;
}

