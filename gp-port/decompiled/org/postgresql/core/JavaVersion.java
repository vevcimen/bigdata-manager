/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

public enum JavaVersion {
    v1_8,
    other;

    private static final JavaVersion RUNTIME_VERSION;

    public static JavaVersion getRuntimeVersion() {
        return RUNTIME_VERSION;
    }

    public static JavaVersion from(String version) {
        if (version.startsWith("1.8")) {
            return v1_8;
        }
        return other;
    }

    static {
        RUNTIME_VERSION = JavaVersion.from(System.getProperty("java.version"));
    }
}

