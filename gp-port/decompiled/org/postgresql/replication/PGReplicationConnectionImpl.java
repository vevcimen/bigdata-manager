/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication;

import java.sql.SQLException;
import java.sql.Statement;
import org.postgresql.core.BaseConnection;
import org.postgresql.replication.PGReplicationConnection;
import org.postgresql.replication.fluent.ChainedCreateReplicationSlotBuilder;
import org.postgresql.replication.fluent.ChainedStreamBuilder;
import org.postgresql.replication.fluent.ReplicationCreateSlotBuilder;
import org.postgresql.replication.fluent.ReplicationStreamBuilder;

public class PGReplicationConnectionImpl
implements PGReplicationConnection {
    private BaseConnection connection;

    public PGReplicationConnectionImpl(BaseConnection connection) {
        this.connection = connection;
    }

    @Override
    public ChainedStreamBuilder replicationStream() {
        return new ReplicationStreamBuilder(this.connection);
    }

    @Override
    public ChainedCreateReplicationSlotBuilder createReplicationSlot() {
        return new ReplicationCreateSlotBuilder(this.connection);
    }

    @Override
    public void dropReplicationSlot(String slotName) throws SQLException {
        if (slotName == null || slotName.isEmpty()) {
            throw new IllegalArgumentException("Replication slot name can't be null or empty");
        }
        try (Statement statement = this.connection.createStatement();){
            statement.execute("DROP_REPLICATION_SLOT " + slotName);
        }
    }
}

