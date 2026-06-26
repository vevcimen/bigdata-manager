/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.thread;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public interface TryExecutor
extends Executor {
    public static final TryExecutor NO_TRY = new TryExecutor(){

        @Override
        public boolean tryExecute(Runnable task) {
            return false;
        }

        public String toString() {
            return "NO_TRY";
        }
    };

    public boolean tryExecute(Runnable var1);

    @Override
    default public void execute(Runnable task) {
        if (!this.tryExecute(task)) {
            throw new RejectedExecutionException();
        }
    }

    public static TryExecutor asTryExecutor(Executor executor) {
        if (executor instanceof TryExecutor) {
            return (TryExecutor)executor;
        }
        return new NoTryExecutor(executor);
    }

    public static class NoTryExecutor
    implements TryExecutor {
        private final Executor executor;

        public NoTryExecutor(Executor executor) {
            this.executor = executor;
        }

        @Override
        public void execute(Runnable task) {
            this.executor.execute(task);
        }

        @Override
        public boolean tryExecute(Runnable task) {
            return false;
        }

        public String toString() {
            return String.format("%s@%x[%s]", this.getClass().getSimpleName(), this.hashCode(), this.executor);
        }
    }
}

