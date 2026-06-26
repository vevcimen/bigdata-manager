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
import org.postgresql.util.internal.Nullness;

public class ResultHandlerBase
implements ResultHandler {
    private @Nullable SQLException firstException;
    private @Nullable SQLException lastException;
    private @Nullable SQLWarning firstWarning;
    private @Nullable SQLWarning lastWarning;

    @Override
    public void handleResultRows(Query fromQuery, Field[] fields, List<Tuple> tuples, @Nullable ResultCursor cursor) {
    }

    @Override
    public void handleCommandStatus(String status, long updateCount, long insertOID) {
    }

    @Override
    public void secureProgress() {
    }

    @Override
    public void handleWarning(SQLWarning warning) {
        if (this.firstWarning == null) {
            this.firstWarning = this.lastWarning = warning;
            return;
        }
        SQLWarning lastWarning = Nullness.castNonNull(this.lastWarning);
        lastWarning.setNextException(warning);
        this.lastWarning = warning;
    }

    @Override
    public void handleError(SQLException error) {
        if (this.firstException == null) {
            this.firstException = this.lastException = error;
            return;
        }
        Nullness.castNonNull(this.lastException).setNextException(error);
        this.lastException = error;
    }

    @Override
    public void handleCompletion() throws SQLException {
        SQLException firstException = this.firstException;
        if (firstException != null) {
            throw firstException;
        }
    }

    @Override
    public @Nullable SQLException getException() {
        return this.firstException;
    }

    @Override
    public @Nullable SQLWarning getWarning() {
        return this.firstWarning;
    }
}

