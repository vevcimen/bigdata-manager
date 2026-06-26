/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3.replication;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgresql.copy.CopyDual;
import org.postgresql.core.PGStream;
import org.postgresql.core.QueryExecutor;
import org.postgresql.core.ReplicationProtocol;
import org.postgresql.core.v3.replication.V3PGReplicationStream;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.ReplicationType;
import org.postgresql.replication.fluent.CommonOptions;
import org.postgresql.replication.fluent.logical.LogicalReplicationOptions;
import org.postgresql.replication.fluent.physical.PhysicalReplicationOptions;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class V3ReplicationProtocol
implements ReplicationProtocol {
    private static final Logger LOGGER = Logger.getLogger(V3ReplicationProtocol.class.getName());
    private final QueryExecutor queryExecutor;
    private final PGStream pgStream;

    public V3ReplicationProtocol(QueryExecutor queryExecutor, PGStream pgStream) {
        this.queryExecutor = queryExecutor;
        this.pgStream = pgStream;
    }

    @Override
    public PGReplicationStream startLogical(LogicalReplicationOptions options) throws SQLException {
        String query = this.createStartLogicalQuery(options);
        return this.initializeReplication(query, options, ReplicationType.LOGICAL);
    }

    @Override
    public PGReplicationStream startPhysical(PhysicalReplicationOptions options) throws SQLException {
        String query = this.createStartPhysicalQuery(options);
        return this.initializeReplication(query, options, ReplicationType.PHYSICAL);
    }

    private PGReplicationStream initializeReplication(String query, CommonOptions options, ReplicationType replicationType) throws SQLException {
        LOGGER.log(Level.FINEST, " FE=> StartReplication(query: {0})", query);
        this.configureSocketTimeout(options);
        CopyDual copyDual = (CopyDual)this.queryExecutor.startCopy(query, true);
        return new V3PGReplicationStream(Nullness.castNonNull(copyDual), options.getStartLSNPosition(), options.getStatusInterval(), replicationType);
    }

    private String createStartPhysicalQuery(PhysicalReplicationOptions options) {
        StringBuilder builder = new StringBuilder();
        builder.append("START_REPLICATION");
        if (options.getSlotName() != null) {
            builder.append(" SLOT ").append(options.getSlotName());
        }
        builder.append(" PHYSICAL ").append(options.getStartLSNPosition().asString());
        return builder.toString();
    }

    private String createStartLogicalQuery(LogicalReplicationOptions options) {
        StringBuilder builder = new StringBuilder();
        builder.append("START_REPLICATION SLOT ").append(options.getSlotName()).append(" LOGICAL ").append(options.getStartLSNPosition().asString());
        Properties slotOptions = options.getSlotOptions();
        if (slotOptions.isEmpty()) {
            return builder.toString();
        }
        builder.append(" (");
        boolean isFirst = true;
        for (String name : slotOptions.stringPropertyNames()) {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append(", ");
            }
            builder.append('\"').append(name).append('\"').append(" ").append('\'').append(slotOptions.getProperty(name)).append('\'');
        }
        builder.append(")");
        return builder.toString();
    }

    private void configureSocketTimeout(CommonOptions options) throws PSQLException {
        if (options.getStatusInterval() == 0) {
            return;
        }
        try {
            int previousTimeOut = this.pgStream.getSocket().getSoTimeout();
            int minimalTimeOut = previousTimeOut > 0 ? Math.min(previousTimeOut, options.getStatusInterval()) : options.getStatusInterval();
            this.pgStream.getSocket().setSoTimeout(minimalTimeOut);
            this.pgStream.setMinStreamAvailableCheckDelay(0);
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("The connection attempt failed.", new Object[0]), PSQLState.CONNECTION_UNABLE_TO_CONNECT, (Throwable)ioe);
        }
    }
}

