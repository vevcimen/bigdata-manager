/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jetty.util.component.Destroyable;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Scheduler;

public abstract class CyclicTimeout
implements Destroyable {
    private static final Logger LOG = Log.getLogger(CyclicTimeout.class);
    private static final Timeout NOT_SET = new Timeout(Long.MAX_VALUE, null);
    private static final Scheduler.Task DESTROYED = () -> false;
    private final Scheduler _scheduler;
    private final AtomicReference<Timeout> _timeout = new AtomicReference<Timeout>(NOT_SET);

    public CyclicTimeout(Scheduler scheduler) {
        this._scheduler = scheduler;
    }

    public Scheduler getScheduler() {
        return this._scheduler;
    }

    public boolean schedule(long delay, TimeUnit units) {
        boolean result;
        Wakeup wakeup;
        Timeout timeout;
        long now = System.nanoTime();
        long newTimeoutAt = now + units.toNanos(delay);
        Wakeup newWakeup = null;
        do {
            result = (timeout = this._timeout.get())._at != Long.MAX_VALUE;
            wakeup = timeout._wakeup;
            if (wakeup != null && wakeup._at <= newTimeoutAt) continue;
            wakeup = newWakeup = new Wakeup(newTimeoutAt, wakeup);
        } while (!this._timeout.compareAndSet(timeout, new Timeout(newTimeoutAt, wakeup)));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Installed timeout in {} ms, waking up in {} ms", units.toMillis(delay), TimeUnit.NANOSECONDS.toMillis(wakeup._at - now));
        }
        if (newWakeup != null) {
            newWakeup.schedule(now);
        }
        return result;
    }

    public boolean cancel() {
        boolean result;
        Wakeup wakeup;
        Timeout newTimeout;
        Timeout timeout;
        do {
            result = (timeout = this._timeout.get())._at != Long.MAX_VALUE;
        } while (!this._timeout.compareAndSet(timeout, newTimeout = (wakeup = timeout._wakeup) == null ? NOT_SET : new Timeout(Long.MAX_VALUE, wakeup)));
        return result;
    }

    public abstract void onTimeoutExpired();

    @Override
    public void destroy() {
        Wakeup wakeup;
        Timeout timeout = this._timeout.getAndSet(NOT_SET);
        Wakeup wakeup2 = wakeup = timeout == null ? null : timeout._wakeup;
        while (wakeup != null) {
            wakeup.destroy();
            wakeup = wakeup._next;
        }
    }

    private class Wakeup
    implements Runnable {
        private final AtomicReference<Scheduler.Task> _task = new AtomicReference();
        private final long _at;
        private final Wakeup _next;

        private Wakeup(long wakeupAt, Wakeup next) {
            this._at = wakeupAt;
            this._next = next;
        }

        private void schedule(long now) {
            this._task.compareAndSet(null, CyclicTimeout.this._scheduler.schedule(this, this._at - now, TimeUnit.NANOSECONDS));
        }

        private void destroy() {
            Scheduler.Task task = this._task.getAndSet(DESTROYED);
            if (task != null) {
                task.cancel();
            }
        }

        @Override
        public void run() {
            Timeout newTimeout;
            Timeout timeout;
            long now = System.nanoTime();
            Wakeup newWakeup = null;
            boolean hasExpired = false;
            do {
                timeout = (Timeout)CyclicTimeout.this._timeout.get();
                Wakeup wakeup = timeout._wakeup;
                while (wakeup != null && wakeup != this) {
                    wakeup = wakeup._next;
                }
                if (wakeup == null) {
                    return;
                }
                wakeup = wakeup._next;
                if (timeout._at <= now) {
                    hasExpired = true;
                    newTimeout = wakeup == null ? NOT_SET : new Timeout(Long.MAX_VALUE, wakeup);
                    continue;
                }
                if (timeout._at != Long.MAX_VALUE) {
                    if (wakeup == null || wakeup._at >= timeout._at) {
                        wakeup = newWakeup = new Wakeup(timeout._at, wakeup);
                    }
                    newTimeout = new Timeout(timeout._at, wakeup);
                    continue;
                }
                Timeout timeout2 = newTimeout = wakeup == null ? NOT_SET : new Timeout(Long.MAX_VALUE, wakeup);
            } while (!CyclicTimeout.this._timeout.compareAndSet(timeout, newTimeout));
            if (newWakeup != null) {
                newWakeup.schedule(now);
            }
            if (hasExpired) {
                CyclicTimeout.this.onTimeoutExpired();
            }
        }

        public String toString() {
            return String.format("%s@%x:%dms->%s", this.getClass().getSimpleName(), this.hashCode(), this._at == Long.MAX_VALUE ? this._at : TimeUnit.NANOSECONDS.toMillis(this._at - System.nanoTime()), this._next);
        }
    }

    private static class Timeout {
        private final long _at;
        private final Wakeup _wakeup;

        private Timeout(long timeoutAt, Wakeup wakeup) {
            this._at = timeoutAt;
            this._wakeup = wakeup;
        }

        public String toString() {
            return String.format("%s@%x:%dms,%s", this.getClass().getSimpleName(), this.hashCode(), TimeUnit.NANOSECONDS.toMillis(this._at - System.nanoTime()), this._wakeup);
        }
    }
}

