/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent.logical;

import java.util.Properties;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.replication.fluent.CommonOptions;

public interface LogicalReplicationOptions
extends CommonOptions {
    @Override
    public @Nullable String getSlotName();

    public Properties getSlotOptions();
}

