/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.util;

public class Preconditions {
    public static <T> T checkNotNull(T value, String valueName) throws IllegalArgumentException {
        if (null == value) {
            throw new IllegalArgumentException("Null value for '" + valueName + "'");
        }
        return value;
    }

    public static String checkNotEmpty(String value, String valueName) throws IllegalArgumentException {
        if (Preconditions.checkNotNull(value, valueName).isEmpty()) {
            throw new IllegalArgumentException("Empty string '" + valueName + "'");
        }
        return value;
    }

    public static void checkArgument(boolean check, String valueName) throws IllegalArgumentException {
        if (!check) {
            throw new IllegalArgumentException("Argument '" + valueName + "' is not valid");
        }
    }

    public static int gt0(int value, String valueName) throws IllegalArgumentException {
        if (value <= 0) {
            throw new IllegalArgumentException("'" + valueName + "' must be positive");
        }
        return value;
    }
}

