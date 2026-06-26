/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

public class JdbcCallParseInfo {
    private final String sql;
    private final boolean isFunction;

    public JdbcCallParseInfo(String sql, boolean isFunction) {
        this.sql = sql;
        this.isFunction = isFunction;
    }

    public String getSql() {
        return this.sql;
    }

    public boolean isFunction() {
        return this.isFunction;
    }
}

