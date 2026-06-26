/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.util;

import java.util.Arrays;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.postgresql.shaded.com.ongres.scram.common.util.StringWritable;

public class StringWritableCsv {
    private static void writeStringWritableToStringBuffer(StringWritable value, StringBuffer sb) {
        if (null != value) {
            value.writeTo(sb);
        }
    }

    public static StringBuffer writeTo(StringBuffer sb, StringWritable ... values) throws IllegalArgumentException {
        Preconditions.checkNotNull(sb, "sb");
        if (null == values || values.length == 0) {
            return sb;
        }
        StringWritableCsv.writeStringWritableToStringBuffer(values[0], sb);
        for (int i = 1; i < values.length; ++i) {
            sb.append(',');
            StringWritableCsv.writeStringWritableToStringBuffer(values[i], sb);
        }
        return sb;
    }

    public static String[] parseFrom(String value, int n, int offset) throws IllegalArgumentException {
        Preconditions.checkNotNull(value, "value");
        if (n < 0 || offset < 0) {
            throw new IllegalArgumentException("Limit and offset have to be >= 0");
        }
        if (value.isEmpty()) {
            return new String[0];
        }
        String[] split = value.split(",");
        if (split.length < offset) {
            throw new IllegalArgumentException("Not enough items for the given offset");
        }
        return Arrays.copyOfRange(split, offset, (n == 0 ? split.length : n) + offset);
    }

    public static String[] parseFrom(String value, int n) throws IllegalArgumentException {
        return StringWritableCsv.parseFrom(value, n, 0);
    }

    public static String[] parseFrom(String value) throws IllegalArgumentException {
        return StringWritableCsv.parseFrom(value, 0, 0);
    }
}

