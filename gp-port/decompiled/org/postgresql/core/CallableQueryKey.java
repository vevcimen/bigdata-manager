/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.BaseQueryKey;

class CallableQueryKey
extends BaseQueryKey {
    CallableQueryKey(String sql) {
        super(sql, true, true);
    }

    @Override
    public String toString() {
        return "CallableQueryKey{sql='" + this.sql + '\'' + ", isParameterized=" + this.isParameterized + ", escapeProcessing=" + this.escapeProcessing + '}';
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return super.equals(o);
    }
}

