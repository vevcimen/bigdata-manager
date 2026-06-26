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
import org.postgresql.core.ResultCursor;
import org.postgresql.core.ResultHandler;
import org.postgresql.core.Tuple;

public class ResultHandlerDelegate
implements ResultHandler {
    private final @Nullable ResultHandler delegate;

    public ResultHandlerDelegate(@Nullable ResultHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void handleResultRows(Query fromQuery, Field[] fields, List<Tuple> tuples, @Nullable ResultCursor cursor) {
        if (this.delegate != null) {
            this.delegate.handleResultRows(fromQuery, fields, tuples, cursor);
        }
    }

    @Override
    public void handleCommandStatus(String status, long updateCount, long insertOID) {
        if (this.delegate != null) {
            this.delegate.handleCommandStatus(status, updateCount, insertOID);
        }
    }

    @Override
    public void handleWarning(SQLWarning warning) {
        if (this.delegate != null) {
            this.delegate.handleWarning(warning);
        }
    }

    @Override
    public void handleError(SQLException error) {
        if (this.delegate != null) {
            this.delegate.handleError(error);
        }
    }

    @Override
    public void handleCompletion() throws SQLException {
        if (this.delegate != null) {
            this.delegate.handleCompletion();
        }
    }

    @Override
    public void secureProgress() {
        if (this.delegate != null) {
            this.delegate.secureProgress();
        }
    }

    @Override
    public @Nullable SQLException getException() {
        if (this.delegate != null) {
            return this.delegate.getException();
        }
        return null;
    }

    @Override
    public @Nullable SQLWarning getWarning() {
        if (this.delegate != null) {
            return this.delegate.getWarning();
        }
        return null;
    }
}

