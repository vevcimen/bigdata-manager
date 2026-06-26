/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent.logical;

import java.sql.SQLException;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.logical.LogicalReplicationOptions;

public interface StartLogicalReplicationCallback {
    public PGReplicationStream start(LogicalReplicationOptions var1) throws SQLException;
}

