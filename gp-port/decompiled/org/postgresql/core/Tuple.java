/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

public class Tuple {
    private final boolean forUpdate;
    final byte[] @Nullable [] data;

    public Tuple(int length) {
        this(new byte[length][], true);
    }

    public Tuple(byte[] @Nullable [] data) {
        this(data, false);
    }

    private Tuple(byte[] @Nullable [] data, boolean forUpdate) {
        this.data = data;
        this.forUpdate = forUpdate;
    }

    public @NonNegative int fieldCount() {
        return this.data.length;
    }

    public @NonNegative int length() {
        int length = 0;
        for (byte[] field : this.data) {
            if (field == null) continue;
            length += field.length;
        }
        return length;
    }

    @Pure
    public byte @Nullable [] get(@NonNegative int index) {
        return this.data[index];
    }

    public Tuple updateableCopy() {
        return this.copy(true);
    }

    public Tuple readOnlyCopy() {
        return this.copy(false);
    }

    private Tuple copy(boolean forUpdate) {
        byte[][] dataCopy = new byte[this.data.length][];
        System.arraycopy(this.data, 0, dataCopy, 0, this.data.length);
        return new Tuple(dataCopy, forUpdate);
    }

    public void set(@NonNegative int index, byte @Nullable [] fieldData) {
        if (!this.forUpdate) {
            throw new IllegalArgumentException("Attempted to write to readonly tuple");
        }
        this.data[index] = fieldData;
    }
}

