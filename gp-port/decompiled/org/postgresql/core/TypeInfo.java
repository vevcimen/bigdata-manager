/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.sql.SQLException;
import java.util.Iterator;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.PGobject;

public interface TypeInfo {
    public void addCoreType(String var1, Integer var2, Integer var3, String var4, Integer var5);

    public void addDataType(String var1, Class<? extends PGobject> var2) throws SQLException;

    public int getSQLType(int var1) throws SQLException;

    public int getSQLType(String var1) throws SQLException;

    public int getJavaArrayType(String var1) throws SQLException;

    public int getPGType(String var1) throws SQLException;

    public @Nullable String getPGType(int var1) throws SQLException;

    public int getPGArrayElement(int var1) throws SQLException;

    public int getPGArrayType(String var1) throws SQLException;

    public char getArrayDelimiter(int var1) throws SQLException;

    public Iterator<String> getPGTypeNamesWithSQLTypes();

    public Iterator<Integer> getPGTypeOidsWithSQLTypes();

    public @Nullable Class<? extends PGobject> getPGobject(String var1);

    public String getJavaClass(int var1) throws SQLException;

    public @Nullable String getTypeForAlias(String var1);

    public int getPrecision(int var1, int var2);

    public int getScale(int var1, int var2);

    public boolean isCaseSensitive(int var1);

    public boolean isSigned(int var1);

    public int getDisplaySize(int var1, int var2);

    public int getMaximumPrecision(int var1);

    public boolean requiresQuoting(int var1) throws SQLException;

    public boolean requiresQuotingSqlType(int var1) throws SQLException;

    public int longOidToInt(long var1) throws SQLException;

    public long intOidToLong(int var1);
}

