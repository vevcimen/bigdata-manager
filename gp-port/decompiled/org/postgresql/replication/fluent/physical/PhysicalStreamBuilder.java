/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent.physical;

import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.AbstractStreamBuilder;
import org.postgresql.replication.fluent.physical.ChainedPhysicalStreamBuilder;
import org.postgresql.replication.fluent.physical.PhysicalReplicationOptions;
import org.postgresql.replication.fluent.physical.StartPhysicalReplicationCallback;

public class PhysicalStreamBuilder
extends AbstractStreamBuilder<ChainedPhysicalStreamBuilder>
implements ChainedPhysicalStreamBuilder,
PhysicalReplicationOptions {
    private final StartPhysicalReplicationCallback startCallback;

    public PhysicalStreamBuilder(StartPhysicalReplicationCallback startCallback) {
        this.startCallback = startCallback;
    }

    @Override
    protected ChainedPhysicalStreamBuilder self() {
        return this;
    }

    @Override
    public PGReplicationStream start() throws SQLException {
        return this.startCallback.start(this);
    }

    @Override
    public @Nullable String getSlotName() {
        return this.slotName;
    }

    @Override
    public LogSequenceNumber getStartLSNPosition() {
        return this.startPosition;
    }

    @Override
    public int getStatusInterval() {
        return this.statusIntervalMs;
    }
}

