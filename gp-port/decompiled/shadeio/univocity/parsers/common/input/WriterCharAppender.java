/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input;

import java.io.IOException;
import java.io.Writer;
import shadeio.univocity.parsers.common.Format;
import shadeio.univocity.parsers.common.input.ExpandingCharAppender;

public class WriterCharAppender
extends ExpandingCharAppender {
    private final char lineSeparator1;
    private final char lineSeparator2;
    private final char newLine;
    private boolean denormalizeLineEndings = true;

    public WriterCharAppender(int maxLength, String emptyValue, int whitespaceRangeStart, Format format) {
        super(maxLength == -1 ? 8192 : maxLength, emptyValue, whitespaceRangeStart);
        char[] lineSeparator = format.getLineSeparator();
        this.lineSeparator1 = lineSeparator[0];
        this.lineSeparator2 = lineSeparator.length > 1 ? lineSeparator[1] : (char)'\u0000';
        this.newLine = format.getNormalizedNewline();
    }

    @Override
    public final void appendIgnoringWhitespace(char ch) {
        if (ch == this.newLine && this.denormalizeLineEndings) {
            super.appendIgnoringWhitespace(this.lineSeparator1);
            if (this.lineSeparator2 != '\u0000') {
                super.appendIgnoringWhitespace(this.lineSeparator2);
            }
        } else {
            super.appendIgnoringWhitespace(ch);
        }
    }

    @Override
    public final void appendIgnoringPadding(char ch, char padding) {
        if (ch == this.newLine && this.denormalizeLineEndings) {
            super.appendIgnoringPadding(this.lineSeparator1, padding);
            if (this.lineSeparator2 != '\u0000') {
                super.appendIgnoringPadding(this.lineSeparator2, padding);
            }
        } else {
            super.appendIgnoringPadding(ch, padding);
        }
    }

    @Override
    public final void appendIgnoringWhitespaceAndPadding(char ch, char padding) {
        if (ch == this.newLine && this.denormalizeLineEndings) {
            super.appendIgnoringWhitespaceAndPadding(this.lineSeparator1, padding);
            if (this.lineSeparator2 != '\u0000') {
                super.appendIgnoringWhitespaceAndPadding(this.lineSeparator2, padding);
            }
        } else {
            super.appendIgnoringWhitespaceAndPadding(ch, padding);
        }
    }

    @Override
    public final void append(char ch) {
        if (ch == this.newLine && this.denormalizeLineEndings) {
            this.appendNewLine();
        } else {
            super.append(ch);
        }
    }

    public final void writeCharsAndReset(Writer writer) throws IOException {
        if (this.index - this.whitespaceCount > 0) {
            writer.write(this.chars, 0, this.index - this.whitespaceCount);
        } else if (this.emptyChars != null) {
            writer.write(this.emptyChars, 0, this.emptyChars.length);
        }
        this.index = 0;
        this.whitespaceCount = 0;
    }

    public final void appendNewLine() {
        if (this.index + 2 >= this.chars.length) {
            this.expand();
        }
        this.chars[this.index++] = this.lineSeparator1;
        if (this.lineSeparator2 != '\u0000') {
            this.chars[this.index++] = this.lineSeparator2;
        }
    }

    public final void enableDenormalizedLineEndings(boolean enableDenormalizedLineEndings) {
        this.denormalizeLineEndings = enableDenormalizedLineEndings;
    }
}

