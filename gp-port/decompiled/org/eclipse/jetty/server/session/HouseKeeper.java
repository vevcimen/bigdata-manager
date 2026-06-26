/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.session;

import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;

@ManagedObject
public class HouseKeeper
extends AbstractLifeCycle {
    private static final Logger LOG = Log.getLogger("org.eclipse.jetty.server.session");
    public static final long DEFAULT_PERIOD_MS = 600000L;
    protected SessionIdManager _sessionIdManager;
    protected Scheduler _scheduler;
    protected Scheduler.Task _task;
    protected Runner _runner;
    protected boolean _ownScheduler = false;
    private long _intervalMs = 600000L;

    public void setSessionIdManager(SessionIdManager sessionIdManager) {
        if (this.isStarted()) {
            throw new IllegalStateException("HouseKeeper started");
        }
        this._sessionIdManager = sessionIdManager;
    }

    @Override
    protected void doStart() throws Exception {
        if (this._sessionIdManager == null) {
            throw new IllegalStateException("No SessionIdManager for Housekeeper");
        }
        this.setIntervalSec(this.getIntervalSec());
        super.doStart();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void startScavenging() throws Exception {
        HouseKeeper houseKeeper = this;
        synchronized (houseKeeper) {
            if (this._scheduler == null) {
                if (this._sessionIdManager instanceof DefaultSessionIdManager) {
                    this._scheduler = ((DefaultSessionIdManager)this._sessionIdManager).getServer().getBean(Scheduler.class);
                }
                if (this._scheduler == null) {
                    this._scheduler = new ScheduledExecutorScheduler(String.format("Session-HouseKeeper-%x", this.hashCode()), false);
                    this._ownScheduler = true;
                    this._scheduler.start();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Using own scheduler for scavenging", new Object[0]);
                    }
                } else if (!this._scheduler.isStarted()) {
                    throw new IllegalStateException("Shared scheduler not started");
                }
            }
            if (this._task != null) {
                this._task.cancel();
            }
            if (this._runner == null) {
                this._runner = new Runner();
            }
            LOG.info("{} Scavenging every {}ms", this._sessionIdManager.getWorkerName(), this._intervalMs);
            this._task = this._scheduler.schedule(this._runner, this._intervalMs, TimeUnit.MILLISECONDS);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void stopScavenging() throws Exception {
        HouseKeeper houseKeeper = this;
        synchronized (houseKeeper) {
            if (this._task != null) {
                this._task.cancel();
                LOG.info("{} Stopped scavenging", this._sessionIdManager.getWorkerName());
            }
            this._task = null;
            if (this._ownScheduler && this._scheduler != null) {
                this._ownScheduler = false;
                this._scheduler.stop();
                this._scheduler = null;
            }
            this._runner = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void doStop() throws Exception {
        HouseKeeper houseKeeper = this;
        synchronized (houseKeeper) {
            this.stopScavenging();
            this._scheduler = null;
        }
        super.doStop();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setIntervalSec(long sec) throws Exception {
        HouseKeeper houseKeeper = this;
        synchronized (houseKeeper) {
            if (this.isStarted() || this.isStarting()) {
                if (sec <= 0L) {
                    this._intervalMs = 0L;
                    LOG.info("{} Scavenging disabled", this._sessionIdManager.getWorkerName());
                    this.stopScavenging();
                } else {
                    if (sec < 10L) {
                        LOG.warn("{} Short interval of {}sec for session scavenging.", this._sessionIdManager.getWorkerName(), sec);
                    }
                    this._intervalMs = sec * 1000L;
                    long tenPercent = this._intervalMs / 10L;
                    if (System.currentTimeMillis() % 2L == 0L) {
                        this._intervalMs += tenPercent;
                    }
                    if (this.isStarting() || this.isStarted()) {
                        this.startScavenging();
                    }
                }
            } else {
                this._intervalMs = sec * 1000L;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @ManagedAttribute(value="secs between scavenge cycles", readonly=true)
    public long getIntervalSec() {
        HouseKeeper houseKeeper = this;
        synchronized (houseKeeper) {
            return this._intervalMs / 1000L;
        }
    }

    public void scavenge() {
        if (this.isStopping() || this.isStopped()) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} scavenging sessions", this._sessionIdManager.getWorkerName());
        }
        for (SessionHandler manager : this._sessionIdManager.getSessionHandlers()) {
            if (manager == null) continue;
            try {
                manager.scavenge();
            }
            catch (Exception e) {
                LOG.warn(e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String toString() {
        HouseKeeper houseKeeper = this;
        synchronized (houseKeeper) {
            return super.toString() + "[interval=" + this._intervalMs + ", ownscheduler=" + this._ownScheduler + "]";
        }
    }

    protected class Runner
    implements Runnable {
        protected Runner() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            try {
                HouseKeeper.this.scavenge();
            }
            finally {
                HouseKeeper houseKeeper = HouseKeeper.this;
                synchronized (houseKeeper) {
                    if (HouseKeeper.this._scheduler != null && HouseKeeper.this._scheduler.isRunning()) {
                        HouseKeeper.this._task = HouseKeeper.this._scheduler.schedule(this, HouseKeeper.this._intervalMs, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }
    }
}

