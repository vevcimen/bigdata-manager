/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input;

import java.io.Reader;
import shadeio.univocity.parsers.common.input.CharInput;

public interface CharInputReader
extends CharInput {
    public void start(Reader var1);

    public void stop();

    @Override
    public char nextChar();

    @Override
    public char getChar();

    public long charCount();

    public long lineCount();

    public void skipLines(long var1);

    public String readComment();

    public void enableNormalizeLineEndings(boolean var1);

    public char[] getLineSeparator();

    public char skipWhitespace(char var1, char var2, char var3);

    public int currentParsedContentLength();

    public String currentParsedContent();

    public int lastIndexOf(char var1);

    public void markRecordStart();

    public String getString(char var1, char var2, boolean var3, String var4, int var5);

    public boolean skipString(char var1, char var2);

    public String getQuotedString(char var1, char var2, char var3, int var4, char var5, char var6, boolean var7, boolean var8, boolean var9, boolean var10);

    public boolean skipQuotedString(char var1, char var2, char var3, char var4);
}

