/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.ParameterList;
import org.postgresql.core.SqlCommand;

public interface Query {
    public ParameterList createParameterList();

    public String toString(@Nullable ParameterList var1);

    public String getNativeSql();

    public @Nullable SqlCommand getSqlCommand();

    public void close();

    public boolean isStatementDescribed();

    public boolean isEmpty();

    public int getBatchSize();

    public @Nullable Map<String, Integer> getResultSetColumnNameIndexMap();

    public Query @Nullable [] org_postgresql_core_Query_arr_getSubqueries();
}

