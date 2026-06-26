/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent;

import java.sql.SQLFeatureNotSupportedException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.ServerVersion;
import org.postgresql.replication.fluent.ChainedCommonCreateSlotBuilder;
import org.postgresql.util.GT;

public abstract class AbstractCreateSlotBuilder<T extends ChainedCommonCreateSlotBuilder<T>>
implements ChainedCommonCreateSlotBuilder<T> {
    protected @Nullable String slotName;
    protected boolean temporaryOption = false;
    protected BaseConnection connection;

    protected AbstractCreateSlotBuilder(BaseConnection connection) {
        this.connection = connection;
    }

    protected abstract T self();

    @Override
    public T withSlotName(String slotName) {
        this.slotName = slotName;
        return this.self();
    }

    @Override
    public T withTemporaryOption() throws SQLFeatureNotSupportedException {
        if (!this.connection.haveMinimumServerVersion(ServerVersion.v10)) {
            throw new SQLFeatureNotSupportedException(GT.tr("Server does not support temporary replication slots", new Object[0]));
        }
        this.temporaryOption = true;
        return this.self();
    }
}

