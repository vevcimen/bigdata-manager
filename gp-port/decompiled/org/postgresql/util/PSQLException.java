/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.postgresql.util.PSQLState;
import org.postgresql.util.ServerErrorMessage;

public class PSQLException
extends SQLException {
    private @Nullable ServerErrorMessage serverError;

    @Pure
    public PSQLException(@Nullable String msg, @Nullable PSQLState state, @Nullable Throwable cause) {
        super(msg, state == null ? null : state.getState(), cause);
    }

    @Pure
    public PSQLException(@Nullable String msg, @Nullable PSQLState state) {
        super(msg, state == null ? null : state.getState());
    }

    @Pure
    public PSQLException(ServerErrorMessage serverError) {
        this(serverError, true);
    }

    @Pure
    public PSQLException(ServerErrorMessage serverError, boolean detail) {
        super(detail ? serverError.toString() : serverError.getNonSensitiveErrorMessage(), serverError.getSQLState());
        this.serverError = serverError;
    }

    @Pure
    public @Nullable ServerErrorMessage getServerErrorMessage() {
        return this.serverError;
    }
}

