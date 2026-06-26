/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.Field;
import org.postgresql.core.ParameterList;
import org.postgresql.core.Query;
import org.postgresql.core.ResultCursor;
import org.postgresql.core.Tuple;
import org.postgresql.jdbc.BatchResultHandler;
import org.postgresql.jdbc.PgStatement;

class CallableBatchResultHandler
extends BatchResultHandler {
    CallableBatchResultHandler(PgStatement statement, Query[] queries, @Nullable ParameterList[] parameterLists) {
        super(statement, queries, parameterLists, false);
    }

    @Override
    public void handleResultRows(Query fromQuery, Field[] fields, List<Tuple> tuples, @Nullable ResultCursor cursor) {
    }
}

