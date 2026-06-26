/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.fixed;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import shadeio.univocity.parsers.common.AbstractWriter;
import shadeio.univocity.parsers.common.TextWritingException;
import shadeio.univocity.parsers.fixed.FieldAlignment;
import shadeio.univocity.parsers.fixed.FixedWidthFormat;
import shadeio.univocity.parsers.fixed.FixedWidthWriterSettings;
import shadeio.univocity.parsers.fixed.Lookup;

public class FixedWidthWriter
extends AbstractWriter<FixedWidthWriterSettings> {
    private int[] fieldLengths;
    private FieldAlignment[] fieldAlignments;
    private char[] fieldPaddings;
    private char padding;
    private char defaultPadding;
    private int length;
    private FieldAlignment alignment;
    private Lookup[] lookaheadFormats;
    private Lookup[] lookbehindFormats;
    private char[] lookupChars;
    private Lookup lookbehindFormat;
    private int[] rootLengths;
    private FieldAlignment[] rootAlignments;
    private boolean[] ignore;
    private boolean[] rootIgnore;
    private int ignoreCount;
    private char[] rootPaddings;
    private boolean defaultHeaderPadding;
    private FieldAlignment defaultHeaderAlignment;

    public FixedWidthWriter(FixedWidthWriterSettings settings) {
        this((Writer)null, settings);
    }

    public FixedWidthWriter(Writer writer, FixedWidthWriterSettings settings) {
        super(writer, settings);
    }

    public FixedWidthWriter(File file, FixedWidthWriterSettings settings) {
        super(file, settings);
    }

    public FixedWidthWriter(File file, String encoding, FixedWidthWriterSettings settings) {
        super(file, encoding, settings);
    }

    public FixedWidthWriter(File file, Charset encoding, FixedWidthWriterSettings settings) {
        super(file, encoding, settings);
    }

    public FixedWidthWriter(OutputStream output, FixedWidthWriterSettings settings) {
        super(output, settings);
    }

    public FixedWidthWriter(OutputStream output, String encoding, FixedWidthWriterSettings settings) {
        super(output, encoding, settings);
    }

    public FixedWidthWriter(OutputStream output, Charset encoding, FixedWidthWriterSettings settings) {
        super(output, encoding, settings);
    }

    @Override
    protected final void initialize(FixedWidthWriterSettings settings) {
        FixedWidthFormat format = (FixedWidthFormat)settings.getFormat();
        this.defaultPadding = this.padding = format.getPadding();
        this.fieldLengths = settings.getAllLengths();
        this.fieldAlignments = settings.getFieldAlignments();
        this.fieldPaddings = settings.getFieldPaddings();
        this.ignore = settings.getFieldsToIgnore();
        if (this.ignore != null) {
            for (int i = 0; i < this.ignore.length; ++i) {
                if (!this.ignore[i]) continue;
                ++this.ignoreCount;
            }
        }
        this.lookaheadFormats = settings.getLookaheadFormats();
        this.lookbehindFormats = settings.getLookbehindFormats();
        this.defaultHeaderPadding = settings.getUseDefaultPaddingForHeaders();
        this.defaultHeaderAlignment = settings.getDefaultAlignmentForHeaders();
        super.enableNewlineAfterRecord(settings.getWriteLineSeparatorAfterRecord());
        if (this.lookaheadFormats != null || this.lookbehindFormats != null) {
            this.lookupChars = new char[Lookup.calculateMaxLookupLength(this.lookaheadFormats, this.lookbehindFormats)];
            this.rootLengths = this.fieldLengths;
            this.rootAlignments = this.fieldAlignments;
            this.rootPaddings = this.fieldPaddings;
            this.rootIgnore = this.ignore;
        } else {
            this.lookupChars = null;
            this.rootLengths = null;
            this.rootAlignments = null;
            this.rootPaddings = null;
            this.rootIgnore = null;
        }
    }

    @Override
    protected void processRow(Object[] row) {
        int i;
        if (row.length > 0 && this.lookaheadFormats != null || this.lookbehindFormats != null) {
            int i2;
            int len;
            int dstBegin = 0;
            for (i2 = 0; i2 < row.length && dstBegin < this.lookupChars.length; dstBegin += len, ++i2) {
                String value = String.valueOf(row[i2]);
                len = value.length();
                if (dstBegin + len > this.lookupChars.length) {
                    len = this.lookupChars.length - dstBegin;
                }
                value.getChars(0, len, this.lookupChars, dstBegin);
            }
            for (i2 = this.lookupChars.length - 1; i2 > dstBegin; --i2) {
                this.lookupChars[i2] = '\u0000';
            }
            boolean matched = false;
            if (this.lookaheadFormats != null) {
                for (i = 0; i < this.lookaheadFormats.length; ++i) {
                    if (!this.lookaheadFormats[i].matches(this.lookupChars)) continue;
                    this.fieldLengths = this.lookaheadFormats[i].lengths;
                    this.fieldAlignments = this.lookaheadFormats[i].alignments;
                    this.fieldPaddings = this.lookaheadFormats[i].paddings;
                    this.ignore = this.lookaheadFormats[i].ignore;
                    matched = true;
                    break;
                }
                if (this.lookbehindFormats != null && matched) {
                    this.lookbehindFormat = null;
                    for (i = 0; i < this.lookbehindFormats.length; ++i) {
                        if (!this.lookbehindFormats[i].matches(this.lookupChars)) continue;
                        this.lookbehindFormat = this.lookbehindFormats[i];
                        break;
                    }
                }
            } else {
                for (i = 0; i < this.lookbehindFormats.length; ++i) {
                    if (!this.lookbehindFormats[i].matches(this.lookupChars)) continue;
                    this.lookbehindFormat = this.lookbehindFormats[i];
                    matched = true;
                    this.fieldLengths = this.rootLengths;
                    this.fieldAlignments = this.rootAlignments;
                    this.fieldPaddings = this.rootPaddings;
                    this.ignore = this.rootIgnore;
                    break;
                }
            }
            if (!matched) {
                if (this.lookbehindFormat == null) {
                    if (this.rootLengths == null) {
                        throw new TextWritingException("Cannot write with the given configuration. No default field lengths defined and no lookahead/lookbehind value match '" + new String(this.lookupChars) + '\'', this.getRecordCount(), row);
                    }
                    this.fieldLengths = this.rootLengths;
                    this.fieldAlignments = this.rootAlignments;
                    this.fieldPaddings = this.rootPaddings;
                    this.ignore = this.rootIgnore;
                } else {
                    this.fieldLengths = this.lookbehindFormat.lengths;
                    this.fieldAlignments = this.lookbehindFormat.alignments;
                    this.fieldPaddings = this.lookbehindFormat.paddings;
                    this.ignore = this.lookbehindFormat.ignore;
                }
            }
        }
        if (this.expandRows) {
            row = this.expand(row, this.fieldLengths.length - this.ignoreCount, null);
        }
        int lastIndex = this.fieldLengths.length < row.length ? this.fieldLengths.length : row.length;
        int off = 0;
        for (i = 0; i < lastIndex + off; ++i) {
            this.length = this.fieldLengths[i];
            if (this.ignore[i]) {
                ++off;
                this.appender.fill(' ', this.length);
                continue;
            }
            this.alignment = this.fieldAlignments[i];
            this.padding = this.fieldPaddings[i];
            if (this.writingHeaders) {
                if (this.defaultHeaderPadding) {
                    this.padding = this.defaultPadding;
                }
                if (this.defaultHeaderAlignment != null) {
                    this.alignment = this.defaultHeaderAlignment;
                }
            }
            String nextElement = this.getStringValue(row[i - off]);
            boolean allowTrim = this.allowTrim(i);
            this.processElement(nextElement, allowTrim);
            this.appendValueToRow();
        }
    }

    private void append(String element, boolean allowTrim) {
        int start = 0;
        if (allowTrim && this.ignoreLeading) {
            start = FixedWidthWriter.skipLeadingWhitespace(this.whitespaceRangeStart, element);
        }
        int padCount = this.alignment.calculatePadding(this.length, element.length() - start);
        this.length -= padCount;
        this.appender.fill(this.padding, padCount);
        if (allowTrim && this.ignoreTrailing) {
            int i = start;
            while (i < element.length() && this.length > 0) {
                while (i < element.length() && this.length-- > 0) {
                    char nextChar = element.charAt(i);
                    this.appender.appendIgnoringWhitespace(nextChar);
                    ++i;
                }
                if (this.length == -1 && this.appender.whitespaceCount() > 0) {
                    for (int j = i; j < element.length(); ++j) {
                        if (element.charAt(j) <= ' ') continue;
                        this.appender.resetWhitespaceCount();
                        break;
                    }
                    if (this.appender.whitespaceCount() > 0) {
                        this.length = 0;
                    }
                }
                this.length += this.appender.whitespaceCount();
                this.appendValueToRow();
            }
        } else {
            for (int i = start; i < element.length() && this.length-- > 0; ++i) {
                char nextChar = element.charAt(i);
                this.appender.append(nextChar);
            }
        }
    }

    private void processElement(String element, boolean allowTrim) {
        if (element != null) {
            this.append(element, allowTrim);
        }
        this.appender.fill(this.padding, this.length);
    }
}

