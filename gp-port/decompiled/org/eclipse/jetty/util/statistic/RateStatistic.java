/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.statistic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RateStatistic {
    private final Deque<Long> _samples = new ArrayDeque<Long>();
    private final long _nanoPeriod;
    private final TimeUnit _units;
    private long _max;
    private long _count;

    public RateStatistic(long period, TimeUnit units) {
        this._nanoPeriod = TimeUnit.NANOSECONDS.convert(period, units);
        this._units = units;
    }

    public long getPeriod() {
        return this._units.convert(this._nanoPeriod, TimeUnit.NANOSECONDS);
    }

    public TimeUnit getUnits() {
        return this._units;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void reset() {
        RateStatistic rateStatistic = this;
        synchronized (rateStatistic) {
            this._samples.clear();
            this._max = 0L;
            this._count = 0L;
        }
    }

    private void update() {
        this.update(System.nanoTime());
    }

    private void update(long now) {
        long expire = now - this._nanoPeriod;
        Long head = this._samples.peekFirst();
        while (head != null && head < expire) {
            this._samples.removeFirst();
            head = this._samples.peekFirst();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void age(long period, TimeUnit units) {
        long increment = TimeUnit.NANOSECONDS.convert(period, units);
        RateStatistic rateStatistic = this;
        synchronized (rateStatistic) {
            int size = this._samples.size();
            for (int i = 0; i < size; ++i) {
                this._samples.addLast(this._samples.removeFirst() - increment);
            }
            this.update();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int record() {
        long now = System.nanoTime();
        RateStatistic rateStatistic = this;
        synchronized (rateStatistic) {
            ++this._count;
            this._samples.add(now);
            this.update(now);
            int rate = this._samples.size();
            if ((long)rate > this._max) {
                this._max = rate;
            }
            return rate;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getRate() {
        RateStatistic rateStatistic = this;
        synchronized (rateStatistic) {
            this.update();
            return this._samples.size();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long getMax() {
        RateStatistic rateStatistic = this;
        synchronized (rateStatistic) {
            return this._max;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long getOldest(TimeUnit units) {
        RateStatistic rateStatistic = this;
        synchronized (rateStatistic) {
            Long head = this._samples.peekFirst();
            if (head == null) {
                return -1L;
            }
            return units.convert(System.nanoTime() - head, TimeUnit.NANOSECONDS);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long getCount() {
        RateStatistic rateStatistic = this;
        synchronized (rateStatistic) {
            return this._count;
        }
    }

    public String dump() {
        return this.dump(TimeUnit.MINUTES);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String dump(TimeUnit units) {
        long now = System.nanoTime();
        RateStatistic rateStatistic = this;
        synchronized (rateStatistic) {
            String samples = this._samples.stream().mapToLong(t -> units.convert(now - t, TimeUnit.NANOSECONDS)).mapToObj(Long::toString).collect(Collectors.joining(System.lineSeparator()));
            return String.format("%s%n%s", this.toString(now), samples);
        }
    }

    public String toString() {
        return this.toString(System.nanoTime());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String toString(long nanoTime) {
        RateStatistic rateStatistic = this;
        synchronized (rateStatistic) {
            this.update(nanoTime);
            return String.format("%s@%x{count=%d,max=%d,rate=%d per %d %s}", new Object[]{this.getClass().getSimpleName(), this.hashCode(), this._count, this._max, this._samples.size(), this._units.convert(this._nanoPeriod, TimeUnit.NANOSECONDS), this._units});
        }
    }
}

