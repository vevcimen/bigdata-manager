/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.sql.SQLWarning;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.ServerErrorMessage;

public class PSQLWarning
extends SQLWarning {
    private final ServerErrorMessage serverError;

    public PSQLWarning(ServerErrorMessage err) {
        super(err.toString(), err.getSQLState());
        this.serverError = err;
    }

    @Override
    public @Nullable String getMessage() {
        return this.serverError.getMessage();
    }

    public ServerErrorMessage getServerErrorMessage() {
        return this.serverError;
    }
}

