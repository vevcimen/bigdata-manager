/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.sql.SQLException;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.logical.LogicalReplicationOptions;
import org.postgresql.replication.fluent.physical.PhysicalReplicationOptions;

public interface ReplicationProtocol {
    public PGReplicationStream startLogical(LogicalReplicationOptions var1) throws SQLException;

    public PGReplicationStream startPhysical(PhysicalReplicationOptions var1) throws SQLException;
}

