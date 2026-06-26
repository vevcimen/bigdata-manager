/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.io.InputStream;
import java.sql.SQLException;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.ByteStreamWriter;

public interface ParameterList {
    public void registerOutParameter(@Positive int var1, int var2) throws SQLException;

    public @NonNegative int getParameterCount();

    public @NonNegative int getInParameterCount();

    public @NonNegative int getOutParameterCount();

    public int[] getTypeOIDs();

    public void setIntParameter(@Positive int var1, int var2) throws SQLException;

    public void setLiteralParameter(@Positive int var1, String var2, int var3) throws SQLException;

    public void setStringParameter(@Positive int var1, String var2, int var3) throws SQLException;

    public void setBytea(@Positive int var1, byte[] var2, @NonNegative int var3, @NonNegative int var4) throws SQLException;

    public void setBytea(@Positive int var1, InputStream var2, @NonNegative int var3) throws SQLException;

    public void setBytea(@Positive int var1, InputStream var2) throws SQLException;

    public void setBytea(@Positive int var1, ByteStreamWriter var2) throws SQLException;

    public void setText(@Positive int var1, InputStream var2) throws SQLException;

    public void setBinaryParameter(@Positive int var1, byte[] var2, int var3) throws SQLException;

    public void setNull(@Positive int var1, int var2) throws SQLException;

    public ParameterList copy();

    public void clear();

    public String toString(@Positive int var1, boolean var2);

    public void appendAll(ParameterList var1) throws SQLException;

    public @Nullable Object @Nullable [] getValues();
}

