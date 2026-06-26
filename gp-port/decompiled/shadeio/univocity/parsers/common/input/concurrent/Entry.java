/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input.concurrent;

class Entry<T> {
    final T entry;
    final int index;

    Entry(T entry, int index) {
        this.entry = entry;
        this.index = index;
    }

    public T get() {
        return this.entry;
    }
}

