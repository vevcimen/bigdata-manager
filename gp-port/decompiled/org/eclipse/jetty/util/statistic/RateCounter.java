/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.statistic;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class RateCounter {
    private final LongAdder _total = new LongAdder();
    private final AtomicLong _timeStamp = new AtomicLong(System.nanoTime());

    public void add(long l) {
        this._total.add(l);
    }

    public long getRate() {
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - this._timeStamp.get());
        return elapsed == 0L ? 0L : this._total.sum() * 1000L / elapsed;
    }

    public void reset() {
        this._timeStamp.getAndSet(System.nanoTime());
        this._total.reset();
    }
}

