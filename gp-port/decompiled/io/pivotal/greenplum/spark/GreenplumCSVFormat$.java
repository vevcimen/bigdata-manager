/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.runtime.Null$
 */
package io.pivotal.greenplum.spark;

import scala.runtime.Null$;
import shadeio.univocity.parsers.csv.CsvFormat;
import shadeio.univocity.parsers.csv.CsvParserSettings;
import shadeio.univocity.parsers.csv.UnescapedQuoteHandling;

public final class GreenplumCSVFormat$ {
    public static GreenplumCSVFormat$ MODULE$;
    private CsvParserSettings DEFAULT;
    private final char ESCAPE;
    private final char CHAR_DELIMITER;
    private final String NEWLINE;
    private final char QUOTE;
    private final String QUOTE_STRING;
    private final String VALUE_OF_NULL;
    private final String EMPTY_VALUE;
    private final Null$ NULL_VALUE;
    private final String DEFAULT_ENCODING;
    private final boolean STRICT_QUOTES;
    private final boolean IGNORE_WHITESPACE;
    private final String ESCAPED_QUOTE;
    private final int inputBufferSize;
    private final int maxCharsPerColumn;
    private final int maxErrorContentLength;
    private volatile boolean bitmap$0;

    static {
        new GreenplumCSVFormat$();
    }

    public char ESCAPE() {
        return this.ESCAPE;
    }

    public char CHAR_DELIMITER() {
        return this.CHAR_DELIMITER;
    }

    public String NEWLINE() {
        return this.NEWLINE;
    }

    public char QUOTE() {
        return this.QUOTE;
    }

    public String QUOTE_STRING() {
        return this.QUOTE_STRING;
    }

    public String VALUE_OF_NULL() {
        return this.VALUE_OF_NULL;
    }

    public String EMPTY_VALUE() {
        return this.EMPTY_VALUE;
    }

    public Null$ NULL_VALUE() {
        return this.NULL_VALUE;
    }

    public String DEFAULT_ENCODING() {
        return this.DEFAULT_ENCODING;
    }

    public boolean STRICT_QUOTES() {
        return this.STRICT_QUOTES;
    }

    public boolean IGNORE_WHITESPACE() {
        return this.IGNORE_WHITESPACE;
    }

    public String ESCAPED_QUOTE() {
        return this.ESCAPED_QUOTE;
    }

    public int inputBufferSize() {
        return this.inputBufferSize;
    }

    public int maxCharsPerColumn() {
        return this.maxCharsPerColumn;
    }

    public int maxErrorContentLength() {
        return this.maxErrorContentLength;
    }

    private CsvParserSettings DEFAULT$lzycompute() {
        GreenplumCSVFormat$ greenplumCSVFormat$ = this;
        synchronized (greenplumCSVFormat$) {
            if (!this.bitmap$0) {
                this.DEFAULT = this.getDefault();
                this.bitmap$0 = true;
            }
        }
        return this.DEFAULT;
    }

    public CsvParserSettings DEFAULT() {
        if (!this.bitmap$0) {
            return this.DEFAULT$lzycompute();
        }
        return this.DEFAULT;
    }

    private CsvParserSettings getDefault() {
        CsvParserSettings settings = new CsvParserSettings();
        CsvFormat format = (CsvFormat)settings.getFormat();
        this.NULL_VALUE();
        settings.setNullValue(null);
        settings.setEmptyValue(this.EMPTY_VALUE());
        format.setDelimiter(this.CHAR_DELIMITER());
        format.setQuote(this.QUOTE());
        format.setQuoteEscape(this.QUOTE());
        settings.setIgnoreLeadingWhitespacesInQuotes(this.IGNORE_WHITESPACE());
        settings.setIgnoreTrailingWhitespacesInQuotes(this.IGNORE_WHITESPACE());
        settings.setIgnoreLeadingWhitespaces(this.IGNORE_WHITESPACE());
        settings.setIgnoreTrailingWhitespaces(this.IGNORE_WHITESPACE());
        settings.setReadInputOnSeparateThread(false);
        settings.setInputBufferSize(this.inputBufferSize());
        settings.setMaxCharsPerColumn(this.maxCharsPerColumn());
        settings.setUnescapedQuoteHandling(UnescapedQuoteHandling.STOP_AT_DELIMITER);
        settings.setLineSeparatorDetectionEnabled(true);
        settings.setCommentProcessingEnabled(false);
        return settings;
    }

    private GreenplumCSVFormat$() {
        MODULE$ = this;
        this.ESCAPE = (char)92;
        this.CHAR_DELIMITER = (char)124;
        this.NEWLINE = "\n";
        this.QUOTE = (char)34;
        this.QUOTE_STRING = "\"";
        this.VALUE_OF_NULL = "";
        this.EMPTY_VALUE = "";
        this.NULL_VALUE = null;
        this.DEFAULT_ENCODING = "UTF-8";
        this.STRICT_QUOTES = true;
        this.IGNORE_WHITESPACE = false;
        this.ESCAPED_QUOTE = "\"\"";
        this.inputBufferSize = 128;
        this.maxCharsPerColumn = -1;
        this.maxErrorContentLength = 1000;
    }
}

