/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import java.util.Map;
import shadeio.univocity.parsers.common.Context;

public interface ParsingContext
extends Context {
    @Override
    public String[] headers();

    @Override
    public int[] extractedFieldIndexes();

    @Override
    public boolean columnsReordered();

    public long currentLine();

    public long currentChar();

    public void skipLines(long var1);

    public String[] parsedHeaders();

    public String currentParsedContent();

    public int currentParsedContentLength();

    public String fieldContentOnError();

    public Map<Long, String> comments();

    public String lastComment();

    public char[] lineSeparator();
}

