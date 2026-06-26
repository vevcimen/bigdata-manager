/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.thread;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jetty.util.AtomicBiInteger;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.eclipse.jetty.util.annotation.Name;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.component.DumpableCollection;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.PrivilegedThreadFactory;
import org.eclipse.jetty.util.thread.ReservedThreadExecutor;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.util.thread.ThreadPoolBudget;
import org.eclipse.jetty.util.thread.TryExecutor;

@ManagedObject(value="A thread pool")
public class QueuedThreadPool
extends ContainerLifeCycle
implements ThreadFactory,
ThreadPool.SizedThreadPool,
Dumpable,
TryExecutor {
    private static final Logger LOG = Log.getLogger(QueuedThreadPool.class);
    private static final Runnable NOOP = () -> {};
    private final AtomicBiInteger _counts = new AtomicBiInteger(Integer.MIN_VALUE, 0);
    private final AtomicLong _lastShrink = new AtomicLong();
    private final Set<Thread> _threads = ConcurrentHashMap.newKeySet();
    private final Object _joinLock = new Object();
    private final BlockingQueue<Runnable> _jobs;
    private final ThreadGroup _threadGroup;
    private final ThreadFactory _threadFactory;
    private String _name = "qtp" + this.hashCode();
    private int _idleTimeout;
    private int _maxThreads;
    private int _minThreads;
    private int _reservedThreads = -1;
    private TryExecutor _tryExecutor = TryExecutor.NO_TRY;
    private int _priority = 5;
    private boolean _daemon = false;
    private boolean _detailedDump = false;
    private int _lowThreadsThreshold = 1;
    private ThreadPoolBudget _budget;
    private final Runnable _runnable = new Runner();

    public QueuedThreadPool() {
        this(200);
    }

    public QueuedThreadPool(@Name(value="maxThreads") int maxThreads) {
        this(maxThreads, Math.min(8, maxThreads));
    }

    public QueuedThreadPool(@Name(value="maxThreads") int maxThreads, @Name(value="minThreads") int minThreads) {
        this(maxThreads, minThreads, 60000);
    }

    public QueuedThreadPool(@Name(value="maxThreads") int maxThreads, @Name(value="minThreads") int minThreads, @Name(value="queue") BlockingQueue<Runnable> queue) {
        this(maxThreads, minThreads, 60000, -1, queue, null);
    }

    public QueuedThreadPool(@Name(value="maxThreads") int maxThreads, @Name(value="minThreads") int minThreads, @Name(value="idleTimeout") int idleTimeout) {
        this(maxThreads, minThreads, idleTimeout, null);
    }

    public QueuedThreadPool(@Name(value="maxThreads") int maxThreads, @Name(value="minThreads") int minThreads, @Name(value="idleTimeout") int idleTimeout, @Name(value="queue") BlockingQueue<Runnable> queue) {
        this(maxThreads, minThreads, idleTimeout, queue, null);
    }

    public QueuedThreadPool(@Name(value="maxThreads") int maxThreads, @Name(value="minThreads") int minThreads, @Name(value="idleTimeout") int idleTimeout, @Name(value="queue") BlockingQueue<Runnable> queue, @Name(value="threadGroup") ThreadGroup threadGroup) {
        this(maxThreads, minThreads, idleTimeout, -1, queue, threadGroup);
    }

    public QueuedThreadPool(@Name(value="maxThreads") int maxThreads, @Name(value="minThreads") int minThreads, @Name(value="idleTimeout") int idleTimeout, @Name(value="reservedThreads") int reservedThreads, @Name(value="queue") BlockingQueue<Runnable> queue, @Name(value="threadGroup") ThreadGroup threadGroup) {
        this(maxThreads, minThreads, idleTimeout, reservedThreads, queue, threadGroup, null);
    }

    public QueuedThreadPool(@Name(value="maxThreads") int maxThreads, @Name(value="minThreads") int minThreads, @Name(value="idleTimeout") int idleTimeout, @Name(value="reservedThreads") int reservedThreads, @Name(value="queue") BlockingQueue<Runnable> queue, @Name(value="threadGroup") ThreadGroup threadGroup, @Name(value="threadFactory") ThreadFactory threadFactory) {
        if (maxThreads < minThreads) {
            throw new IllegalArgumentException("max threads (" + maxThreads + ") less than min threads (" + minThreads + ")");
        }
        this.setMinThreads(minThreads);
        this.setMaxThreads(maxThreads);
        this.setIdleTimeout(idleTimeout);
        this.setStopTimeout(5000L);
        this.setReservedThreads(reservedThreads);
        if (queue == null) {
            int capacity = Math.max(this._minThreads, 8) * 1024;
            queue = new BlockingArrayQueue<Runnable>(capacity, capacity);
        }
        this._jobs = queue;
        this._threadGroup = threadGroup;
        this.setThreadPoolBudget(new ThreadPoolBudget(this));
        this._threadFactory = threadFactory == null ? this : threadFactory;
    }

    @Override
    public ThreadPoolBudget getThreadPoolBudget() {
        return this._budget;
    }

    public void setThreadPoolBudget(ThreadPoolBudget budget) {
        if (budget != null && budget.getSizedThreadPool() != this) {
            throw new IllegalArgumentException();
        }
        this.updateBean(this._budget, budget);
        this._budget = budget;
    }

    @Override
    protected void doStart() throws Exception {
        if (this._reservedThreads == 0) {
            this._tryExecutor = NO_TRY;
        } else {
            ReservedThreadExecutor reserved = new ReservedThreadExecutor(this, this._reservedThreads);
            reserved.setIdleTimeout(this._idleTimeout, TimeUnit.MILLISECONDS);
            this._tryExecutor = reserved;
        }
        this.addBean(this._tryExecutor);
        this._lastShrink.set(System.nanoTime());
        super.doStart();
        this._counts.set(0, 0);
        this.ensureThreads();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void doStop() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Stopping {}", this);
        }
        super.doStop();
        this.removeBean(this._tryExecutor);
        this._tryExecutor = TryExecutor.NO_TRY;
        int threads = this._counts.getAndSetHi(Integer.MIN_VALUE);
        long timeout = this.getStopTimeout();
        BlockingQueue<Runnable> jobs = this.getQueue();
        if (timeout > 0L) {
            for (int i = 0; i < threads; ++i) {
                jobs.offer(NOOP);
            }
            this.joinThreads(System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout) / 2L);
            for (Thread thread : this._threads) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Interrupting {}", thread);
                }
                thread.interrupt();
            }
            this.joinThreads(System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout) / 2L);
            Thread.yield();
            if (LOG.isDebugEnabled()) {
                for (Thread unstopped : this._threads) {
                    StringBuilder dmp = new StringBuilder();
                    for (StackTraceElement element : unstopped.getStackTrace()) {
                        dmp.append(System.lineSeparator()).append("\tat ").append(element);
                    }
                    LOG.warn("Couldn't stop {}{}", unstopped, dmp.toString());
                }
            } else {
                for (Thread unstopped : this._threads) {
                    LOG.warn("{} Couldn't stop {}", this, unstopped);
                }
            }
        }
        while (!this._jobs.isEmpty()) {
            Runnable job = (Runnable)this._jobs.poll();
            if (job instanceof Closeable) {
                try {
                    ((Closeable)((Object)job)).close();
                }
                catch (Throwable t) {
                    LOG.warn(t);
                }
                continue;
            }
            if (job == NOOP) continue;
            LOG.warn("Stopped without executing or closing {}", job);
        }
        if (this._budget != null) {
            this._budget.reset();
        }
        Object object = this._joinLock;
        synchronized (object) {
            this._joinLock.notifyAll();
        }
    }

    private void joinThreads(long stopByNanos) throws InterruptedException {
        for (Thread thread : this._threads) {
            long canWait = TimeUnit.NANOSECONDS.toMillis(stopByNanos - System.nanoTime());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Waiting for {} for {}", thread, canWait);
            }
            if (canWait <= 0L) continue;
            thread.join(canWait);
        }
    }

    @ManagedAttribute(value="maximum time a thread may be idle in ms")
    public int getIdleTimeout() {
        return this._idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this._idleTimeout = idleTimeout;
        ReservedThreadExecutor reserved = this.getBean(ReservedThreadExecutor.class);
        if (reserved != null) {
            reserved.setIdleTimeout(idleTimeout, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    @ManagedAttribute(value="maximum number of threads in the pool")
    public int getMaxThreads() {
        return this._maxThreads;
    }

    @Override
    public void setMaxThreads(int maxThreads) {
        if (this._budget != null) {
            this._budget.check(maxThreads);
        }
        this._maxThreads = maxThreads;
        if (this._minThreads > this._maxThreads) {
            this._minThreads = this._maxThreads;
        }
    }

    @Override
    @ManagedAttribute(value="minimum number of threads in the pool")
    public int getMinThreads() {
        return this._minThreads;
    }

    @Override
    public void setMinThreads(int minThreads) {
        this._minThreads = minThreads;
        if (this._minThreads > this._maxThreads) {
            this._maxThreads = this._minThreads;
        }
        if (this.isStarted()) {
            this.ensureThreads();
        }
    }

    @ManagedAttribute(value="number of configured reserved threads or -1 for heuristic")
    public int getReservedThreads() {
        return this._reservedThreads;
    }

    public void setReservedThreads(int reservedThreads) {
        if (this.isRunning()) {
            throw new IllegalStateException(this.getState());
        }
        this._reservedThreads = reservedThreads;
    }

    @ManagedAttribute(value="name of the thread pool")
    public String getName() {
        return this._name;
    }

    public void setName(String name) {
        if (this.isRunning()) {
            throw new IllegalStateException(this.getState());
        }
        this._name = name;
    }

    @ManagedAttribute(value="priority of threads in the pool")
    public int getThreadsPriority() {
        return this._priority;
    }

    public void setThreadsPriority(int priority) {
        this._priority = priority;
    }

    @ManagedAttribute(value="thread pool uses daemon threads")
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

    @ManagedAttribute(value="threshold at which the pool is low on threads")
    public int getLowThreadsThreshold() {
        return this._lowThreadsThreshold;
    }

    public void setLowThreadsThreshold(int lowThreadsThreshold) {
        this._lowThreadsThreshold = lowThreadsThreshold;
    }

    @ManagedAttribute(value="size of the job queue")
    public int getQueueSize() {
        int idle = this._counts.getLo();
        return Math.max(0, -idle);
    }

    @ManagedAttribute(value="maximum number (capacity) of reserved threads")
    public int getMaxReservedThreads() {
        TryExecutor tryExecutor = this._tryExecutor;
        if (tryExecutor instanceof ReservedThreadExecutor) {
            ReservedThreadExecutor reservedThreadExecutor = (ReservedThreadExecutor)tryExecutor;
            return reservedThreadExecutor.getCapacity();
        }
        return 0;
    }

    @ManagedAttribute(value="number of available reserved threads")
    public int getAvailableReservedThreads() {
        TryExecutor tryExecutor = this._tryExecutor;
        if (tryExecutor instanceof ReservedThreadExecutor) {
            ReservedThreadExecutor reservedThreadExecutor = (ReservedThreadExecutor)tryExecutor;
            return reservedThreadExecutor.getAvailable();
        }
        return 0;
    }

    @Override
    @ManagedAttribute(value="number of threads in the pool")
    public int getThreads() {
        int threads = this._counts.getHi();
        return Math.max(0, threads);
    }

    @ManagedAttribute(value="number of threads ready to execute transient jobs")
    public int getReadyThreads() {
        return this.getIdleThreads() + this.getAvailableReservedThreads();
    }

    @ManagedAttribute(value="number of threads used by internal components")
    public int getLeasedThreads() {
        return this.getMaxLeasedThreads() - this.getMaxReservedThreads();
    }

    @ManagedAttribute(value="maximum number of threads leased to internal components")
    public int getMaxLeasedThreads() {
        ThreadPoolBudget budget = this._budget;
        return budget == null ? 0 : budget.getLeasedThreads();
    }

    @Override
    @ManagedAttribute(value="number of idle threads but not reserved")
    public int getIdleThreads() {
        int idle = this._counts.getLo();
        return Math.max(0, idle);
    }

    @ManagedAttribute(value="number of threads executing internal and transient jobs")
    public int getBusyThreads() {
        return this.getThreads() - this.getReadyThreads();
    }

    @ManagedAttribute(value="number of threads executing transient jobs")
    public int getUtilizedThreads() {
        return this.getThreads() - this.getLeasedThreads() - this.getReadyThreads();
    }

    @ManagedAttribute(value="maximum number of threads available to run transient jobs")
    public int getMaxAvailableThreads() {
        return this.getMaxThreads() - this.getLeasedThreads();
    }

    @ManagedAttribute(value="utilization rate of threads executing transient jobs")
    public double getUtilizationRate() {
        return (double)this.getUtilizedThreads() / (double)this.getMaxAvailableThreads();
    }

    @Override
    @ManagedAttribute(value="thread pool is low on threads", readonly=true)
    public boolean isLowOnThreads() {
        return this.getMaxThreads() - this.getThreads() + this.getReadyThreads() - this.getQueueSize() <= this.getLowThreadsThreshold();
    }

    @Override
    public void execute(Runnable job) {
        int idle;
        int startThread;
        int threads;
        long counts;
        do {
            if ((threads = AtomicBiInteger.getHi(counts = this._counts.get())) != Integer.MIN_VALUE) continue;
            throw new RejectedExecutionException(job.toString());
        } while (!this._counts.compareAndSet(counts, threads + (startThread = (idle = AtomicBiInteger.getLo(counts)) <= 0 && threads < this._maxThreads ? 1 : 0), idle + startThread - 1));
        if (!this._jobs.offer(job)) {
            if (this.addCounts(-startThread, 1 - startThread)) {
                LOG.warn("{} rejected {}", this, job);
            }
            throw new RejectedExecutionException(job.toString());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("queue {} startThread={}", job, startThread);
        }
        while (startThread-- > 0) {
            this.startThread();
        }
    }

    @Override
    public boolean tryExecute(Runnable task) {
        TryExecutor tryExecutor = this._tryExecutor;
        return tryExecutor != null && tryExecutor.tryExecute(task);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void join() throws InterruptedException {
        Object object = this._joinLock;
        synchronized (object) {
            while (this.isRunning()) {
                this._joinLock.wait();
            }
        }
        while (this.isStopping()) {
            Thread.sleep(1L);
        }
    }

    private void ensureThreads() {
        long counts;
        int threads;
        while ((threads = AtomicBiInteger.getHi(counts = this._counts.get())) != Integer.MIN_VALUE) {
            int idle = AtomicBiInteger.getLo(counts);
            if (threads >= this._minThreads && (idle >= 0 || threads >= this._maxThreads)) break;
            if (!this._counts.compareAndSet(counts, threads + 1, idle + 1)) continue;
            this.startThread();
        }
    }

    protected void startThread() {
        boolean started = false;
        try {
            Thread thread = this._threadFactory.newThread(this._runnable);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting {}", thread);
            }
            this._threads.add(thread);
            this._lastShrink.set(System.nanoTime());
            thread.start();
            started = true;
        }
        finally {
            if (!started) {
                this.addCounts(-1, -1);
            }
        }
    }

    private boolean addCounts(int deltaThreads, int deltaIdle) {
        int idle;
        int threads;
        long update;
        long encoded;
        do {
            encoded = this._counts.get();
            threads = AtomicBiInteger.getHi(encoded);
            idle = AtomicBiInteger.getLo(encoded);
            if (threads != Integer.MIN_VALUE) continue;
            return false;
        } while (!this._counts.compareAndSet(encoded, update = AtomicBiInteger.encode(threads + deltaThreads, idle + deltaIdle)));
        return true;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return PrivilegedThreadFactory.newThread(() -> {
            Thread thread = new Thread(this._threadGroup, runnable);
            thread.setDaemon(this.isDaemon());
            thread.setPriority(this.getThreadsPriority());
            thread.setName(this._name + "-" + thread.getId());
            thread.setContextClassLoader(this.getClass().getClassLoader());
            return thread;
        });
    }

    protected void removeThread(Thread thread) {
        this._threads.remove(thread);
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
        ArrayList<Object> threads = new ArrayList<Object>(this.getMaxThreads());
        for (Thread thread : this._threads) {
            StackTraceElement[] trace = thread.getStackTrace();
            String stackTag = this.getCompressedStackTag(trace);
            String baseThreadInfo = String.format("%s %s tid=%d prio=%d", new Object[]{thread.getName(), thread.getState(), thread.getId(), thread.getPriority()});
            if (!StringUtil.isBlank(stackTag)) {
                threads.add(baseThreadInfo + " " + stackTag);
                continue;
            }
            if (this.isDetailedDump()) {
                threads.add((o, i) -> Dumpable.dumpObjects(o, i, baseThreadInfo, trace));
                continue;
            }
            threads.add(baseThreadInfo + " @ " + (trace.length > 0 ? trace[0].toString() : "???"));
        }
        DumpableCollection threadsDump = new DumpableCollection("threads", threads);
        if (this.isDetailedDump()) {
            this.dumpObjects(out, indent, threadsDump, new DumpableCollection("jobs", new ArrayList<Runnable>(this.getQueue())));
        } else {
            this.dumpObjects(out, indent, threadsDump);
        }
    }

    private String getCompressedStackTag(StackTraceElement[] trace) {
        for (StackTraceElement t : trace) {
            if ("idleJobPoll".equals(t.getMethodName()) && t.getClassName().equals(Runner.class.getName())) {
                return "IDLE";
            }
            if ("reservedWait".equals(t.getMethodName()) && t.getClassName().endsWith("ReservedThread")) {
                return "RESERVED";
            }
            if ("select".equals(t.getMethodName()) && t.getClassName().endsWith("SelectorProducer")) {
                return "SELECTING";
            }
            if (!"accept".equals(t.getMethodName()) || !t.getClassName().contains("ServerConnector")) continue;
            return "ACCEPTING";
        }
        return "";
    }

    protected void runJob(Runnable job) {
        job.run();
    }

    protected BlockingQueue<Runnable> getQueue() {
        return this._jobs;
    }

    @Deprecated
    public void setQueue(BlockingQueue<Runnable> queue) {
        throw new UnsupportedOperationException("Use constructor injection");
    }

    @ManagedOperation(value="interrupts a pool thread")
    public boolean interruptThread(@Name(value="id") long id) {
        for (Thread thread : this._threads) {
            if (thread.getId() != id) continue;
            thread.interrupt();
            return true;
        }
        return false;
    }

    @ManagedOperation(value="dumps a pool thread stack")
    public String dumpThread(@Name(value="id") long id) {
        for (Thread thread : this._threads) {
            if (thread.getId() != id) continue;
            StringBuilder buf = new StringBuilder();
            buf.append(thread.getId()).append(" ").append(thread.getName()).append(" ");
            buf.append((Object)thread.getState()).append(":").append(System.lineSeparator());
            for (StackTraceElement element : thread.getStackTrace()) {
                buf.append("  at ").append(element.toString()).append(System.lineSeparator());
            }
            return buf.toString();
        }
        return null;
    }

    @Override
    public String toString() {
        long count = this._counts.get();
        int threads = Math.max(0, AtomicBiInteger.getHi(count));
        int idle = Math.max(0, AtomicBiInteger.getLo(count));
        int queue = this.getQueueSize();
        return String.format("%s[%s]@%x{%s,%d<=%d<=%d,i=%d,r=%d,q=%d}[%s]", this.getClass().getSimpleName(), this._name, this.hashCode(), this.getState(), this.getMinThreads(), threads, this.getMaxThreads(), idle, this.getReservedThreads(), queue, this._tryExecutor);
    }

    static /* synthetic */ Logger access$200() {
        return LOG;
    }

    static /* synthetic */ boolean access$300(QueuedThreadPool x0, int x1, int x2) {
        return x0.addCounts(x1, x2);
    }

    static /* synthetic */ AtomicBiInteger access$400(QueuedThreadPool x0) {
        return x0._counts;
    }

    static /* synthetic */ int access$500(QueuedThreadPool x0) {
        return x0._minThreads;
    }

    static /* synthetic */ AtomicLong access$600(QueuedThreadPool x0) {
        return x0._lastShrink;
    }

    static /* synthetic */ void access$700(QueuedThreadPool x0) {
        x0.ensureThreads();
    }

    private class Runner
    implements Runnable {
        private Runner() {
        }

        private Runnable idleJobPoll(long idleTimeout) throws InterruptedException {
            if (idleTimeout <= 0L) {
                return (Runnable)QueuedThreadPool.this._jobs.take();
            }
            return (Runnable)QueuedThreadPool.this._jobs.poll(idleTimeout, TimeUnit.MILLISECONDS);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Unable to fully structure code
         */
        @Override
        public void run() {
            if (QueuedThreadPool.access$200().isDebugEnabled()) {
                QueuedThreadPool.access$200().debug("Runner started for {}", new Object[]{QueuedThreadPool.this});
            }
            idle = true;
            try {
                job = null;
                while (true) {
                    if (job != null) {
                        if (!QueuedThreadPool.access$300(QueuedThreadPool.this, 0, 1)) ** break;
                        idle = true;
                    } else if (QueuedThreadPool.access$400(QueuedThreadPool.this).getHi() == -2147483648) ** break;
                    try {
                        job = (Runnable)QueuedThreadPool.access$100(QueuedThreadPool.this).poll();
                        if (job == null) {
                            idleTimeout = QueuedThreadPool.this.getIdleTimeout();
                            if (idleTimeout > 0L && QueuedThreadPool.this.getThreads() > QueuedThreadPool.access$500(QueuedThreadPool.this)) {
                                last = QueuedThreadPool.access$600(QueuedThreadPool.this).get();
                                now = System.nanoTime();
                                if (now - last > TimeUnit.MILLISECONDS.toNanos(idleTimeout) && QueuedThreadPool.access$600(QueuedThreadPool.this).compareAndSet(last, now)) {
                                    if (!QueuedThreadPool.access$200().isDebugEnabled()) ** break;
                                    QueuedThreadPool.access$200().debug("shrinking {}", new Object[]{QueuedThreadPool.this});
                                    ** break;
                                }
                            }
                            if ((job = this.idleJobPoll(idleTimeout)) == null) continue;
                        }
                        idle = false;
                        if (QueuedThreadPool.access$200().isDebugEnabled()) {
                            QueuedThreadPool.access$200().debug("run {} in {}", new Object[]{job, QueuedThreadPool.this});
                        }
                        QueuedThreadPool.this.runJob(job);
                        if (!QueuedThreadPool.access$200().isDebugEnabled()) continue;
                        QueuedThreadPool.access$200().debug("ran {} in {}", new Object[]{job, QueuedThreadPool.this});
                        continue;
                    }
                    catch (InterruptedException e) {
                        if (QueuedThreadPool.access$200().isDebugEnabled()) {
                            QueuedThreadPool.access$200().debug("interrupted {} in {}", new Object[]{job, QueuedThreadPool.this});
                        }
                        QueuedThreadPool.access$200().ignore(e);
                        continue;
                    }
                    catch (Throwable e) {
                        QueuedThreadPool.access$200().warn(e);
                        continue;
                    }
                    finally {
                        Thread.interrupted();
                        continue;
                    }
                    break;
                }
            }
            catch (Throwable var10_9) {
                thread = Thread.currentThread();
                QueuedThreadPool.this.removeThread(thread);
                QueuedThreadPool.access$300(QueuedThreadPool.this, -1, idle != false ? -1 : 0);
                if (QueuedThreadPool.access$200().isDebugEnabled()) {
                    QueuedThreadPool.access$200().debug("{} exited for {}", new Object[]{thread, QueuedThreadPool.this});
                }
                QueuedThreadPool.access$700(QueuedThreadPool.this);
                throw var10_9;
            }
lbl52:
            // 4 sources

            thread = Thread.currentThread();
            QueuedThreadPool.this.removeThread(thread);
            QueuedThreadPool.access$300(QueuedThreadPool.this, -1, idle != false ? -1 : 0);
            if (QueuedThreadPool.access$200().isDebugEnabled()) {
                QueuedThreadPool.access$200().debug("{} exited for {}", new Object[]{thread, QueuedThreadPool.this});
            }
            QueuedThreadPool.access$700(QueuedThreadPool.this);
        }
    }
}

