/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication.fluent;

import java.util.concurrent.TimeUnit;
import org.postgresql.replication.LogSequenceNumber;

public interface ChainedCommonStreamBuilder<T extends ChainedCommonStreamBuilder<T>> {
    public T withSlotName(String var1);

    public T withStatusInterval(int var1, TimeUnit var2);

    public T withStartPosition(LogSequenceNumber var1);
}

