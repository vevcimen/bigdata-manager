/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.tsv;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import shadeio.univocity.parsers.common.AbstractWriter;
import shadeio.univocity.parsers.tsv.TsvFormat;
import shadeio.univocity.parsers.tsv.TsvWriterSettings;

public class TsvWriter
extends AbstractWriter<TsvWriterSettings> {
    private boolean joinLines;
    private char escapeChar;
    private char escapedTabChar;
    private char newLine;

    public TsvWriter(TsvWriterSettings settings) {
        this((Writer)null, settings);
    }

    public TsvWriter(Writer writer, TsvWriterSettings settings) {
        super(writer, settings);
    }

    public TsvWriter(File file, TsvWriterSettings settings) {
        super(file, settings);
    }

    public TsvWriter(File file, String encoding, TsvWriterSettings settings) {
        super(file, encoding, settings);
    }

    public TsvWriter(File file, Charset encoding, TsvWriterSettings settings) {
        super(file, encoding, settings);
    }

    public TsvWriter(OutputStream output, TsvWriterSettings settings) {
        super(output, settings);
    }

    public TsvWriter(OutputStream output, String encoding, TsvWriterSettings settings) {
        super(output, encoding, settings);
    }

    public TsvWriter(OutputStream output, Charset encoding, TsvWriterSettings settings) {
        super(output, encoding, settings);
    }

    @Override
    protected final void initialize(TsvWriterSettings settings) {
        this.escapeChar = ((TsvFormat)settings.getFormat()).getEscapeChar();
        this.escapedTabChar = ((TsvFormat)settings.getFormat()).getEscapedTabChar();
        this.joinLines = settings.isLineJoiningEnabled();
        this.newLine = ((TsvFormat)settings.getFormat()).getNormalizedNewline();
    }

    @Override
    protected void processRow(Object[] row) {
        for (int i = 0; i < row.length; ++i) {
            if (i != 0) {
                this.appendToRow('\t');
            }
            String nextElement = this.getStringValue(row[i]);
            boolean allowTrim = this.allowTrim(i);
            int originalLength = this.appender.length();
            this.append(nextElement, allowTrim);
            if (this.appender.length() == originalLength && this.nullValue != null && !this.nullValue.isEmpty()) {
                this.append(this.nullValue, allowTrim);
            }
            this.appendValueToRow();
        }
    }

    private void append(String element, boolean allowTrim) {
        int i;
        if (element == null) {
            element = this.nullValue;
        }
        if (element == null) {
            return;
        }
        int start = 0;
        if (allowTrim && this.ignoreLeading) {
            start = TsvWriter.skipLeadingWhitespace(this.whitespaceRangeStart, element);
        }
        int length = element.length();
        char ch = '\u0000';
        for (i = start; i < length; ++i) {
            ch = element.charAt(i);
            if (ch != '\t' && ch != '\n' && ch != '\r' && ch != '\\') continue;
            this.appender.append(element, start, i);
            start = i + 1;
            this.appender.append(this.escapeChar);
            if (ch == '\t') {
                this.appender.append(this.escapedTabChar);
                continue;
            }
            if (ch == '\n') {
                this.appender.append(this.joinLines ? (char)this.newLine : (char)'n');
                continue;
            }
            if (ch == '\\') {
                this.appender.append('\\');
                continue;
            }
            this.appender.append(this.joinLines ? (char)this.newLine : (char)'r');
        }
        this.appender.append(element, start, i);
        if (allowTrim && ch <= ' ' && this.ignoreTrailing && this.whitespaceRangeStart < ch) {
            this.appender.updateWhitespace();
        }
    }
}

