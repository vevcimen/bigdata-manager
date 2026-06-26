/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent.logical;

import java.sql.SQLException;
import java.util.Properties;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.AbstractStreamBuilder;
import org.postgresql.replication.fluent.logical.ChainedLogicalStreamBuilder;
import org.postgresql.replication.fluent.logical.LogicalReplicationOptions;
import org.postgresql.replication.fluent.logical.StartLogicalReplicationCallback;
import org.postgresql.util.internal.Nullness;

public class LogicalStreamBuilder
extends AbstractStreamBuilder<ChainedLogicalStreamBuilder>
implements ChainedLogicalStreamBuilder,
LogicalReplicationOptions {
    private final Properties slotOptions;
    private StartLogicalReplicationCallback startCallback;

    public LogicalStreamBuilder(StartLogicalReplicationCallback startCallback) {
        this.startCallback = startCallback;
        this.slotOptions = new Properties();
    }

    @Override
    protected ChainedLogicalStreamBuilder self() {
        return this;
    }

    @Override
    public PGReplicationStream start() throws SQLException {
        return this.startCallback.start(this);
    }

    @Override
    public @Nullable String getSlotName() {
        return this.slotName;
    }

    @Override
    public ChainedLogicalStreamBuilder withStartPosition(LogSequenceNumber lsn) {
        this.startPosition = lsn;
        return this;
    }

    @Override
    public ChainedLogicalStreamBuilder withSlotOption(String optionName, boolean optionValue) {
        this.slotOptions.setProperty(optionName, String.valueOf(optionValue));
        return this;
    }

    @Override
    public ChainedLogicalStreamBuilder withSlotOption(String optionName, int optionValue) {
        this.slotOptions.setProperty(optionName, String.valueOf(optionValue));
        return this;
    }

    @Override
    public ChainedLogicalStreamBuilder withSlotOption(String optionName, String optionValue) {
        this.slotOptions.setProperty(optionName, optionValue);
        return this;
    }

    @Override
    public ChainedLogicalStreamBuilder withSlotOptions(Properties options) {
        for (String propertyName : options.stringPropertyNames()) {
            this.slotOptions.setProperty(propertyName, Nullness.castNonNull(options.getProperty(propertyName)));
        }
        return this;
    }

    @Override
    public LogSequenceNumber getStartLSNPosition() {
        return this.startPosition;
    }

    @Override
    public Properties getSlotOptions() {
        return this.slotOptions;
    }

    @Override
    public int getStatusInterval() {
        return this.statusIntervalMs;
    }
}

