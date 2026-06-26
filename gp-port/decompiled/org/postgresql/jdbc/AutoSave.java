/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.util.Locale;

public enum AutoSave {
    NEVER,
    ALWAYS,
    CONSERVATIVE;

    private final String value = this.name().toLowerCase(Locale.ROOT);

    public String value() {
        return this.value;
    }

    public static AutoSave of(String value) {
        return AutoSave.valueOf(value.toUpperCase(Locale.ROOT));
    }
}

