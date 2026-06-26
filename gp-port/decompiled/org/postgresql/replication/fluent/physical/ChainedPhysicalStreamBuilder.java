/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent.physical;

import java.sql.SQLException;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.ChainedCommonStreamBuilder;

public interface ChainedPhysicalStreamBuilder
extends ChainedCommonStreamBuilder<ChainedPhysicalStreamBuilder> {
    public PGReplicationStream start() throws SQLException;
}

