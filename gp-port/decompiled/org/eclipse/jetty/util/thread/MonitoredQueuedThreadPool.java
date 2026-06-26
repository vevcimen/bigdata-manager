/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.thread;

import java.util.concurrent.BlockingQueue;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.eclipse.jetty.util.statistic.CounterStatistic;
import org.eclipse.jetty.util.statistic.SampleStatistic;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

@ManagedObject
public class MonitoredQueuedThreadPool
extends QueuedThreadPool {
    private final CounterStatistic queueStats = new CounterStatistic();
    private final SampleStatistic queueLatencyStats = new SampleStatistic();
    private final SampleStatistic taskLatencyStats = new SampleStatistic();
    private final CounterStatistic threadStats = new CounterStatistic();

    public MonitoredQueuedThreadPool() {
        this(256);
    }

    public MonitoredQueuedThreadPool(int maxThreads) {
        this(maxThreads, maxThreads, 86400000, new BlockingArrayQueue<Runnable>(maxThreads, 256));
    }

    public MonitoredQueuedThreadPool(int maxThreads, int minThreads, int idleTimeOut, BlockingQueue<Runnable> queue) {
        super(maxThreads, minThreads, idleTimeOut, queue);
        this.addBean(this.queueStats);
        this.addBean(this.queueLatencyStats);
        this.addBean(this.taskLatencyStats);
        this.addBean(this.threadStats);
    }

    @Override
    public void execute(final Runnable job) {
        this.queueStats.increment();
        final long begin = System.nanoTime();
        super.execute(new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                long queueLatency = System.nanoTime() - begin;
                MonitoredQueuedThreadPool.this.queueStats.decrement();
                MonitoredQueuedThreadPool.this.threadStats.increment();
                MonitoredQueuedThreadPool.this.queueLatencyStats.record(queueLatency);
                long start = System.nanoTime();
                try {
                    job.run();
                }
                finally {
                    long taskLatency = System.nanoTime() - start;
                    MonitoredQueuedThreadPool.this.threadStats.decrement();
                    MonitoredQueuedThreadPool.this.taskLatencyStats.record(taskLatency);
                }
            }

            public String toString() {
                return job.toString();
            }
        });
    }

    @ManagedOperation(value="resets the statistics", impact="ACTION")
    public void reset() {
        this.queueStats.reset();
        this.queueLatencyStats.reset();
        this.taskLatencyStats.reset();
        this.threadStats.reset(0L);
    }

    @ManagedAttribute(value="the number of tasks executed")
    public long getTasks() {
        return this.taskLatencyStats.getCount();
    }

    @ManagedAttribute(value="the maximum number of busy threads")
    public int getMaxBusyThreads() {
        return (int)this.threadStats.getMax();
    }

    @ManagedAttribute(value="the maximum task queue size")
    public int getMaxQueueSize() {
        return (int)this.queueStats.getMax();
    }

    @ManagedAttribute(value="the average time a task remains in the queue, in nanoseconds")
    public long getAverageQueueLatency() {
        return (long)this.queueLatencyStats.getMean();
    }

    @ManagedAttribute(value="the maximum time a task remains in the queue, in nanoseconds")
    public long getMaxQueueLatency() {
        return this.queueLatencyStats.getMax();
    }

    @ManagedAttribute(value="the average task execution time, in nanoseconds")
    public long getAverageTaskLatency() {
        return (long)this.taskLatencyStats.getMean();
    }

    @ManagedAttribute(value="the maximum task execution time, in nanoseconds")
    public long getMaxTaskLatency() {
        return this.taskLatencyStats.getMax();
    }
}

