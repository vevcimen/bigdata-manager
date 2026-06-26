/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.io.SelectorManager;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.eclipse.jetty.util.annotation.Name;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.statistic.RateStatistic;
import org.eclipse.jetty.util.thread.Scheduler;

@ManagedObject
public class AcceptRateLimit
extends AbstractLifeCycle
implements SelectorManager.AcceptListener,
Runnable {
    private static final Logger LOG = Log.getLogger(AcceptRateLimit.class);
    private final Server _server;
    private final List<AbstractConnector> _connectors = new ArrayList<AbstractConnector>();
    private final Rate _rate;
    private final int _acceptRateLimit;
    private boolean _limiting;
    private Scheduler.Task _task;

    public AcceptRateLimit(@Name(value="acceptRateLimit") int acceptRateLimit, @Name(value="period") long period, @Name(value="units") TimeUnit units, @Name(value="server") Server server) {
        this._server = server;
        this._acceptRateLimit = acceptRateLimit;
        this._rate = new Rate(period, units);
    }

    public AcceptRateLimit(@Name(value="limit") int limit, @Name(value="period") long period, @Name(value="units") TimeUnit units, Connector ... connectors) {
        this(limit, period, units, (Server)null);
        for (Connector c : connectors) {
            if (c instanceof AbstractConnector) {
                this._connectors.add((AbstractConnector)c);
                continue;
            }
            LOG.warn("Connector {} is not an AbstractConnector. Connections not limited", c);
        }
    }

    @ManagedAttribute(value="The accept rate limit")
    public int getAcceptRateLimit() {
        return this._acceptRateLimit;
    }

    @ManagedAttribute(value="The accept rate period")
    public long getPeriod() {
        return this._rate.getPeriod();
    }

    @ManagedAttribute(value="The accept rate period units")
    public TimeUnit getUnits() {
        return this._rate.getUnits();
    }

    @ManagedAttribute(value="The current accept rate")
    public int getRate() {
        return this._rate.getRate();
    }

    @ManagedAttribute(value="The maximum accept rate achieved")
    public long getMaxRate() {
        return this._rate.getMax();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @ManagedOperation(value="Resets the accept rate", impact="ACTION")
    public void reset() {
        Rate rate = this._rate;
        synchronized (rate) {
            this._rate.reset();
            if (this._limiting) {
                this._limiting = false;
                this.unlimit();
            }
        }
    }

    protected void age(long period, TimeUnit units) {
        this._rate.age(period, units);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void doStart() throws Exception {
        Rate rate = this._rate;
        synchronized (rate) {
            if (this._server != null) {
                for (Connector c : this._server.getConnectors()) {
                    if (c instanceof AbstractConnector) {
                        this._connectors.add((AbstractConnector)c);
                        continue;
                    }
                    LOG.warn("Connector {} is not an AbstractConnector. Connections not limited", c);
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("AcceptLimit accept<{} rate<{} in {} for {}", this._acceptRateLimit, this._rate, this._connectors);
            }
            for (AbstractConnector c : this._connectors) {
                c.addBean(this);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void doStop() throws Exception {
        Rate rate = this._rate;
        synchronized (rate) {
            if (this._task != null) {
                this._task.cancel();
            }
            this._task = null;
            for (AbstractConnector c : this._connectors) {
                c.removeBean(this);
            }
            if (this._server != null) {
                this._connectors.clear();
            }
            this._limiting = false;
        }
    }

    protected void limit() {
        for (AbstractConnector c : this._connectors) {
            c.setAccepting(false);
        }
        this.schedule();
    }

    protected void unlimit() {
        for (AbstractConnector c : this._connectors) {
            c.setAccepting(true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void onAccepting(SelectableChannel channel) {
        Rate rate = this._rate;
        synchronized (rate) {
            int rate2 = this._rate.record();
            if (LOG.isDebugEnabled()) {
                LOG.debug("onAccepting rate {}/{} for {} {}", rate2, this._acceptRateLimit, this._rate, channel);
            }
            if (rate2 > this._acceptRateLimit && !this._limiting) {
                this._limiting = true;
                LOG.warn("AcceptLimit rate exceeded {}>{} on {}", rate2, this._acceptRateLimit, this._connectors);
                this.limit();
            }
        }
    }

    private void schedule() {
        long oldest = this._rate.getOldest(TimeUnit.MILLISECONDS);
        long period = TimeUnit.MILLISECONDS.convert(this._rate.getPeriod(), this._rate.getUnits());
        long delay = period - (oldest > 0L ? oldest : 0L);
        if (delay < 0L) {
            delay = 0L;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("schedule {} {}", new Object[]{delay, TimeUnit.MILLISECONDS});
        }
        this._task = this._connectors.get(0).getScheduler().schedule(this, delay, TimeUnit.MILLISECONDS);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        Rate rate = this._rate;
        synchronized (rate) {
            this._task = null;
            if (!this.isRunning()) {
                return;
            }
            int rate2 = this._rate.getRate();
            if (rate2 > this._acceptRateLimit) {
                this.schedule();
                return;
            }
            if (this._limiting) {
                this._limiting = false;
                LOG.warn("AcceptLimit rate OK {}<={} on {}", rate2, this._acceptRateLimit, this._connectors);
                this.unlimit();
            }
        }
    }

    private final class Rate
    extends RateStatistic {
        private Rate(long period, TimeUnit units) {
            super(period, units);
        }

        @Override
        protected void age(long period, TimeUnit units) {
            super.age(period, units);
        }
    }
}

