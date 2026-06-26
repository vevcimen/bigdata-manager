/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class Strings {
    public static byte[] toUTF8ByteArray(char[] string) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        try {
            Strings.toUTF8ByteArray(string, bOut);
        }
        catch (IOException e) {
            throw new IllegalStateException("cannot encode string to byte array!");
        }
        return bOut.toByteArray();
    }

    public static void toUTF8ByteArray(char[] string, OutputStream sOut) throws IOException {
        char[] c = string;
        for (int i = 0; i < c.length; ++i) {
            char ch = c[i];
            if (ch < '\u0080') {
                sOut.write(ch);
                continue;
            }
            if (ch < '\u0800') {
                sOut.write(0xC0 | ch >> 6);
                sOut.write(0x80 | ch & 0x3F);
                continue;
            }
            if (ch >= '\ud800' && ch <= '\udfff') {
                if (i + 1 >= c.length) {
                    throw new IllegalStateException("invalid UTF-16 codepoint");
                }
                char W1 = ch;
                char W2 = ch = c[++i];
                if (W1 > '\udbff') {
                    throw new IllegalStateException("invalid UTF-16 codepoint");
                }
                int codePoint = ((W1 & 0x3FF) << 10 | W2 & 0x3FF) + 65536;
                sOut.write(0xF0 | codePoint >> 18);
                sOut.write(0x80 | codePoint >> 12 & 0x3F);
                sOut.write(0x80 | codePoint >> 6 & 0x3F);
                sOut.write(0x80 | codePoint & 0x3F);
                continue;
            }
            sOut.write(0xE0 | ch >> 12);
            sOut.write(0x80 | ch >> 6 & 0x3F);
            sOut.write(0x80 | ch & 0x3F);
        }
    }

    public static String fromByteArray(byte[] bytes) {
        return new String(Strings.asCharArray(bytes));
    }

    public static char[] asCharArray(byte[] bytes) {
        char[] chars = new char[bytes.length];
        for (int i = 0; i != chars.length; ++i) {
            chars[i] = (char)(bytes[i] & 0xFF);
        }
        return chars;
    }
}

