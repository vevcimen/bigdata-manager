/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent.physical;

import java.sql.SQLException;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.physical.PhysicalReplicationOptions;

public interface StartPhysicalReplicationCallback {
    public PGReplicationStream start(PhysicalReplicationOptions var1) throws SQLException;
}

