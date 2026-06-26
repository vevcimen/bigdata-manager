/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Scheduler;

public abstract class IdleTimeout {
    private static final Logger LOG = Log.getLogger(IdleTimeout.class);
    private final Scheduler _scheduler;
    private final AtomicReference<Scheduler.Task> _timeout = new AtomicReference();
    private volatile long _idleTimeout;
    private volatile long _idleTimestamp = System.nanoTime();

    public IdleTimeout(Scheduler scheduler) {
        this._scheduler = scheduler;
    }

    public Scheduler getScheduler() {
        return this._scheduler;
    }

    public long getIdleFor() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - this._idleTimestamp);
    }

    public long getIdleTimeout() {
        return this._idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        long old = this._idleTimeout;
        this._idleTimeout = idleTimeout;
        if (old > 0L) {
            if (old <= idleTimeout) {
                return;
            }
            this.deactivate();
        }
        if (this.isOpen()) {
            this.activate();
        }
    }

    public void notIdle() {
        this._idleTimestamp = System.nanoTime();
    }

    private void idleCheck() {
        long idleLeft = this.checkIdleTimeout();
        if (idleLeft >= 0L) {
            this.scheduleIdleTimeout(idleLeft > 0L ? idleLeft : this.getIdleTimeout());
        }
    }

    private void scheduleIdleTimeout(long delay) {
        Scheduler.Task oldTimeout;
        Scheduler.Task newTimeout = null;
        if (this.isOpen() && delay > 0L && this._scheduler != null) {
            newTimeout = this._scheduler.schedule(this::idleCheck, delay, TimeUnit.MILLISECONDS);
        }
        if ((oldTimeout = (Scheduler.Task)this._timeout.getAndSet(newTimeout)) != null) {
            oldTimeout.cancel();
        }
    }

    public void onOpen() {
        this.activate();
    }

    private void activate() {
        if (this._idleTimeout > 0L) {
            this.idleCheck();
        }
    }

    public void onClose() {
        this.deactivate();
    }

    private void deactivate() {
        Scheduler.Task oldTimeout = this._timeout.getAndSet(null);
        if (oldTimeout != null) {
            oldTimeout.cancel();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected long checkIdleTimeout() {
        if (this.isOpen()) {
            long idleTimestamp = this._idleTimestamp;
            long idleElapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - idleTimestamp);
            long idleTimeout = this.getIdleTimeout();
            long idleLeft = idleTimeout - idleElapsed;
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} idle timeout check, elapsed: {} ms, remaining: {} ms", this, idleElapsed, idleLeft);
            }
            if (idleTimeout > 0L && idleLeft <= 0L) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{} idle timeout expired", this);
                }
                try {
                    this.onIdleExpired(new TimeoutException("Idle timeout expired: " + idleElapsed + "/" + idleTimeout + " ms"));
                }
                finally {
                    this.notIdle();
                }
            }
            return idleLeft >= 0L ? idleLeft : 0L;
        }
        return -1L;
    }

    protected abstract void onIdleExpired(TimeoutException var1);

    public abstract boolean isOpen();
}

