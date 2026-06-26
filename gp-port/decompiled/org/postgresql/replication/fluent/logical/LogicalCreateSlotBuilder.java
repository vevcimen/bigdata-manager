/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent.logical;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.BaseConnection;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.ReplicationSlotInfo;
import org.postgresql.replication.ReplicationType;
import org.postgresql.replication.fluent.AbstractCreateSlotBuilder;
import org.postgresql.replication.fluent.logical.ChainedLogicalCreateSlotBuilder;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class LogicalCreateSlotBuilder
extends AbstractCreateSlotBuilder<ChainedLogicalCreateSlotBuilder>
implements ChainedLogicalCreateSlotBuilder {
    private @Nullable String outputPlugin;

    public LogicalCreateSlotBuilder(BaseConnection connection) {
        super(connection);
    }

    @Override
    protected ChainedLogicalCreateSlotBuilder self() {
        return this;
    }

    @Override
    public ChainedLogicalCreateSlotBuilder withOutputPlugin(String outputPlugin) {
        this.outputPlugin = outputPlugin;
        return this.self();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ReplicationSlotInfo make() throws SQLException {
        ReplicationSlotInfo slotInfo;
        block7: {
            String outputPlugin = this.outputPlugin;
            if (outputPlugin == null || outputPlugin.isEmpty()) {
                throw new IllegalArgumentException("OutputPlugin required parameter for logical replication slot");
            }
            if (this.slotName == null || this.slotName.isEmpty()) {
                throw new IllegalArgumentException("Replication slotName can't be null");
            }
            Statement statement = this.connection.createStatement();
            ResultSet result = null;
            slotInfo = null;
            try {
                String sql = String.format("CREATE_REPLICATION_SLOT %s %s LOGICAL %s", this.slotName, this.temporaryOption ? "TEMPORARY" : "", outputPlugin);
                statement.execute(sql);
                result = statement.getResultSet();
                if (result != null && result.next()) {
                    slotInfo = new ReplicationSlotInfo(Nullness.castNonNull(result.getString("slot_name")), ReplicationType.LOGICAL, LogSequenceNumber.valueOf(Nullness.castNonNull(result.getString("consistent_point"))), result.getString("snapshot_name"), result.getString("output_plugin"));
                    break block7;
                }
                throw new PSQLException(GT.tr("{0} returned no results", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
            }
            finally {
                if (result != null) {
                    result.close();
                }
                statement.close();
            }
        }
        return slotInfo;
    }
}

