/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent;

import java.sql.SQLException;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.ReplicationProtocol;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.ChainedStreamBuilder;
import org.postgresql.replication.fluent.logical.ChainedLogicalStreamBuilder;
import org.postgresql.replication.fluent.logical.LogicalReplicationOptions;
import org.postgresql.replication.fluent.logical.LogicalStreamBuilder;
import org.postgresql.replication.fluent.logical.StartLogicalReplicationCallback;
import org.postgresql.replication.fluent.physical.ChainedPhysicalStreamBuilder;
import org.postgresql.replication.fluent.physical.PhysicalReplicationOptions;
import org.postgresql.replication.fluent.physical.PhysicalStreamBuilder;
import org.postgresql.replication.fluent.physical.StartPhysicalReplicationCallback;

public class ReplicationStreamBuilder
implements ChainedStreamBuilder {
    private final BaseConnection baseConnection;

    public ReplicationStreamBuilder(BaseConnection connection) {
        this.baseConnection = connection;
    }

    @Override
    public ChainedLogicalStreamBuilder logical() {
        return new LogicalStreamBuilder(new StartLogicalReplicationCallback(){

            @Override
            public PGReplicationStream start(LogicalReplicationOptions options) throws SQLException {
                ReplicationProtocol protocol = ReplicationStreamBuilder.this.baseConnection.getReplicationProtocol();
                return protocol.startLogical(options);
            }
        });
    }

    @Override
    public ChainedPhysicalStreamBuilder physical() {
        return new PhysicalStreamBuilder(new StartPhysicalReplicationCallback(){

            @Override
            public PGReplicationStream start(PhysicalReplicationOptions options) throws SQLException {
                ReplicationProtocol protocol = ReplicationStreamBuilder.this.baseConnection.getReplicationProtocol();
                return protocol.startPhysical(options);
            }
        });
    }
}

