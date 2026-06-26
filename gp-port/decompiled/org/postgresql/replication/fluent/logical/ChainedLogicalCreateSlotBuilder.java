/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent.logical;

import org.postgresql.replication.fluent.ChainedCommonCreateSlotBuilder;

public interface ChainedLogicalCreateSlotBuilder
extends ChainedCommonCreateSlotBuilder<ChainedLogicalCreateSlotBuilder> {
    public ChainedLogicalCreateSlotBuilder withOutputPlugin(String var1);
}

