/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util.internal;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

public class Nullness {
    @Pure
    @EnsuresNonNull(value={"#1"})
    public static <T> @NonNull T castNonNull(@Nullable T ref) {
        assert (ref != null) : "Misuse of castNonNull: called with a null argument";
        return ref;
    }

    @Pure
    @EnsuresNonNull(value={"#1"})
    public static <T> @NonNull T castNonNull(@Nullable T ref, String message) {
        assert (ref != null) : "Misuse of castNonNull: called with a null argument " + message;
        return ref;
    }
}

