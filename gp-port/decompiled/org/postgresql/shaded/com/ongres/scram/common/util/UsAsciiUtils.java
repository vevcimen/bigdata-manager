/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.util;

import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public class UsAsciiUtils {
    public static String toPrintable(String value) throws IllegalArgumentException {
        Preconditions.checkNotNull(value, "value");
        char[] printable = new char[value.length()];
        int i = 0;
        for (char chr : value.toCharArray()) {
            char c = chr;
            if (c < '\u0000' || c >= '\u007f') {
                throw new IllegalArgumentException("value contains character '" + chr + "' which is non US-ASCII");
            }
            if (c <= ' ') continue;
            printable[i++] = chr;
        }
        return i == value.length() ? value : new String(printable, 0, i);
    }
}

