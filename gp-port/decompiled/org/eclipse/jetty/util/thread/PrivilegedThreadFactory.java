/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.thread;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.function.Supplier;

class PrivilegedThreadFactory {
    PrivilegedThreadFactory() {
    }

    static <T extends Thread> T newThread(final Supplier<T> newThreadSupplier) {
        return (T)((Thread)AccessController.doPrivileged(new PrivilegedAction<T>(){

            @Override
            public T run() {
                return (Thread)newThreadSupplier.get();
            }
        }));
    }
}

