/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.component;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jetty.util.FutureCallback;

public interface Graceful {
    public Future<Void> shutdown();

    public boolean isShutdown();

    public static class Shutdown
    implements Graceful {
        private final AtomicReference<FutureCallback> _shutdown = new AtomicReference();

        protected FutureCallback newShutdownCallback() {
            return FutureCallback.SUCCEEDED;
        }

        @Override
        public Future<Void> shutdown() {
            return this._shutdown.updateAndGet(fcb -> fcb == null ? this.newShutdownCallback() : fcb);
        }

        @Override
        public boolean isShutdown() {
            return this._shutdown.get() != null;
        }

        public void cancel() {
            FutureCallback shutdown = this._shutdown.getAndSet(null);
            if (shutdown != null && !shutdown.isDone()) {
                shutdown.cancel(true);
            }
        }

        public FutureCallback get() {
            return this._shutdown.get();
        }
    }
}

