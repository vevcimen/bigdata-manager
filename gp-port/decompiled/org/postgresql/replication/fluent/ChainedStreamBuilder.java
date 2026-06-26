/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent;

import org.postgresql.replication.fluent.logical.ChainedLogicalStreamBuilder;
import org.postgresql.replication.fluent.physical.ChainedPhysicalStreamBuilder;

public interface ChainedStreamBuilder {
    public ChainedLogicalStreamBuilder logical();

    public ChainedPhysicalStreamBuilder physical();
}

