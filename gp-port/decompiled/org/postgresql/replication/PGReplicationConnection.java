/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication;

import java.sql.SQLException;
import org.postgresql.replication.fluent.ChainedCreateReplicationSlotBuilder;
import org.postgresql.replication.fluent.ChainedStreamBuilder;

public interface PGReplicationConnection {
    public ChainedStreamBuilder replicationStream();

    public ChainedCreateReplicationSlotBuilder createReplicationSlot();

    public void dropReplicationSlot(String var1) throws SQLException;
}

