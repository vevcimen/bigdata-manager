/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.ReplicationType;

public final class ReplicationSlotInfo {
    private final String slotName;
    private final ReplicationType replicationType;
    private final LogSequenceNumber consistentPoint;
    private final @Nullable String snapshotName;
    private final @Nullable String outputPlugin;

    public ReplicationSlotInfo(String slotName, ReplicationType replicationType, LogSequenceNumber consistentPoint, @Nullable String snapshotName, @Nullable String outputPlugin) {
        this.slotName = slotName;
        this.replicationType = replicationType;
        this.consistentPoint = consistentPoint;
        this.snapshotName = snapshotName;
        this.outputPlugin = outputPlugin;
    }

    public String getSlotName() {
        return this.slotName;
    }

    public ReplicationType getReplicationType() {
        return this.replicationType;
    }

    public LogSequenceNumber getConsistentPoint() {
        return this.consistentPoint;
    }

    public @Nullable String getSnapshotName() {
        return this.snapshotName;
    }

    public @Nullable String getOutputPlugin() {
        return this.outputPlugin;
    }
}

