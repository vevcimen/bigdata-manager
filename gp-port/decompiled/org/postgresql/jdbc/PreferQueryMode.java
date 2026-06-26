/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

public enum PreferQueryMode {
    SIMPLE("simple"),
    EXTENDED_FOR_PREPARED("extendedForPrepared"),
    EXTENDED("extended"),
    EXTENDED_CACHE_EVERYTHING("extendedCacheEverything");

    private final String value;

    private PreferQueryMode(String value) {
        this.value = value;
    }

    public static PreferQueryMode of(String mode) {
        for (PreferQueryMode preferQueryMode : PreferQueryMode.values()) {
            if (!preferQueryMode.value.equals(mode)) continue;
            return preferQueryMode;
        }
        return EXTENDED;
    }

    public String value() {
        return this.value;
    }
}

