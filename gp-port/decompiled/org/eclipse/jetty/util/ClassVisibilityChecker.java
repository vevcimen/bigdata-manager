/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

public interface ClassVisibilityChecker {
    public boolean isSystemClass(Class<?> var1);

    public boolean isServerClass(Class<?> var1);
}

