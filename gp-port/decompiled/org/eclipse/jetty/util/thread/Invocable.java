/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.thread;

public interface Invocable {
    public static final ThreadLocal<Boolean> __nonBlocking = new ThreadLocal();

    public static boolean isNonBlockingInvocation() {
        return Boolean.TRUE.equals(__nonBlocking.get());
    }

    public static void invokeNonBlocking(Runnable task) {
        Boolean wasNonBlocking = __nonBlocking.get();
        try {
            __nonBlocking.set(Boolean.TRUE);
            task.run();
        }
        finally {
            __nonBlocking.set(wasNonBlocking);
        }
    }

    public static InvocationType combine(InvocationType it1, InvocationType it2) {
        if (it1 != null && it2 != null) {
            if (it1 == it2) {
                return it1;
            }
            if (it1 == InvocationType.EITHER) {
                return it2;
            }
            if (it2 == InvocationType.EITHER) {
                return it1;
            }
        }
        return InvocationType.BLOCKING;
    }

    public static InvocationType getInvocationType(Object o) {
        if (o instanceof Invocable) {
            return ((Invocable)o).getInvocationType();
        }
        return InvocationType.BLOCKING;
    }

    default public InvocationType getInvocationType() {
        return InvocationType.BLOCKING;
    }

    public static enum InvocationType {
        BLOCKING,
        NON_BLOCKING,
        EITHER;

    }
}

