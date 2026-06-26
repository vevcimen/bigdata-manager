/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent;

import org.postgresql.core.BaseConnection;
import org.postgresql.replication.fluent.ChainedCreateReplicationSlotBuilder;
import org.postgresql.replication.fluent.logical.ChainedLogicalCreateSlotBuilder;
import org.postgresql.replication.fluent.logical.LogicalCreateSlotBuilder;
import org.postgresql.replication.fluent.physical.ChainedPhysicalCreateSlotBuilder;
import org.postgresql.replication.fluent.physical.PhysicalCreateSlotBuilder;

public class ReplicationCreateSlotBuilder
implements ChainedCreateReplicationSlotBuilder {
    private final BaseConnection baseConnection;

    public ReplicationCreateSlotBuilder(BaseConnection baseConnection) {
        this.baseConnection = baseConnection;
    }

    @Override
    public ChainedLogicalCreateSlotBuilder logical() {
        return new LogicalCreateSlotBuilder(this.baseConnection);
    }

    @Override
    public ChainedPhysicalCreateSlotBuilder physical() {
        return new PhysicalCreateSlotBuilder(this.baseConnection);
    }
}

