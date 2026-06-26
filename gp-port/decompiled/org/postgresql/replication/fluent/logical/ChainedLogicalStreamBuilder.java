/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent.logical;

import java.sql.SQLException;
import java.util.Properties;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.ChainedCommonStreamBuilder;

public interface ChainedLogicalStreamBuilder
extends ChainedCommonStreamBuilder<ChainedLogicalStreamBuilder> {
    public PGReplicationStream start() throws SQLException;

    public ChainedLogicalStreamBuilder withSlotOption(String var1, boolean var2);

    public ChainedLogicalStreamBuilder withSlotOption(String var1, int var2);

    public ChainedLogicalStreamBuilder withSlotOption(String var1, String var2);

    public ChainedLogicalStreamBuilder withSlotOptions(Properties var1);
}

