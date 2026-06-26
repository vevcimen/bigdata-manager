/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input;

import shadeio.univocity.parsers.common.input.CharInput;

public interface CharAppender
extends CharSequence {
    public void appendIgnoringWhitespace(char var1);

    public void appendIgnoringPadding(char var1, char var2);

    public void appendIgnoringWhitespaceAndPadding(char var1, char var2);

    public void append(char var1);

    public int indexOf(char var1, int var2);

    public int indexOf(char[] var1, int var2);

    public int indexOf(CharSequence var1, int var2);

    public int indexOfAny(char[] var1, int var2);

    public String substring(int var1, int var2);

    public void remove(int var1, int var2);

    public void append(int var1);

    public void append(Object var1);

    @Override
    public int length();

    public int whitespaceCount();

    public void resetWhitespaceCount();

    public String getAndReset();

    public void reset();

    public char[] getCharsAndReset();

    public char[] getChars();

    public void fill(char var1, int var2);

    public void prepend(char var1);

    public void prepend(char var1, char var2);

    public void prepend(char[] var1);

    public void updateWhitespace();

    public char appendUntil(char var1, CharInput var2, char var3);

    public char appendUntil(char var1, CharInput var2, char var3, char var4);

    public char appendUntil(char var1, CharInput var2, char var3, char var4, char var5);

    public void append(char[] var1, int var2, int var3);

    public void append(char[] var1);

    public void append(int[] var1);

    public void append(String var1);

    public void append(String var1, int var2, int var3);

    public void ignore(int var1);

    public void delete(int var1);

    @Override
    public boolean isEmpty();

    public int lastIndexOf(char var1);
}

