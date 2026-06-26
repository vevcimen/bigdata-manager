/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input;

import shadeio.univocity.parsers.common.input.CharAppender;
import shadeio.univocity.parsers.common.input.CharInput;

public class DefaultCharAppender
implements CharAppender {
    final int whitespaceRangeStart;
    final char[] emptyChars;
    char[] chars;
    int index;
    final String emptyValue;
    int whitespaceCount;

    public DefaultCharAppender(int maxLength, String emptyValue, int whitespaceRangeStart) {
        this.whitespaceRangeStart = whitespaceRangeStart;
        this.chars = new char[maxLength];
        this.emptyValue = emptyValue;
        this.emptyChars = (char[])(emptyValue == null ? null : emptyValue.toCharArray());
    }

    @Override
    public void appendIgnoringPadding(char ch, char padding) {
        this.chars[this.index++] = ch;
        this.whitespaceCount = ch == padding ? ++this.whitespaceCount : 0;
    }

    @Override
    public void appendIgnoringWhitespaceAndPadding(char ch, char padding) {
        this.chars[this.index++] = ch;
        this.whitespaceCount = ch == padding || ch <= ' ' && this.whitespaceRangeStart < ch ? ++this.whitespaceCount : 0;
    }

    @Override
    public void appendIgnoringWhitespace(char ch) {
        this.chars[this.index++] = ch;
        this.whitespaceCount = ch <= ' ' && this.whitespaceRangeStart < ch ? ++this.whitespaceCount : 0;
    }

    @Override
    public int indexOf(char ch, int from) {
        int len = this.index - this.whitespaceCount;
        for (int i = from; i < len; ++i) {
            if (this.chars[i] != ch) continue;
            return i;
        }
        return -1;
    }

    @Override
    public int indexOfAny(char[] chars, int from) {
        int len = this.index - this.whitespaceCount;
        for (int i = from; i < len; ++i) {
            for (int j = 0; j < chars.length; ++j) {
                if (this.chars[i] != chars[j]) continue;
                return i;
            }
        }
        return -1;
    }

    @Override
    public String substring(int from, int length) {
        return new String(this.chars, from, length);
    }

    @Override
    public void remove(int from, int length) {
        if (length > 0) {
            int srcPos = from + length;
            int len = this.index - length;
            if (srcPos + len > this.index) {
                len -= from;
            }
            System.arraycopy(this.chars, srcPos, this.chars, from, len);
            this.index -= length;
        }
    }

    @Override
    public void append(char ch) {
        this.chars[this.index++] = ch;
    }

    @Override
    public final void append(Object o) {
        this.append(String.valueOf(o));
    }

    @Override
    public final void append(int ch) {
        if (ch < 65536) {
            this.append((char)ch);
        } else {
            int off = ch - 65536;
            this.append((char)((off >>> 10) + 55296));
            this.append((char)((off & 0x3FF) + 56320));
        }
    }

    @Override
    public final void append(int[] ch) {
        for (int i = 0; i < ch.length; ++i) {
            this.append(ch[i]);
        }
    }

    @Override
    public String getAndReset() {
        String out = this.emptyValue;
        if (this.index > this.whitespaceCount) {
            out = new String(this.chars, 0, this.index - this.whitespaceCount);
        }
        this.index = 0;
        this.whitespaceCount = 0;
        return out;
    }

    @Override
    public final String toString() {
        if (this.index <= this.whitespaceCount) {
            return this.emptyValue;
        }
        return new String(this.chars, 0, this.index - this.whitespaceCount);
    }

    @Override
    public final int length() {
        return this.index - this.whitespaceCount;
    }

    @Override
    public char[] getCharsAndReset() {
        char[] out = this.emptyChars;
        if (this.index > this.whitespaceCount) {
            int length = this.index - this.whitespaceCount;
            out = new char[length];
            System.arraycopy(this.chars, 0, out, 0, length);
        }
        this.index = 0;
        this.whitespaceCount = 0;
        return out;
    }

    @Override
    public final int whitespaceCount() {
        return this.whitespaceCount;
    }

    @Override
    public void reset() {
        this.index = 0;
        this.whitespaceCount = 0;
    }

    public void append(DefaultCharAppender appender) {
        System.arraycopy(appender.chars, 0, this.chars, this.index, appender.index - appender.whitespaceCount);
        this.index += appender.index - appender.whitespaceCount;
        appender.reset();
    }

    @Override
    public final void resetWhitespaceCount() {
        this.whitespaceCount = 0;
    }

    @Override
    public final char[] getChars() {
        return this.chars;
    }

    @Override
    public void fill(char ch, int length) {
        for (int i = 0; i < length; ++i) {
            this.chars[this.index++] = ch;
        }
    }

    @Override
    public void prepend(char ch) {
        System.arraycopy(this.chars, 0, this.chars, 1, this.index);
        this.chars[0] = ch;
        ++this.index;
    }

    @Override
    public void prepend(char ch1, char ch2) {
        System.arraycopy(this.chars, 0, this.chars, 2, this.index);
        this.chars[0] = ch1;
        this.chars[1] = ch2;
        this.index += 2;
    }

    @Override
    public void prepend(char[] chars) {
        System.arraycopy(this.chars, 0, this.chars, chars.length, this.index);
        System.arraycopy(chars, 0, this.chars, 0, chars.length);
        this.index += chars.length;
    }

    @Override
    public final void updateWhitespace() {
        this.whitespaceCount = 0;
        int i = this.index - 1;
        while (i >= 0 && this.chars[i] <= ' ' && this.whitespaceRangeStart < this.chars[i]) {
            --i;
            ++this.whitespaceCount;
        }
    }

    @Override
    public char appendUntil(char ch, CharInput input, char stop) {
        while (ch != stop) {
            this.chars[this.index++] = ch;
            ch = input.nextChar();
        }
        return ch;
    }

    @Override
    public char appendUntil(char ch, CharInput input, char stop1, char stop2) {
        while (ch != stop1 && ch != stop2) {
            this.chars[this.index++] = ch;
            ch = input.nextChar();
        }
        return ch;
    }

    @Override
    public char appendUntil(char ch, CharInput input, char stop1, char stop2, char stop3) {
        while (ch != stop1 && ch != stop2 && ch != stop3) {
            this.chars[this.index++] = ch;
            ch = input.nextChar();
        }
        return ch;
    }

    @Override
    public void append(char[] ch, int from, int length) {
        System.arraycopy(ch, from, this.chars, this.index, length);
        this.index += length;
    }

    @Override
    public final void append(char[] ch) {
        this.append(ch, 0, ch.length);
    }

    @Override
    public void append(String string, int from, int to) {
        string.getChars(from, to, this.chars, this.index);
        this.index += to - from;
    }

    @Override
    public final void append(String string) {
        this.append(string, 0, string.length());
    }

    @Override
    public final char charAt(int i) {
        return this.chars[i];
    }

    @Override
    public final String subSequence(int from, int to) {
        return new String(this.chars, from, to - from);
    }

    @Override
    public final void ignore(int count) {
        this.whitespaceCount += count;
    }

    @Override
    public void delete(int count) {
        this.index -= count;
        if (this.index < 0) {
            this.index = 0;
        }
        this.whitespaceCount = 0;
    }

    @Override
    public int indexOf(char[] charSequence, int fromIndex) {
        if (charSequence.length == 0) {
            return fromIndex;
        }
        if (fromIndex >= this.index) {
            return -1;
        }
        char first = charSequence[0];
        int max = this.index - charSequence.length;
        for (int i = fromIndex; i <= max; ++i) {
            if (this.chars[i] != first) {
                while (++i <= max && this.chars[i] != first) {
                }
            }
            if (i > max) continue;
            int j = i + 1;
            int end = j + charSequence.length - 1;
            int k = 1;
            while (j < end && this.chars[j] == charSequence[k]) {
                ++j;
                ++k;
            }
            if (j != end) continue;
            return i;
        }
        return -1;
    }

    @Override
    public int indexOf(CharSequence charSequence, int fromIndex) {
        if (charSequence.length() == 0) {
            return fromIndex;
        }
        if (fromIndex >= this.index) {
            return -1;
        }
        char first = charSequence.charAt(0);
        int max = this.index - charSequence.length();
        for (int i = fromIndex; i <= max; ++i) {
            if (this.chars[i] != first) {
                while (++i <= max && this.chars[i] != first) {
                }
            }
            if (i > max) continue;
            int j = i + 1;
            int end = j + charSequence.length() - 1;
            int k = 1;
            while (j < end && this.chars[j] == charSequence.charAt(k)) {
                ++j;
                ++k;
            }
            if (j != end) continue;
            return i;
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return this.index > this.whitespaceCount;
    }

    @Override
    public int lastIndexOf(char ch) {
        for (int x = this.index - this.whitespaceCount - 1; x >= 0; --x) {
            if (this.chars[x] != ch) continue;
            return x;
        }
        return -1;
    }
}

