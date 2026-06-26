/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common;

import java.nio.charset.StandardCharsets;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.base64.Base64;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public class ScramStringFormatting {
    public static String toSaslName(String value) {
        char[] originalChars;
        if (null == value || value.isEmpty()) {
            return value;
        }
        int nComma = 0;
        int nEqual = 0;
        for (char c : originalChars = value.toCharArray()) {
            if (',' == c) {
                ++nComma;
                continue;
            }
            if ('=' != c) continue;
            ++nEqual;
        }
        if (nComma == 0 && nEqual == 0) {
            return value;
        }
        char[] saslChars = new char[originalChars.length + nComma * 2 + nEqual * 2];
        int i = 0;
        for (char c : originalChars) {
            if (',' == c) {
                saslChars[i++] = 61;
                saslChars[i++] = 50;
                saslChars[i++] = 67;
                continue;
            }
            if ('=' == c) {
                saslChars[i++] = 61;
                saslChars[i++] = 51;
                saslChars[i++] = 68;
                continue;
            }
            saslChars[i++] = c;
        }
        return new String(saslChars);
    }

    public static String fromSaslName(String value) throws IllegalArgumentException {
        if (null == value || value.isEmpty()) {
            return value;
        }
        int nEqual = 0;
        char[] orig = value.toCharArray();
        for (int i = 0; i < orig.length; ++i) {
            if (orig[i] == ',') {
                throw new IllegalArgumentException("Invalid ',' character present in saslName");
            }
            if (orig[i] != '=') continue;
            ++nEqual;
            if (i + 2 > orig.length - 1) {
                throw new IllegalArgumentException("Invalid '=' character present in saslName");
            }
            if (orig[i + 1] == '2' && orig[i + 2] == 'C' || orig[i + 1] == '3' && orig[i + 2] == 'D') continue;
            throw new IllegalArgumentException("Invalid char '=" + orig[i + 1] + orig[i + 2] + "' found in saslName");
        }
        if (nEqual == 0) {
            return value;
        }
        char[] replaced = new char[orig.length - nEqual * 2];
        int o = 0;
        for (int r = 0; r < replaced.length; ++r) {
            if ('=' == orig[o]) {
                if (orig[o + 1] == '2' && orig[o + 2] == 'C') {
                    replaced[r] = 44;
                } else if (orig[o + 1] == '3' && orig[o + 2] == 'D') {
                    replaced[r] = 61;
                }
                o += 3;
                continue;
            }
            replaced[r] = orig[o];
            ++o;
        }
        return new String(replaced);
    }

    public static String base64Encode(byte[] value) throws IllegalArgumentException {
        return Base64.toBase64String(Preconditions.checkNotNull(value, "value"));
    }

    public static String base64Encode(String value) throws IllegalArgumentException {
        return ScramStringFormatting.base64Encode(Preconditions.checkNotEmpty(value, "value").getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] base64Decode(String value) throws IllegalArgumentException {
        return Base64.decode(Preconditions.checkNotEmpty(value, "value"));
    }
}

