/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.util.TimeZone;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.Provider;
import org.postgresql.core.QueryExecutor;

class QueryExecutorTimeZoneProvider
implements Provider<TimeZone> {
    private final QueryExecutor queryExecutor;

    QueryExecutorTimeZoneProvider(QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    @Override
    public @Nullable TimeZone get() {
        return this.queryExecutor.getTimeZone();
    }
}

