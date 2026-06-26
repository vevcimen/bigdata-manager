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
import org.postgresql.core.Tuple;

public interface ResultHandler {
    public void handleResultRows(Query var1, Field[] var2, List<Tuple> var3, @Nullable ResultCursor var4);

    public void handleCommandStatus(String var1, long var2, long var4);

    public void handleWarning(SQLWarning var1);

    public void handleError(SQLException var1);

    public void handleCompletion() throws SQLException;

    public void secureProgress();

    public @Nullable SQLException getException();

    public @Nullable SQLWarning getWarning();
}

