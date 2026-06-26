/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.eclipse.jetty.util.ProcessorUtils;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.component.DumpableCollection;
import org.eclipse.jetty.util.thread.ReservedThreadExecutor;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.util.thread.ThreadPoolBudget;
import org.eclipse.jetty.util.thread.TryExecutor;

@ManagedObject(value="A thread pool")
public class ExecutorThreadPool
extends ContainerLifeCycle
implements ThreadPool.SizedThreadPool,
TryExecutor {
    private final ThreadPoolExecutor _executor;
    private final ThreadPoolBudget _budget;
    private final ThreadGroup _group;
    private String _name = "etp" + this.hashCode();
    private int _minThreads;
    private int _reservedThreads = -1;
    private TryExecutor _tryExecutor = TryExecutor.NO_TRY;
    private int _priority = 5;
    private boolean _daemon;
    private boolean _detailedDump;

    public ExecutorThreadPool() {
        this(200, 8);
    }

    public ExecutorThreadPool(int maxThreads) {
        this(maxThreads, Math.min(8, maxThreads));
    }

    public ExecutorThreadPool(int maxThreads, int minThreads) {
        this(maxThreads, minThreads, new LinkedBlockingQueue<Runnable>());
    }

    public ExecutorThreadPool(int maxThreads, int minThreads, BlockingQueue<Runnable> queue) {
        this(new ThreadPoolExecutor(maxThreads, maxThreads, 60L, TimeUnit.SECONDS, queue), minThreads, -1, null);
    }

    public ExecutorThreadPool(ThreadPoolExecutor executor) {
        this(executor, -1);
    }

    public ExecutorThreadPool(ThreadPoolExecutor executor, int reservedThreads) {
        this(executor, reservedThreads, null);
    }

    public ExecutorThreadPool(ThreadPoolExecutor executor, int reservedThreads, ThreadGroup group) {
        this(executor, Math.min(ProcessorUtils.availableProcessors(), executor.getCorePoolSize()), reservedThreads, group);
    }

    private ExecutorThreadPool(ThreadPoolExecutor executor, int minThreads, int reservedThreads, ThreadGroup group) {
        int maxThreads = executor.getMaximumPoolSize();
        if (maxThreads < minThreads) {
            executor.shutdownNow();
            throw new IllegalArgumentException("max threads (" + maxThreads + ") cannot be less than min threads (" + minThreads + ")");
        }
        this._executor = executor;
        this._executor.setThreadFactory(this::newThread);
        this._group = group;
        this._minThreads = minThreads;
        this._reservedThreads = reservedThreads;
        this._budget = new ThreadPoolBudget(this);
    }

    @ManagedAttribute(value="name of this thread pool")
    public String getName() {
        return this._name;
    }

    public void setName(String name) {
        if (this.isRunning()) {
            throw new IllegalStateException(this.getState());
        }
        this._name = name;
    }

    @Override
    @ManagedAttribute(value="minimum number of threads in the pool")
    public int getMinThreads() {
        return this._minThreads;
    }

    @Override
    public void setMinThreads(int threads) {
        this._minThreads = threads;
    }

    @Override
    @ManagedAttribute(value="maximum number of threads in the pool")
    public int getMaxThreads() {
        return this._executor.getMaximumPoolSize();
    }

    @Override
    public void setMaxThreads(int threads) {
        if (this._budget != null) {
            this._budget.check(threads);
        }
        this._executor.setCorePoolSize(threads);
        this._executor.setMaximumPoolSize(threads);
    }

    @ManagedAttribute(value="maximum time a thread may be idle in ms")
    public int getIdleTimeout() {
        return (int)this._executor.getKeepAliveTime(TimeUnit.MILLISECONDS);
    }

    public void setIdleTimeout(int idleTimeout) {
        this._executor.setKeepAliveTime(idleTimeout, TimeUnit.MILLISECONDS);
    }

    @ManagedAttribute(value="the number of reserved threads in the pool")
    public int getReservedThreads() {
        if (this.isStarted()) {
            return this.getBean(ReservedThreadExecutor.class).getCapacity();
        }
        return this._reservedThreads;
    }

    public void setReservedThreads(int reservedThreads) {
        if (this.isRunning()) {
            throw new IllegalStateException(this.getState());
        }
        this._reservedThreads = reservedThreads;
    }

    public void setThreadsPriority(int priority) {
        this._priority = priority;
    }

    public int getThreadsPriority() {
        return this._priority;
    }

    @ManagedAttribute(value="whether this thread pool uses daemon threads")
    public boolean isDaemon() {
        return this._daemon;
    }

    public void setDaemon(boolean daemon) {
        this._daemon = daemon;
    }

    @ManagedAttribute(value="reports additional details in the dump")
    public boolean isDetailedDump() {
        return this._detailedDump;
    }

    public void setDetailedDump(boolean detailedDump) {
        this._detailedDump = detailedDump;
    }

    @Override
    @ManagedAttribute(value="number of threads in the pool")
    public int getThreads() {
        return this._executor.getPoolSize();
    }

    @Override
    @ManagedAttribute(value="number of idle threads in the pool")
    public int getIdleThreads() {
        return this._executor.getPoolSize() - this._executor.getActiveCount();
    }

    @Override
    public void execute(Runnable command) {
        this._executor.execute(command);
    }

    @Override
    public boolean tryExecute(Runnable task) {
        TryExecutor tryExecutor = this._tryExecutor;
        return tryExecutor != null && tryExecutor.tryExecute(task);
    }

    @Override
    @ManagedAttribute(value="thread pool is low on threads", readonly=true)
    public boolean isLowOnThreads() {
        return this.getThreads() == this.getMaxThreads() && this._executor.getQueue().size() >= this.getIdleThreads();
    }

    @Override
    protected void doStart() throws Exception {
        if (this._executor.isShutdown()) {
            throw new IllegalStateException("This thread pool is not restartable");
        }
        for (int i = 0; i < this._minThreads; ++i) {
            this._executor.prestartCoreThread();
        }
        this._tryExecutor = new ReservedThreadExecutor(this, this._reservedThreads);
        this.addBean(this._tryExecutor);
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        this.removeBean(this._tryExecutor);
        this._tryExecutor = TryExecutor.NO_TRY;
        this._executor.shutdownNow();
        this._budget.reset();
    }

    @Override
    public void join() throws InterruptedException {
        this._executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    @Override
    public ThreadPoolBudget getThreadPoolBudget() {
        return this._budget;
    }

    protected Thread newThread(Runnable job) {
        Thread thread = new Thread(this._group, job);
        thread.setDaemon(this.isDaemon());
        thread.setPriority(this.getThreadsPriority());
        thread.setName(this.getName() + "-" + thread.getId());
        return thread;
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
        String prefix = this.getName() + "-";
        List threads = Thread.getAllStackTraces().entrySet().stream().filter(entry -> ((Thread)entry.getKey()).getName().startsWith(prefix)).map(entry -> {
            final Thread thread = (Thread)entry.getKey();
            final StackTraceElement[] frames = (StackTraceElement[])entry.getValue();
            String knownMethod = null;
            for (StackTraceElement frame : frames) {
                if ("getTask".equals(frame.getMethodName()) && frame.getClassName().endsWith("ThreadPoolExecutor")) {
                    knownMethod = "IDLE ";
                    break;
                }
                if ("reservedWait".equals(frame.getMethodName()) && frame.getClassName().endsWith("ReservedThread")) {
                    knownMethod = "RESERVED ";
                    break;
                }
                if ("select".equals(frame.getMethodName()) && frame.getClassName().endsWith("SelectorProducer")) {
                    knownMethod = "SELECTING ";
                    break;
                }
                if (!"accept".equals(frame.getMethodName()) || !frame.getClassName().contains("ServerConnector")) continue;
                knownMethod = "ACCEPTING ";
                break;
            }
            final String known = knownMethod == null ? "" : knownMethod;
            return new Dumpable(){

                @Override
                public void dump(Appendable out, String indent) throws IOException {
                    StringBuilder b = new StringBuilder();
                    b.append(thread.getId()).append(" ").append(thread.getName()).append(" p=").append(thread.getPriority()).append(" ").append(known).append(thread.getState().toString());
                    if (ExecutorThreadPool.this.isDetailedDump()) {
                        if (known.isEmpty()) {
                            Dumpable.dumpObjects(out, indent, b.toString(), frames);
                        } else {
                            Dumpable.dumpObject(out, b.toString());
                        }
                    } else {
                        b.append(" @ ").append(frames.length > 0 ? String.valueOf(frames[0]) : "<no_stack_frames>");
                        Dumpable.dumpObject(out, b.toString());
                    }
                }

                @Override
                public String dump() {
                    return null;
                }
            };
        }).collect(Collectors.toList());
        List jobs = Collections.emptyList();
        if (this.isDetailedDump()) {
            jobs = new ArrayList<Runnable>(this._executor.getQueue());
        }
        this.dumpObjects(out, indent, threads, new DumpableCollection("jobs", jobs));
    }

    @Override
    public String toString() {
        return String.format("%s[%s]@%x{%s,%d<=%d<=%d,i=%d,q=%d,%s}", this.getClass().getSimpleName(), this.getName(), this.hashCode(), this.getState(), this.getMinThreads(), this.getThreads(), this.getMaxThreads(), this.getIdleThreads(), this._executor.getQueue().size(), this._tryExecutor);
    }
}

