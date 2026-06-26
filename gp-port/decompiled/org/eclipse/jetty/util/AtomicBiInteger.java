/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicBiInteger
extends AtomicLong {
    public AtomicBiInteger() {
    }

    public AtomicBiInteger(long encoded) {
        super(encoded);
    }

    public AtomicBiInteger(int hi, int lo) {
        super(AtomicBiInteger.encode(hi, lo));
    }

    public int getHi() {
        return AtomicBiInteger.getHi(this.get());
    }

    public int getLo() {
        return AtomicBiInteger.getLo(this.get());
    }

    public int getAndSetHi(int hi) {
        long update;
        long encoded;
        while (!this.compareAndSet(encoded = this.get(), update = AtomicBiInteger.encodeHi(encoded, hi))) {
        }
        return AtomicBiInteger.getHi(encoded);
    }

    public int getAndSetLo(int lo) {
        long update;
        long encoded;
        while (!this.compareAndSet(encoded = this.get(), update = AtomicBiInteger.encodeLo(encoded, lo))) {
        }
        return AtomicBiInteger.getLo(encoded);
    }

    public void set(int hi, int lo) {
        this.set(AtomicBiInteger.encode(hi, lo));
    }

    public boolean compareAndSetHi(int expectHi, int hi) {
        long update;
        long encoded;
        do {
            if (AtomicBiInteger.getHi(encoded = this.get()) == expectHi) continue;
            return false;
        } while (!this.compareAndSet(encoded, update = AtomicBiInteger.encodeHi(encoded, hi)));
        return true;
    }

    public boolean compareAndSetLo(int expectLo, int lo) {
        long update;
        long encoded;
        do {
            if (AtomicBiInteger.getLo(encoded = this.get()) == expectLo) continue;
            return false;
        } while (!this.compareAndSet(encoded, update = AtomicBiInteger.encodeLo(encoded, lo)));
        return true;
    }

    public boolean compareAndSet(long encoded, int hi, int lo) {
        long update = AtomicBiInteger.encode(hi, lo);
        return this.compareAndSet(encoded, update);
    }

    public boolean compareAndSet(int expectHi, int hi, int expectLo, int lo) {
        long encoded = AtomicBiInteger.encode(expectHi, expectLo);
        long update = AtomicBiInteger.encode(hi, lo);
        return this.compareAndSet(encoded, update);
    }

    public int addAndGetHi(int delta) {
        int hi;
        long update;
        long encoded;
        while (!this.compareAndSet(encoded = this.get(), update = AtomicBiInteger.encodeHi(encoded, hi = AtomicBiInteger.getHi(encoded) + delta))) {
        }
        return hi;
    }

    public int addAndGetLo(int delta) {
        int lo;
        long update;
        long encoded;
        while (!this.compareAndSet(encoded = this.get(), update = AtomicBiInteger.encodeLo(encoded, lo = AtomicBiInteger.getLo(encoded) + delta))) {
        }
        return lo;
    }

    public void add(int deltaHi, int deltaLo) {
        long update;
        long encoded;
        while (!this.compareAndSet(encoded = this.get(), update = AtomicBiInteger.encode(AtomicBiInteger.getHi(encoded) + deltaHi, AtomicBiInteger.getLo(encoded) + deltaLo))) {
        }
    }

    public static int getHi(long encoded) {
        return (int)(encoded >> 32 & 0xFFFFFFFFL);
    }

    public static int getLo(long encoded) {
        return (int)(encoded & 0xFFFFFFFFL);
    }

    public static long encode(int hi, int lo) {
        long h2 = (long)hi & 0xFFFFFFFFL;
        long l = (long)lo & 0xFFFFFFFFL;
        return (h2 << 32) + l;
    }

    public static long encodeHi(long encoded, int hi) {
        long h2 = (long)hi & 0xFFFFFFFFL;
        long l = encoded & 0xFFFFFFFFL;
        return (h2 << 32) + l;
    }

    public static long encodeLo(long encoded, int lo) {
        long h2 = encoded >> 32 & 0xFFFFFFFFL;
        long l = (long)lo & 0xFFFFFFFFL;
        return (h2 << 32) + l;
    }
}

