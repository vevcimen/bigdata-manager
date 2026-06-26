/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface Gettable<K, V> {
    public @Nullable V get(K var1);
}

