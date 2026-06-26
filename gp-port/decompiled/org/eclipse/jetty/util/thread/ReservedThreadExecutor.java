/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.thread;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.jetty.util.ProcessorUtils;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.util.thread.ThreadPoolBudget;
import org.eclipse.jetty.util.thread.TryExecutor;

@ManagedObject(value="A pool for reserved threads")
public class ReservedThreadExecutor
extends AbstractLifeCycle
implements TryExecutor {
    private static final Logger LOG = Log.getLogger(ReservedThreadExecutor.class);
    private static final Runnable STOP = new Runnable(){

        @Override
        public void run() {
        }

        public String toString() {
            return "STOP!";
        }
    };
    private final Executor _executor;
    private final int _capacity;
    private final ConcurrentLinkedDeque<ReservedThread> _stack;
    private final AtomicInteger _size = new AtomicInteger();
    private final AtomicInteger _pending = new AtomicInteger();
    private ThreadPoolBudget.Lease _lease;
    private long _idleTime = 1L;
    private TimeUnit _idleTimeUnit = TimeUnit.MINUTES;

    public ReservedThreadExecutor(Executor executor, int capacity) {
        this._executor = executor;
        this._capacity = ReservedThreadExecutor.reservedThreads(executor, capacity);
        this._stack = new ConcurrentLinkedDeque();
        if (LOG.isDebugEnabled()) {
            LOG.debug("{}", this);
        }
    }

    private static int reservedThreads(Executor executor, int capacity) {
        if (capacity >= 0) {
            return capacity;
        }
        int cpus = ProcessorUtils.availableProcessors();
        if (executor instanceof ThreadPool.SizedThreadPool) {
            int threads = ((ThreadPool.SizedThreadPool)executor).getMaxThreads();
            return Math.max(1, Math.min(cpus, threads / 10));
        }
        return cpus;
    }

    public Executor getExecutor() {
        return this._executor;
    }

    @ManagedAttribute(value="max number of reserved threads", readonly=true)
    public int getCapacity() {
        return this._capacity;
    }

    @ManagedAttribute(value="available reserved threads", readonly=true)
    public int getAvailable() {
        return this._stack.size();
    }

    @ManagedAttribute(value="pending reserved threads", readonly=true)
    public int getPending() {
        return this._pending.get();
    }

    @ManagedAttribute(value="idletimeout in MS", readonly=true)
    public long getIdleTimeoutMs() {
        if (this._idleTimeUnit == null) {
            return 0L;
        }
        return this._idleTimeUnit.toMillis(this._idleTime);
    }

    public void setIdleTimeout(long idleTime, TimeUnit idleTimeUnit) {
        if (this.isRunning()) {
            throw new IllegalStateException();
        }
        this._idleTime = idleTime;
        this._idleTimeUnit = idleTimeUnit;
    }

    @Override
    public void doStart() throws Exception {
        this._lease = ThreadPoolBudget.leaseFrom(this.getExecutor(), this, this._capacity);
        this._size.set(0);
        super.doStart();
    }

    @Override
    public void doStop() throws Exception {
        int size;
        if (this._lease != null) {
            this._lease.close();
        }
        super.doStop();
        while ((size = this._size.get()) != 0 || !this._size.compareAndSet(size, -1)) {
            ReservedThread thread = this._stack.pollFirst();
            if (thread == null) {
                Thread.yield();
                continue;
            }
            this._size.decrementAndGet();
            thread.stop();
        }
    }

    @Override
    public void execute(Runnable task) throws RejectedExecutionException {
        this._executor.execute(task);
    }

    @Override
    public boolean tryExecute(Runnable task) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} tryExecute {}", this, task);
        }
        if (task == null) {
            return false;
        }
        ReservedThread thread = this._stack.pollFirst();
        if (thread == null) {
            if (task != STOP) {
                this.startReservedThread();
            }
            return false;
        }
        int size = this._size.decrementAndGet();
        if (!thread.offer(task)) {
            return false;
        }
        if (size == 0 && task != STOP) {
            this.startReservedThread();
        }
        return true;
    }

    private void startReservedThread() {
        try {
            int pending;
            do {
                int size;
                if ((pending = this._pending.get()) + (size = this._size.get()) < this._capacity) continue;
                return;
            } while (!this._pending.compareAndSet(pending, pending + 1));
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} startReservedThread p={}", this, pending + 1);
            }
            this._executor.execute(new ReservedThread());
            return;
        }
        catch (RejectedExecutionException e) {
            LOG.ignore(e);
            return;
        }
    }

    @Override
    public String toString() {
        return String.format("%s@%x{s=%d/%d,p=%d}", this.getClass().getSimpleName(), this.hashCode(), this._size.get(), this._capacity, this._pending.get());
    }

    private class ReservedThread
    implements Runnable {
        private final SynchronousQueue<Runnable> _task = new SynchronousQueue();
        private boolean _starting = true;

        private ReservedThread() {
        }

        public boolean offer(Runnable task) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} offer {}", this, task);
            }
            try {
                this._task.put(task);
                return true;
            }
            catch (Throwable e) {
                LOG.ignore(e);
                ReservedThreadExecutor.this._size.getAndIncrement();
                ReservedThreadExecutor.this._stack.offerFirst(this);
                return false;
            }
        }

        public void stop() {
            this.offer(STOP);
        }

        private Runnable reservedWait() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} waiting", this);
            }
            while (true) {
                try {
                    do {
                        Runnable task;
                        Runnable runnable = task = ReservedThreadExecutor.this._idleTime <= 0L ? this._task.take() : this._task.poll(ReservedThreadExecutor.this._idleTime, ReservedThreadExecutor.this._idleTimeUnit);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("{} task={}", this, task);
                        }
                        if (task == null) continue;
                        return task;
                    } while (!ReservedThreadExecutor.this._stack.remove(this));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{} IDLE", this);
                    }
                    ReservedThreadExecutor.this._size.decrementAndGet();
                    return STOP;
                }
                catch (InterruptedException e) {
                    LOG.ignore(e);
                    continue;
                }
                break;
            }
        }

        @Override
        public void run() {
            while (ReservedThreadExecutor.this.isRunning()) {
                int size = ReservedThreadExecutor.this._size.get();
                if (size < 0) {
                    return;
                }
                if (size >= ReservedThreadExecutor.this._capacity) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{} size {} > capacity", this, size, ReservedThreadExecutor.this._capacity);
                    }
                    if (this._starting) {
                        ReservedThreadExecutor.this._pending.decrementAndGet();
                    }
                    return;
                }
                if (!ReservedThreadExecutor.this._size.compareAndSet(size, size + 1)) continue;
                if (this._starting) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{} started", this);
                    }
                    ReservedThreadExecutor.this._pending.decrementAndGet();
                    this._starting = false;
                }
                ReservedThreadExecutor.this._stack.offerFirst(this);
                Runnable task = this.reservedWait();
                if (task == STOP) break;
                try {
                    task.run();
                }
                catch (Throwable e) {
                    LOG.warn(e);
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} Exited", this);
            }
        }

        public String toString() {
            return String.format("%s@%x", ReservedThreadExecutor.this, this.hashCode());
        }
    }
}

