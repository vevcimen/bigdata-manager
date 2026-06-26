/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent;

import org.postgresql.replication.fluent.logical.ChainedLogicalCreateSlotBuilder;
import org.postgresql.replication.fluent.physical.ChainedPhysicalCreateSlotBuilder;

public interface ChainedCreateReplicationSlotBuilder {
    public ChainedLogicalCreateSlotBuilder logical();

    public ChainedPhysicalCreateSlotBuilder physical();
}

