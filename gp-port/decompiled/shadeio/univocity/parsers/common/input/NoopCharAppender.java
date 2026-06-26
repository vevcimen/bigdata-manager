/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input;

import shadeio.univocity.parsers.common.input.CharAppender;
import shadeio.univocity.parsers.common.input.CharInput;

public class NoopCharAppender
implements CharAppender {
    private static final NoopCharAppender instance = new NoopCharAppender();

    public static CharAppender getInstance() {
        return instance;
    }

    private NoopCharAppender() {
    }

    @Override
    public int length() {
        return -1;
    }

    @Override
    public String getAndReset() {
        return null;
    }

    @Override
    public void appendIgnoringWhitespace(char ch) {
    }

    @Override
    public void append(char ch) {
    }

    @Override
    public char[] getCharsAndReset() {
        return null;
    }

    @Override
    public int whitespaceCount() {
        return 0;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetWhitespaceCount() {
    }

    @Override
    public char[] getChars() {
        return null;
    }

    @Override
    public void fill(char ch, int length) {
    }

    @Override
    public void appendIgnoringPadding(char ch, char padding) {
    }

    @Override
    public void appendIgnoringWhitespaceAndPadding(char ch, char padding) {
    }

    @Override
    public void prepend(char ch) {
    }

    @Override
    public void updateWhitespace() {
    }

    @Override
    public char appendUntil(char ch, CharInput input, char stop) {
        while (ch != stop) {
            ch = input.nextChar();
        }
        return ch;
    }

    @Override
    public final char appendUntil(char ch, CharInput input, char stop1, char stop2) {
        while (ch != stop1 && ch != stop2) {
            ch = input.nextChar();
        }
        return ch;
    }

    @Override
    public final char appendUntil(char ch, CharInput input, char stop1, char stop2, char stop3) {
        while (ch != stop1 && ch != stop2 && ch != stop3) {
            ch = input.nextChar();
        }
        return ch;
    }

    @Override
    public void append(char[] ch, int from, int length) {
    }

    @Override
    public void prepend(char ch1, char ch2) {
    }

    @Override
    public void prepend(char[] chars) {
    }

    @Override
    public void append(char[] ch) {
    }

    @Override
    public void append(String string) {
    }

    @Override
    public void append(String string, int from, int to) {
    }

    @Override
    public char charAt(int i) {
        return '\u0000';
    }

    @Override
    public CharSequence subSequence(int i, int i1) {
        return null;
    }

    @Override
    public void append(int ch) {
    }

    @Override
    public void append(int[] ch) {
    }

    @Override
    public void append(Object obj) {
    }

    @Override
    public void ignore(int count) {
    }

    @Override
    public int indexOf(char ch, int from) {
        return -1;
    }

    @Override
    public String substring(int from, int length) {
        return null;
    }

    @Override
    public void remove(int from, int length) {
    }

    @Override
    public void delete(int count) {
    }

    @Override
    public int indexOfAny(char[] chars, int from) {
        return -1;
    }

    @Override
    public int indexOf(char[] charSequence, int from) {
        return -1;
    }

    @Override
    public int indexOf(CharSequence charSequence, int from) {
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int lastIndexOf(char ch) {
        return -1;
    }
}

