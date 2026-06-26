/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jetty.util.ArrayTrie;
import org.eclipse.jetty.util.QuotedStringTokenizer;
import org.eclipse.jetty.util.Trie;
import org.eclipse.jetty.util.TypeUtil;

public class StringUtil {
    private static final Trie<String> CHARSETS = new ArrayTrie<String>(256);
    public static final String ALL_INTERFACES = "0.0.0.0";
    public static final String CRLF = "\r\n";
    @Deprecated
    public static final String __LINE_SEPARATOR = System.lineSeparator();
    public static final String __ISO_8859_1 = "iso-8859-1";
    public static final String __UTF8 = "utf-8";
    public static final String __UTF16 = "utf-16";
    public static final char[] lowercases;

    public static String normalizeCharset(String s2) {
        String n = CHARSETS.get(s2);
        return n == null ? s2 : n;
    }

    public static String normalizeCharset(String s2, int offset, int length) {
        String n = CHARSETS.get(s2, offset, length);
        return n == null ? s2.substring(offset, offset + length) : n;
    }

    public static String asciiToLowerCase(String s2) {
        if (s2 == null) {
            return null;
        }
        char[] c = null;
        int i = s2.length();
        while (i-- > 0) {
            char c2;
            char c1 = s2.charAt(i);
            if (c1 > '\u007f' || c1 == (c2 = lowercases[c1])) continue;
            c = s2.toCharArray();
            c[i] = c2;
            break;
        }
        while (i-- > 0) {
            if (c[i] > 127) continue;
            c[i] = lowercases[c[i]];
        }
        return c == null ? s2 : new String(c);
    }

    public static String sanitizeFileSystemName(String str) {
        if (str == null) {
            return null;
        }
        char[] chars = str.toCharArray();
        int len = chars.length;
        for (int i = 0; i < len; ++i) {
            char c = chars[i];
            if (c > '\u001f' && c < '\u007f' && c != '|' && c != '>' && c != '<' && c != '/' && c != '&' && c != '\\' && c != '.' && c != ':' && c != '=' && c != '\"' && c != ',' && c != '*' && c != '?' && c != '!' && c != ' ') continue;
            chars[i] = 95;
        }
        return String.valueOf(chars);
    }

    public static boolean startsWithIgnoreCase(String s2, String w) {
        if (w == null) {
            return true;
        }
        if (s2 == null || s2.length() < w.length()) {
            return false;
        }
        for (int i = 0; i < w.length(); ++i) {
            char c2;
            char c1 = s2.charAt(i);
            if (c1 == (c2 = w.charAt(i))) continue;
            if (c1 <= '\u007f') {
                c1 = lowercases[c1];
            }
            if (c2 <= '\u007f') {
                c2 = lowercases[c2];
            }
            if (c1 == c2) continue;
            return false;
        }
        return true;
    }

    public static boolean endsWithIgnoreCase(String s2, String w) {
        int wl;
        if (w == null) {
            return true;
        }
        if (s2 == null) {
            return false;
        }
        int sl = s2.length();
        if (sl < (wl = w.length())) {
            return false;
        }
        int i = wl;
        while (i-- > 0) {
            char c2;
            char c1;
            if ((c1 = s2.charAt(--sl)) == (c2 = w.charAt(i))) continue;
            if (c1 <= '\u007f') {
                c1 = lowercases[c1];
            }
            if (c2 <= '\u007f') {
                c2 = lowercases[c2];
            }
            if (c1 == c2) continue;
            return false;
        }
        return true;
    }

    public static int indexFrom(String s2, String chars) {
        for (int i = 0; i < s2.length(); ++i) {
            if (chars.indexOf(s2.charAt(i)) < 0) continue;
            return i;
        }
        return -1;
    }

    public static String replace(String str, char find, char with) {
        if (str == null) {
            return null;
        }
        if (find == with) {
            return str;
        }
        int c = 0;
        int idx = str.indexOf(find, c);
        if (idx == -1) {
            return str;
        }
        char[] chars = str.toCharArray();
        int len = chars.length;
        for (int i = idx; i < len; ++i) {
            if (chars[i] != find) continue;
            chars[i] = with;
        }
        return String.valueOf(chars);
    }

    public static String replace(String s2, String sub, String with) {
        if (s2 == null) {
            return null;
        }
        int c = 0;
        int i = s2.indexOf(sub, c);
        if (i == -1) {
            return s2;
        }
        StringBuilder buf = new StringBuilder(s2.length() + with.length());
        do {
            buf.append(s2, c, i);
            buf.append(with);
        } while ((i = s2.indexOf(sub, c = i + sub.length())) != -1);
        if (c < s2.length()) {
            buf.append(s2.substring(c));
        }
        return buf.toString();
    }

    public static String replaceFirst(String original, String target, String replacement) {
        int idx = original.indexOf(target);
        if (idx == -1) {
            return original;
        }
        int offset = 0;
        int originalLen = original.length();
        StringBuilder buf = new StringBuilder(originalLen + replacement.length());
        buf.append(original, offset, idx);
        buf.append(replacement);
        buf.append(original, offset += idx + target.length(), originalLen);
        return buf.toString();
    }

    @Deprecated
    public static String unquote(String s2) {
        return QuotedStringTokenizer.unquote(s2);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void append(StringBuilder buf, String s2, int offset, int length) {
        StringBuilder stringBuilder = buf;
        synchronized (stringBuilder) {
            int end = offset + length;
            for (int i = offset; i < end && i < s2.length(); ++i) {
                buf.append(s2.charAt(i));
            }
        }
    }

    public static void append(StringBuilder buf, byte b, int base) {
        int bi = 0xFF & b;
        int c = 48 + bi / base % base;
        if (c > 57) {
            c = 97 + (c - 48 - 10);
        }
        buf.append((char)c);
        c = 48 + bi % base;
        if (c > 57) {
            c = 97 + (c - 48 - 10);
        }
        buf.append((char)c);
    }

    public static void append2digits(StringBuffer buf, int i) {
        if (i < 100) {
            buf.append((char)(i / 10 + 48));
            buf.append((char)(i % 10 + 48));
        }
    }

    public static void append2digits(StringBuilder buf, int i) {
        if (i < 100) {
            buf.append((char)(i / 10 + 48));
            buf.append((char)(i % 10 + 48));
        }
    }

    public static String nonNull(String s2) {
        if (s2 == null) {
            return "";
        }
        return s2;
    }

    public static boolean equals(String s2, char[] buf, int offset, int length) {
        if (s2.length() != length) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            if (buf[offset + i] == s2.charAt(i)) continue;
            return false;
        }
        return true;
    }

    public static String toUTF8String(byte[] b, int offset, int length) {
        return new String(b, offset, length, StandardCharsets.UTF_8);
    }

    public static String toString(byte[] b, int offset, int length, String charset) {
        try {
            return new String(b, offset, length, charset);
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static int indexOfControlChars(String str) {
        if (str == null) {
            return -1;
        }
        int len = str.length();
        for (int i = 0; i < len; ++i) {
            if (!Character.isISOControl(str.codePointAt(i))) continue;
            return i;
        }
        return -1;
    }

    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        int len = str.length();
        for (int i = 0; i < len; ++i) {
            if (Character.isWhitespace(str.codePointAt(i))) continue;
            return false;
        }
        return true;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotBlank(String str) {
        if (str == null) {
            return false;
        }
        int len = str.length();
        for (int i = 0; i < len; ++i) {
            if (Character.isWhitespace(str.codePointAt(i))) continue;
            return true;
        }
        return false;
    }

    public static boolean isUTF8(String charset) {
        return __UTF8.equalsIgnoreCase(charset) || __UTF8.equalsIgnoreCase(StringUtil.normalizeCharset(charset));
    }

    public static boolean isHex(String str, int offset, int length) {
        if (offset + length > str.length()) {
            return false;
        }
        for (int i = offset; i < offset + length; ++i) {
            char c = str.charAt(i);
            if (c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F' || c >= '0' && c <= '9') continue;
            return false;
        }
        return true;
    }

    public static String printable(String name) {
        if (name == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (Character.isISOControl(c)) continue;
            buf.append(c);
        }
        return buf.toString();
    }

    public static String printable(byte[] b) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < b.length; ++i) {
            char c = (char)b[i];
            if (Character.isWhitespace(c) || c > ' ' && c < '\u007f') {
                buf.append(c);
                continue;
            }
            buf.append("0x");
            TypeUtil.toHex(b[i], (Appendable)buf);
        }
        return buf.toString();
    }

    public static byte[] getBytes(String s2) {
        return s2.getBytes(StandardCharsets.ISO_8859_1);
    }

    public static byte[] getUtf8Bytes(String s2) {
        return s2.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] getBytes(String s2, String charset) {
        try {
            return s2.getBytes(charset);
        }
        catch (Exception e) {
            return s2.getBytes();
        }
    }

    @Deprecated
    public static String sidBytesToString(byte[] sidBytes) {
        StringBuilder sidString = new StringBuilder();
        sidString.append("S-");
        sidString.append(sidBytes[0]).append('-');
        StringBuilder tmpBuilder = new StringBuilder();
        for (int i = 2; i <= 7; ++i) {
            tmpBuilder.append(Integer.toHexString(sidBytes[i] & 0xFF));
        }
        sidString.append(Long.parseLong(tmpBuilder.toString(), 16));
        int subAuthorityCount = sidBytes[1];
        for (int i = 0; i < subAuthorityCount; ++i) {
            int offset = i * 4;
            tmpBuilder.setLength(0);
            tmpBuilder.append(String.format("%02X%02X%02X%02X", sidBytes[11 + offset] & 0xFF, sidBytes[10 + offset] & 0xFF, sidBytes[9 + offset] & 0xFF, sidBytes[8 + offset] & 0xFF));
            sidString.append('-').append(Long.parseLong(tmpBuilder.toString(), 16));
        }
        return sidString.toString();
    }

    @Deprecated
    public static byte[] sidStringToBytes(String sidString) {
        int i;
        String[] sidTokens = sidString.split("-");
        int subAuthorityCount = sidTokens.length - 3;
        int byteCount = 0;
        byte[] sidBytes = new byte[8 + 4 * subAuthorityCount];
        sidBytes[byteCount++] = (byte)Integer.parseInt(sidTokens[1]);
        sidBytes[byteCount++] = (byte)subAuthorityCount;
        String hexStr = Long.toHexString(Long.parseLong(sidTokens[2]));
        while (hexStr.length() < 12) {
            hexStr = "0" + hexStr;
        }
        for (i = 0; i < hexStr.length(); i += 2) {
            sidBytes[byteCount++] = (byte)Integer.parseInt(hexStr.substring(i, i + 2), 16);
        }
        for (i = 3; i < sidTokens.length; ++i) {
            hexStr = Long.toHexString(Long.parseLong(sidTokens[i]));
            while (hexStr.length() < 8) {
                hexStr = "0" + hexStr;
            }
            for (int j = hexStr.length(); j > 0; j -= 2) {
                sidBytes[byteCount++] = (byte)Integer.parseInt(hexStr.substring(j - 2, j), 16);
            }
        }
        return sidBytes;
    }

    public static int toInt(String string, int from) {
        int val = 0;
        boolean started = false;
        boolean minus = false;
        for (int i = from; i < string.length(); ++i) {
            char b = string.charAt(i);
            if (b <= ' ') {
                if (!started) continue;
                break;
            }
            if (b >= '0' && b <= '9') {
                val = val * 10 + (b - 48);
                started = true;
                continue;
            }
            if (b != '-' || started) break;
            minus = true;
        }
        if (started) {
            return minus ? -val : val;
        }
        throw new NumberFormatException(string);
    }

    public static long toLong(String string) {
        long val = 0L;
        boolean started = false;
        boolean minus = false;
        for (int i = 0; i < string.length(); ++i) {
            char b = string.charAt(i);
            if (b <= ' ') {
                if (!started) continue;
                break;
            }
            if (b >= '0' && b <= '9') {
                val = val * 10L + (long)(b - 48);
                started = true;
                continue;
            }
            if (b != '-' || started) break;
            minus = true;
        }
        if (started) {
            return minus ? -val : val;
        }
        throw new NumberFormatException(string);
    }

    public static String truncate(String str, int maxSize) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxSize) {
            return str;
        }
        return str.substring(0, maxSize);
    }

    public static String[] arrayFromString(String s2) {
        if (s2 == null) {
            return new String[0];
        }
        if (!s2.startsWith("[") || !s2.endsWith("]")) {
            throw new IllegalArgumentException();
        }
        if (s2.length() == 2) {
            return new String[0];
        }
        return StringUtil.csvSplit(s2, 1, s2.length() - 2);
    }

    public static String[] csvSplit(String s2) {
        if (s2 == null) {
            return null;
        }
        return StringUtil.csvSplit(s2, 0, s2.length());
    }

    public static String[] csvSplit(String s2, int off, int len) {
        if (s2 == null) {
            return null;
        }
        if (off < 0 || len < 0 || off > s2.length()) {
            throw new IllegalArgumentException();
        }
        ArrayList<String> list = new ArrayList<String>();
        StringUtil.csvSplit(list, s2, off, len);
        return list.toArray(new String[list.size()]);
    }

    public static List<String> csvSplit(List<String> list, String s2, int off, int len) {
        if (list == null) {
            list = new ArrayList<String>();
        }
        CsvSplitState state = CsvSplitState.PRE_DATA;
        StringBuilder out = new StringBuilder();
        int last = -1;
        block13: while (len > 0) {
            char ch = s2.charAt(off++);
            --len;
            switch (state) {
                case PRE_DATA: {
                    if (Character.isWhitespace(ch)) continue block13;
                    if ('\"' == ch) {
                        state = CsvSplitState.QUOTE;
                        continue block13;
                    }
                    if (',' == ch) {
                        list.add("");
                        continue block13;
                    }
                    state = CsvSplitState.DATA;
                    out.append(ch);
                    continue block13;
                }
                case DATA: {
                    if (Character.isWhitespace(ch)) {
                        last = out.length();
                        out.append(ch);
                        state = CsvSplitState.WHITE;
                        continue block13;
                    }
                    if (',' == ch) {
                        list.add(out.toString());
                        out.setLength(0);
                        state = CsvSplitState.PRE_DATA;
                        continue block13;
                    }
                    out.append(ch);
                    continue block13;
                }
                case WHITE: {
                    if (Character.isWhitespace(ch)) {
                        out.append(ch);
                        continue block13;
                    }
                    if (',' == ch) {
                        out.setLength(last);
                        list.add(out.toString());
                        out.setLength(0);
                        state = CsvSplitState.PRE_DATA;
                        continue block13;
                    }
                    state = CsvSplitState.DATA;
                    out.append(ch);
                    last = -1;
                    continue block13;
                }
                case QUOTE: {
                    if ('\\' == ch) {
                        state = CsvSplitState.SLOSH;
                        continue block13;
                    }
                    if ('\"' == ch) {
                        list.add(out.toString());
                        out.setLength(0);
                        state = CsvSplitState.POST_DATA;
                        continue block13;
                    }
                    out.append(ch);
                    continue block13;
                }
                case SLOSH: {
                    out.append(ch);
                    state = CsvSplitState.QUOTE;
                    continue block13;
                }
                case POST_DATA: {
                    if (',' != ch) continue block13;
                    state = CsvSplitState.PRE_DATA;
                    continue block13;
                }
            }
        }
        switch (state) {
            case PRE_DATA: 
            case POST_DATA: {
                break;
            }
            case DATA: 
            case QUOTE: 
            case SLOSH: {
                list.add(out.toString());
                break;
            }
            case WHITE: {
                out.setLength(last);
                list.add(out.toString());
            }
        }
        return list;
    }

    public static String sanitizeXmlString(String html) {
        int i;
        if (html == null) {
            return null;
        }
        block10: for (i = 0; i < html.length(); ++i) {
            char c = html.charAt(i);
            switch (c) {
                case '\"': 
                case '&': 
                case '\'': 
                case '<': 
                case '>': {
                    break block10;
                }
                default: {
                    if (Character.isISOControl(c) && !Character.isWhitespace(c)) break block10;
                    continue block10;
                }
            }
        }
        if (i == html.length()) {
            return html;
        }
        StringBuilder out = new StringBuilder(html.length() * 4 / 3);
        out.append(html, 0, i);
        while (i < html.length()) {
            char c = html.charAt(i);
            switch (c) {
                case '&': {
                    out.append("&amp;");
                    break;
                }
                case '<': {
                    out.append("&lt;");
                    break;
                }
                case '>': {
                    out.append("&gt;");
                    break;
                }
                case '\'': {
                    out.append("&apos;");
                    break;
                }
                case '\"': {
                    out.append("&quot;");
                    break;
                }
                default: {
                    if (Character.isISOControl(c) && !Character.isWhitespace(c)) {
                        out.append('?');
                        break;
                    }
                    out.append(c);
                }
            }
            ++i;
        }
        return out.toString();
    }

    public static String strip(String str, String find) {
        return StringUtil.replace(str, find, "");
    }

    public static String valueOf(Object object) {
        return object == null ? null : String.valueOf(object);
    }

    static {
        CHARSETS.put(__UTF8, __UTF8);
        CHARSETS.put("utf8", __UTF8);
        CHARSETS.put(__UTF16, __UTF16);
        CHARSETS.put("utf16", __UTF16);
        CHARSETS.put(__ISO_8859_1, __ISO_8859_1);
        CHARSETS.put("iso_8859_1", __ISO_8859_1);
        lowercases = new char[]{'\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007', '\b', '\t', '\n', '\u000b', '\f', '\r', '\u000e', '\u000f', '\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019', '\u001a', '\u001b', '\u001c', '\u001d', '\u001e', '\u001f', ' ', '!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '@', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', '\u007f'};
    }

    static enum CsvSplitState {
        PRE_DATA,
        QUOTE,
        SLOSH,
        DATA,
        WHITE,
        POST_DATA;

    }
}

