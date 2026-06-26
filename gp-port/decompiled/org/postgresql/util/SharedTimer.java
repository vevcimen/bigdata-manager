/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SharedTimer {
    private static final AtomicInteger timerCount = new AtomicInteger(0);
    private static final Logger LOGGER = Logger.getLogger(SharedTimer.class.getName());
    private volatile @Nullable Timer timer;
    private final AtomicInteger refCount = new AtomicInteger(0);

    public int getRefCount() {
        return this.refCount.get();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized Timer getTimer() {
        Timer timer = this.timer;
        if (timer == null) {
            int index = timerCount.incrementAndGet();
            ClassLoader prevContextCL = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(null);
                this.timer = timer = new Timer("PostgreSQL-JDBC-SharedTimer-" + index, true);
            }
            finally {
                Thread.currentThread().setContextClassLoader(prevContextCL);
            }
        }
        this.refCount.incrementAndGet();
        return timer;
    }

    public synchronized void releaseTimer() {
        int count = this.refCount.decrementAndGet();
        if (count > 0) {
            LOGGER.log(Level.FINEST, "Outstanding references still exist so not closing shared Timer");
        } else if (count == 0) {
            LOGGER.log(Level.FINEST, "No outstanding references to shared Timer, will cancel and close it");
            if (this.timer != null) {
                this.timer.cancel();
                this.timer = null;
            }
        } else {
            LOGGER.log(Level.WARNING, "releaseTimer() called too many times; there is probably a bug in the calling code");
            this.refCount.set(0);
        }
    }
}

