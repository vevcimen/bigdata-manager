/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.thread;

import java.io.Closeable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.ThreadPool;

@ManagedObject
public class ThreadPoolBudget {
    private static final Logger LOG = Log.getLogger(ThreadPoolBudget.class);
    private static final Lease NOOP_LEASE = new Lease(){

        @Override
        public void close() {
        }

        @Override
        public int getThreads() {
            return 0;
        }
    };
    private final Set<Leased> leases = new CopyOnWriteArraySet<Leased>();
    private final AtomicBoolean warned = new AtomicBoolean();
    private final ThreadPool.SizedThreadPool pool;
    private final int warnAt;

    public ThreadPoolBudget(ThreadPool.SizedThreadPool pool) {
        this.pool = pool;
        this.warnAt = -1;
    }

    @Deprecated
    public ThreadPoolBudget(ThreadPool.SizedThreadPool pool, int warnAt) {
        this.pool = pool;
        this.warnAt = warnAt;
    }

    public ThreadPool.SizedThreadPool getSizedThreadPool() {
        return this.pool;
    }

    @ManagedAttribute(value="the number of threads leased to components")
    public int getLeasedThreads() {
        return this.leases.stream().mapToInt(Lease::getThreads).sum();
    }

    public void reset() {
        this.leases.clear();
        this.warned.set(false);
    }

    public Lease leaseTo(Object leasee, int threads) {
        Leased lease = new Leased(leasee, threads);
        this.leases.add(lease);
        try {
            this.check(this.pool.getMaxThreads());
            return lease;
        }
        catch (IllegalStateException e) {
            lease.close();
            throw e;
        }
    }

    public boolean check(int maxThreads) throws IllegalStateException {
        int required = this.getLeasedThreads();
        int left = maxThreads - required;
        if (left <= 0) {
            this.printInfoOnLeases();
            throw new IllegalStateException(String.format("Insufficient configured threads: required=%d < max=%d for %s", required, maxThreads, this.pool));
        }
        if (left < this.warnAt) {
            if (this.warned.compareAndSet(false, true)) {
                this.printInfoOnLeases();
                LOG.info("Low configured threads: (max={} - required={})={} < warnAt={} for {}", maxThreads, required, left, this.warnAt, this.pool);
            }
            return false;
        }
        return true;
    }

    private void printInfoOnLeases() {
        this.leases.forEach(lease -> LOG.info("{} requires {} threads from {}", ((Leased)lease).leasee, lease.getThreads(), this.pool));
    }

    public static Lease leaseFrom(Executor executor, Object leasee, int threads) {
        ThreadPoolBudget budget;
        if (executor instanceof ThreadPool.SizedThreadPool && (budget = ((ThreadPool.SizedThreadPool)executor).getThreadPoolBudget()) != null) {
            return budget.leaseTo(leasee, threads);
        }
        return NOOP_LEASE;
    }

    public class Leased
    implements Lease {
        private final Object leasee;
        private final int threads;

        private Leased(Object leasee, int threads) {
            this.leasee = leasee;
            this.threads = threads;
        }

        @Override
        public int getThreads() {
            return this.threads;
        }

        @Override
        public void close() {
            ThreadPoolBudget.this.leases.remove(this);
            ThreadPoolBudget.this.warned.set(false);
        }
    }

    public static interface Lease
    extends Closeable {
        public int getThreads();
    }
}

