/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.Field;
import org.postgresql.core.Query;
import org.postgresql.core.QueryExecutor;
import org.postgresql.core.ResultCursor;
import org.postgresql.core.ResultHandlerBase;
import org.postgresql.core.Tuple;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class SetupQueryRunner {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static @Nullable Tuple run(QueryExecutor executor, String queryString, boolean wantResults) throws SQLException {
        Query query = executor.createSimpleQuery(queryString);
        SimpleResultHandler handler = new SimpleResultHandler();
        int flags = 1041;
        if (!wantResults) {
            flags |= 6;
        }
        try {
            executor.execute(query, null, handler, 0, 0, flags);
        }
        finally {
            query.close();
        }
        if (!wantResults) {
            return null;
        }
        List<Tuple> tuples = handler.getResults();
        if (tuples == null || tuples.size() != 1) {
            throw new PSQLException(GT.tr("An unexpected result was returned by a query.", new Object[0]), PSQLState.CONNECTION_UNABLE_TO_CONNECT);
        }
        return tuples.get(0);
    }

    private static class SimpleResultHandler
    extends ResultHandlerBase {
        private @Nullable List<Tuple> tuples;

        private SimpleResultHandler() {
        }

        @Nullable List<Tuple> getResults() {
            return this.tuples;
        }

        @Override
        public void handleResultRows(Query fromQuery, Field[] fields, List<Tuple> tuples, @Nullable ResultCursor cursor) {
            this.tuples = tuples;
        }

        @Override
        public void handleWarning(SQLWarning warning) {
        }
    }
}

