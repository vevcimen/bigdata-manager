/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.replication.LogSequenceNumber;

public interface CommonOptions {
    public @Nullable String getSlotName();

    public LogSequenceNumber getStartLSNPosition();

    public int getStatusInterval();
}

