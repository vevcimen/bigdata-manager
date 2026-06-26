/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.csv;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import shadeio.univocity.parsers.common.AbstractWriter;
import shadeio.univocity.parsers.common.fields.FieldSelector;
import shadeio.univocity.parsers.csv.CsvFormat;
import shadeio.univocity.parsers.csv.CsvWriterSettings;

public class CsvWriter
extends AbstractWriter<CsvWriterSettings> {
    private char delimiter;
    private char[] multiDelimiter;
    private char quoteChar;
    private char escapeChar;
    private char escapeEscape;
    private boolean quoteAllFields;
    private boolean escapeUnquoted;
    private boolean inputNotEscaped;
    private char newLine;
    private boolean dontProcessNormalizedNewLines;
    private boolean[] quotationTriggers;
    private char maxTrigger;
    private Set<Integer> quotedColumns;
    private FieldSelector quotedFieldSelector;
    private boolean quoteNulls;

    public CsvWriter(CsvWriterSettings settings) {
        this((Writer)null, settings);
    }

    public CsvWriter(Writer writer, CsvWriterSettings settings) {
        super(writer, settings);
    }

    public CsvWriter(File file, CsvWriterSettings settings) {
        super(file, settings);
    }

    public CsvWriter(File file, String encoding, CsvWriterSettings settings) {
        super(file, encoding, settings);
    }

    public CsvWriter(File file, Charset encoding, CsvWriterSettings settings) {
        super(file, encoding, settings);
    }

    public CsvWriter(OutputStream output, CsvWriterSettings settings) {
        super(output, settings);
    }

    public CsvWriter(OutputStream output, String encoding, CsvWriterSettings settings) {
        super(output, encoding, settings);
    }

    public CsvWriter(OutputStream output, Charset encoding, CsvWriterSettings settings) {
        super(output, encoding, settings);
    }

    @Override
    protected final void initialize(CsvWriterSettings settings) {
        int i;
        CsvFormat format = (CsvFormat)settings.getFormat();
        this.multiDelimiter = format.getDelimiterString().toCharArray();
        if (this.multiDelimiter.length == 1) {
            this.delimiter = this.multiDelimiter[0];
            this.multiDelimiter = null;
        }
        this.quoteChar = format.getQuote();
        this.escapeChar = format.getQuoteEscape();
        this.escapeEscape = ((CsvFormat)settings.getFormat()).getCharToEscapeQuoteEscaping();
        this.newLine = format.getNormalizedNewline();
        this.quoteAllFields = settings.getQuoteAllFields();
        this.quoteNulls = settings.getQuoteNulls();
        this.escapeUnquoted = settings.isEscapeUnquotedValues();
        this.inputNotEscaped = !settings.isInputEscaped();
        this.dontProcessNormalizedNewLines = !settings.isNormalizeLineEndingsWithinQuotes();
        this.quotationTriggers = null;
        this.quotedColumns = null;
        this.maxTrigger = '\u0000';
        this.quotedColumns = Collections.emptySet();
        this.quotedFieldSelector = settings.getQuotedFieldSelector();
        char[] sep = format.getLineSeparator();
        int triggerCount = 3 + settings.getQuotationTriggers().length + sep.length;
        int offset = settings.isQuoteEscapingEnabled() ? 1 : 0;
        char[] tmp = Arrays.copyOf(settings.getQuotationTriggers(), triggerCount + offset);
        if (offset == 1) {
            tmp[triggerCount] = this.quoteChar;
        }
        tmp[triggerCount - 1] = 10;
        tmp[triggerCount - 2] = 13;
        tmp[triggerCount - 3] = this.newLine;
        tmp[triggerCount - 4] = sep[0];
        if (sep.length > 1) {
            tmp[triggerCount - 5] = sep[1];
        }
        for (i = 0; i < tmp.length; ++i) {
            if (this.maxTrigger >= tmp[i]) continue;
            this.maxTrigger = tmp[i];
        }
        if (this.maxTrigger != '\u0000') {
            this.maxTrigger = (char)(this.maxTrigger + '\u0001');
            this.quotationTriggers = new boolean[this.maxTrigger];
            Arrays.fill(this.quotationTriggers, false);
            for (i = 0; i < tmp.length; ++i) {
                this.quotationTriggers[tmp[i]] = true;
            }
        }
    }

    @Override
    protected void processRow(Object[] row) {
        int[] quotedIndexes;
        if (this.recordCount == 0L && this.quotedFieldSelector != null && (quotedIndexes = this.quotedFieldSelector.getFieldIndexes(this.headers)).length > 0) {
            this.quotedColumns = new HashSet<Integer>();
            for (int idx : quotedIndexes) {
                this.quotedColumns.add(idx);
            }
        }
        for (int i = 0; i < row.length; ++i) {
            boolean isElementQuoted;
            if (i != 0) {
                if (this.multiDelimiter == null) {
                    this.appendToRow(this.delimiter);
                } else {
                    this.appendToRow(this.multiDelimiter);
                }
            }
            if (this.dontProcessNormalizedNewLines) {
                this.appender.enableDenormalizedLineEndings(false);
            }
            boolean allowTrim = this.allowTrim(i);
            String nextElement = this.getStringValue(row[i]);
            boolean quoteOn = this.quoteNulls || row[i] != null;
            int originalLength = this.appender.length();
            boolean bl = isElementQuoted = this.append(i, quoteOn && (this.quoteAllFields || this.quotedColumns.contains(i)), allowTrim, nextElement) && quoteOn;
            if (this.appender.length() == originalLength && !this.usingNullOrEmptyValue) {
                if (isElementQuoted) {
                    if (nextElement == null) {
                        this.append(i, false, allowTrim, this.nullValue);
                    } else {
                        this.append(i, true, allowTrim, this.emptyValue);
                    }
                } else if (nextElement == null) {
                    this.append(i, false, allowTrim, this.nullValue);
                } else {
                    this.append(i, false, allowTrim, this.emptyValue);
                }
            }
            if (isElementQuoted) {
                this.appendToRow(this.quoteChar);
                this.appendValueToRow();
                this.appendToRow(this.quoteChar);
                if (!this.dontProcessNormalizedNewLines) continue;
                this.appender.enableDenormalizedLineEndings(true);
                continue;
            }
            this.appendValueToRow();
        }
    }

    private boolean matchMultiDelimiter(String element, int from) {
        if (from + this.multiDelimiter.length - 2 >= element.length()) {
            return false;
        }
        int j = 1;
        while (j < this.multiDelimiter.length) {
            if (element.charAt(from) != this.multiDelimiter[j]) {
                return false;
            }
            ++j;
            ++from;
        }
        return true;
    }

    private boolean quoteElement(int start, String element) {
        int length = element.length();
        if (this.multiDelimiter == null) {
            if (this.maxTrigger == '\u0000') {
                for (int i = start; i < length; ++i) {
                    char nextChar = element.charAt(i);
                    if (nextChar != this.delimiter && nextChar != this.newLine) continue;
                    return true;
                }
            } else {
                for (int i = start; i < length; ++i) {
                    char nextChar = element.charAt(i);
                    if (nextChar != this.delimiter && (nextChar >= this.maxTrigger || !this.quotationTriggers[nextChar])) continue;
                    return true;
                }
            }
        } else if (this.maxTrigger == '\u0000') {
            for (int i = start; i < length; ++i) {
                char nextChar = element.charAt(i);
                if ((nextChar != this.multiDelimiter[0] || !this.matchMultiDelimiter(element, i + 1)) && nextChar != this.newLine) continue;
                return true;
            }
        } else {
            for (int i = start; i < length; ++i) {
                char nextChar = element.charAt(i);
                if ((nextChar != this.multiDelimiter[0] || !this.matchMultiDelimiter(element, i + 1)) && (nextChar >= this.maxTrigger || !this.quotationTriggers[nextChar])) continue;
                return true;
            }
        }
        return false;
    }

    private boolean append(int columnIndex, boolean isElementQuoted, boolean allowTrim, String element) {
        int i;
        int length;
        if (element == null) {
            if (this.nullValue == null) {
                return isElementQuoted;
            }
            element = this.nullValue;
        }
        int start = 0;
        if (allowTrim && this.ignoreLeading) {
            start = CsvWriter.skipLeadingWhitespace(this.whitespaceRangeStart, element);
        }
        if (start < (length = element.length()) && (element.charAt(start) == this.quoteChar || columnIndex == 0 && element.charAt(0) == this.comment)) {
            isElementQuoted = true;
        }
        if (isElementQuoted) {
            if (this.usingNullOrEmptyValue && length >= 2) {
                if (element.charAt(0) == this.quoteChar && element.charAt(length - 1) == this.quoteChar) {
                    this.appender.append(element);
                    return false;
                }
                this.appendQuoted(start, allowTrim, element);
                return true;
            }
            this.appendQuoted(start, allowTrim, element);
            return true;
        }
        char ch = '\u0000';
        if (this.multiDelimiter == null) {
            for (i = start; i < length; ++i) {
                ch = element.charAt(i);
                if (ch != this.quoteChar && ch != this.delimiter && ch != this.escapeChar && (ch >= this.maxTrigger || !this.quotationTriggers[ch])) continue;
                this.appender.append(element, start, i);
                start = i + 1;
                if (ch == this.quoteChar || ch == this.escapeChar) {
                    if (this.quoteElement(i, element)) {
                        this.appendQuoted(i, allowTrim, element);
                        return true;
                    }
                    if (this.escapeUnquoted) {
                        this.appendQuoted(i, allowTrim, element);
                    } else {
                        this.appender.append(element, i, length);
                        if (allowTrim && this.ignoreTrailing && element.charAt(length - 1) <= ' ' && this.whitespaceRangeStart < element.charAt(length - 1)) {
                            this.appender.updateWhitespace();
                        }
                    }
                    return isElementQuoted;
                }
                if (ch == this.escapeChar && this.inputNotEscaped && this.escapeEscape != '\u0000' && this.escapeUnquoted) {
                    this.appender.append(this.escapeEscape);
                } else if (ch == this.delimiter || ch < this.maxTrigger && this.quotationTriggers[ch]) {
                    this.appendQuoted(i, allowTrim, element);
                    return true;
                }
                this.appender.append(ch);
            }
        } else {
            while (i < length) {
                ch = element.charAt(i);
                if (ch == this.quoteChar || ch == this.multiDelimiter[0] && this.matchMultiDelimiter(element, i + 1) || ch == this.escapeChar || ch < this.maxTrigger && this.quotationTriggers[ch]) {
                    this.appender.append(element, start, i);
                    start = i + 1;
                    if (ch == this.quoteChar || ch == this.escapeChar) {
                        if (this.quoteElement(i, element)) {
                            this.appendQuoted(i, allowTrim, element);
                            return true;
                        }
                        if (this.escapeUnquoted) {
                            this.appendQuoted(i, allowTrim, element);
                        } else {
                            this.appender.append(element, i, length);
                            if (allowTrim && this.ignoreTrailing && element.charAt(length - 1) <= ' ' && this.whitespaceRangeStart < element.charAt(length - 1)) {
                                this.appender.updateWhitespace();
                            }
                        }
                        return isElementQuoted;
                    }
                    if (ch == this.escapeChar && this.inputNotEscaped && this.escapeEscape != '\u0000' && this.escapeUnquoted) {
                        this.appender.append(this.escapeEscape);
                    } else if (ch == this.multiDelimiter[0] && this.matchMultiDelimiter(element, i + 1) || ch < this.maxTrigger && this.quotationTriggers[ch]) {
                        this.appendQuoted(i, allowTrim, element);
                        return true;
                    }
                    this.appender.append(ch);
                }
                ++i;
            }
        }
        this.appender.append(element, start, i);
        if (allowTrim && ch <= ' ' && this.ignoreTrailing && this.whitespaceRangeStart < ch) {
            this.appender.updateWhitespace();
        }
        return isElementQuoted;
    }

    private void appendQuoted(int start, boolean allowTrim, String element) {
        int i;
        int length = element.length();
        char ch = '\u0000';
        for (i = start; i < length; ++i) {
            ch = element.charAt(i);
            if (ch != this.quoteChar && ch != this.newLine && ch != this.escapeChar) continue;
            this.appender.append(element, start, i);
            start = i + 1;
            if (ch == this.quoteChar && this.inputNotEscaped) {
                this.appender.append(this.escapeChar);
            } else if (ch == this.escapeChar && this.inputNotEscaped && this.escapeEscape != '\u0000') {
                this.appender.append(this.escapeEscape);
            }
            this.appender.append(ch);
        }
        this.appender.append(element, start, i);
        if (allowTrim && ch <= ' ' && this.ignoreTrailing && this.whitespaceRangeStart < ch) {
            this.appender.updateWhitespace();
        }
    }
}

