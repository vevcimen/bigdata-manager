/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.io.IOException;
import java.util.concurrent.atomic.LongAdder;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.statistic.CounterStatistic;
import org.eclipse.jetty.util.statistic.RateCounter;
import org.eclipse.jetty.util.statistic.SampleStatistic;

@ManagedObject(value="Tracks statistics on connections")
public class ConnectionStatistics
extends AbstractLifeCycle
implements Connection.Listener,
Dumpable {
    private final CounterStatistic _connections = new CounterStatistic();
    private final SampleStatistic _connectionsDuration = new SampleStatistic();
    private final LongAdder _bytesIn = new LongAdder();
    private final LongAdder _bytesOut = new LongAdder();
    private final LongAdder _messagesIn = new LongAdder();
    private final LongAdder _messagesOut = new LongAdder();
    private final RateCounter _bytesInRate = new RateCounter();
    private final RateCounter _bytesOutRate = new RateCounter();
    private final RateCounter _messagesInRate = new RateCounter();
    private final RateCounter _messagesOutRate = new RateCounter();

    @ManagedOperation(value="Resets the statistics", impact="ACTION")
    public void reset() {
        this._connections.reset();
        this._connectionsDuration.reset();
        this._bytesIn.reset();
        this._bytesOut.reset();
        this._messagesIn.reset();
        this._messagesOut.reset();
        this._bytesInRate.reset();
        this._bytesOutRate.reset();
        this._messagesInRate.reset();
        this._messagesOutRate.reset();
    }

    @Override
    protected void doStart() throws Exception {
        this.reset();
    }

    @Override
    public void onOpened(Connection connection) {
        if (!this.isStarted()) {
            return;
        }
        this._connections.increment();
    }

    @Override
    public void onClosed(Connection connection) {
        long messagesOut;
        long messagesIn;
        long bytesOut;
        if (!this.isStarted()) {
            return;
        }
        this._connections.decrement();
        this._connectionsDuration.record(System.currentTimeMillis() - connection.getCreatedTimeStamp());
        long bytesIn = connection.getBytesIn();
        if (bytesIn > 0L) {
            this._bytesIn.add(bytesIn);
            this._bytesInRate.add(bytesIn);
        }
        if ((bytesOut = connection.getBytesOut()) > 0L) {
            this._bytesOut.add(bytesOut);
            this._bytesOutRate.add(bytesOut);
        }
        if ((messagesIn = connection.getMessagesIn()) > 0L) {
            this._messagesIn.add(messagesIn);
            this._messagesInRate.add(messagesIn);
        }
        if ((messagesOut = connection.getMessagesOut()) > 0L) {
            this._messagesOut.add(messagesOut);
            this._messagesOutRate.add(messagesOut);
        }
    }

    @ManagedAttribute(value="Total number of bytes received by tracked connections")
    public long getReceivedBytes() {
        return this._bytesIn.sum();
    }

    @ManagedAttribute(value="Total number of bytes received per second since the last invocation of this method")
    public long getReceivedBytesRate() {
        long rate = this._bytesInRate.getRate();
        this._bytesInRate.reset();
        return rate;
    }

    @ManagedAttribute(value="Total number of bytes sent by tracked connections")
    public long getSentBytes() {
        return this._bytesOut.sum();
    }

    @ManagedAttribute(value="Total number of bytes sent per second since the last invocation of this method")
    public long getSentBytesRate() {
        long rate = this._bytesOutRate.getRate();
        this._bytesOutRate.reset();
        return rate;
    }

    @ManagedAttribute(value="The max duration of a connection in ms")
    public long getConnectionDurationMax() {
        return this._connectionsDuration.getMax();
    }

    @ManagedAttribute(value="The mean duration of a connection in ms")
    public double getConnectionDurationMean() {
        return this._connectionsDuration.getMean();
    }

    @ManagedAttribute(value="The standard deviation of the duration of a connection")
    public double getConnectionDurationStdDev() {
        return this._connectionsDuration.getStdDev();
    }

    @ManagedAttribute(value="The total number of connections opened")
    public long getConnectionsTotal() {
        return this._connections.getTotal();
    }

    @ManagedAttribute(value="The current number of open connections")
    public long getConnections() {
        return this._connections.getCurrent();
    }

    @ManagedAttribute(value="The max number of open connections")
    public long getConnectionsMax() {
        return this._connections.getMax();
    }

    @ManagedAttribute(value="The total number of messages received")
    public long getReceivedMessages() {
        return this._messagesIn.sum();
    }

    @ManagedAttribute(value="Total number of messages received per second since the last invocation of this method")
    public long getReceivedMessagesRate() {
        long rate = this._messagesInRate.getRate();
        this._messagesInRate.reset();
        return rate;
    }

    @ManagedAttribute(value="The total number of messages sent")
    public long getSentMessages() {
        return this._messagesOut.sum();
    }

    @ManagedAttribute(value="Total number of messages sent per second since the last invocation of this method")
    public long getSentMessagesRate() {
        long rate = this._messagesOutRate.getRate();
        this._messagesOutRate.reset();
        return rate;
    }

    @Override
    public String dump() {
        return Dumpable.dump(this);
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
        Dumpable.dumpObjects(out, indent, this, String.format("connections=%s", this._connections), String.format("durations=%s", this._connectionsDuration), String.format("bytes in/out=%s/%s", this.getReceivedBytes(), this.getSentBytes()), String.format("messages in/out=%s/%s", this.getReceivedMessages(), this.getSentMessages()));
    }

    @Override
    public String toString() {
        return String.format("%s@%x", this.getClass().getSimpleName(), this.hashCode());
    }
}

