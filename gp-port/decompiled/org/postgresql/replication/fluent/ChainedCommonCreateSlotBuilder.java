/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import org.postgresql.replication.ReplicationSlotInfo;

public interface ChainedCommonCreateSlotBuilder<T extends ChainedCommonCreateSlotBuilder<T>> {
    public T withSlotName(String var1);

    public T withTemporaryOption() throws SQLFeatureNotSupportedException;

    public ReplicationSlotInfo make() throws SQLException;
}

