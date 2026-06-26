/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.thread;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Locker {
    private final ReentrantLock _lock = new ReentrantLock();
    private final Lock _unlock = new Lock();

    public Lock lock() {
        this._lock.lock();
        return this._unlock;
    }

    @Deprecated
    public Lock lockIfNotHeld() {
        return this.lock();
    }

    public boolean isLocked() {
        return this._lock.isLocked();
    }

    public Condition newCondition() {
        return this._lock.newCondition();
    }

    @Deprecated
    public class UnLock
    extends Lock {
    }

    public class Lock
    implements AutoCloseable {
        @Override
        public void close() {
            Locker.this._lock.unlock();
        }
    }
}

