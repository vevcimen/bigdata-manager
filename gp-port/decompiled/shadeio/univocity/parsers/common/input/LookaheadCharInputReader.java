/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input;

import java.io.Reader;
import java.util.Arrays;
import shadeio.univocity.parsers.common.input.CharInputReader;
import shadeio.univocity.parsers.common.input.DefaultCharInputReader;
import shadeio.univocity.parsers.common.input.EOFException;

public class LookaheadCharInputReader
implements CharInputReader {
    private final CharInputReader reader;
    private char[] lookahead = new char[0];
    private int length = 0;
    private int start = 0;
    private final char newLine;
    private char delimiter;
    private final int whitespaceRangeStart;

    public LookaheadCharInputReader(CharInputReader reader, char newLine, int whitespaceRangeStart) {
        this.reader = reader;
        this.newLine = newLine;
        this.whitespaceRangeStart = whitespaceRangeStart;
    }

    public boolean matches(char current, char[] sequence, char wildcard) {
        if (sequence.length > this.length - this.start) {
            return false;
        }
        if (sequence[0] != current && sequence[0] != wildcard) {
            return false;
        }
        for (int i = 1; i < sequence.length; ++i) {
            char ch = sequence[i];
            if (ch == wildcard || ch == this.lookahead[i - 1 + this.start]) continue;
            return false;
        }
        return true;
    }

    public boolean matches(char[] sequence, char wildcard) {
        if (sequence.length > this.length - this.start) {
            return false;
        }
        for (int i = 0; i < sequence.length; ++i) {
            char ch = sequence[i];
            if (ch == wildcard || sequence[i] == this.lookahead[i + this.start]) continue;
            return false;
        }
        return true;
    }

    public String getLookahead() {
        if (this.start >= this.length) {
            return "";
        }
        return new String(this.lookahead, this.start, this.length);
    }

    public String getLookahead(char current) {
        if (this.start >= this.length) {
            return String.valueOf(current);
        }
        return current + new String(this.lookahead, this.start, this.length - 1);
    }

    public void lookahead(int numberOfCharacters) {
        if (this.lookahead.length < (numberOfCharacters += this.length - this.start)) {
            this.lookahead = Arrays.copyOf(this.lookahead, numberOfCharacters);
        }
        if (this.start >= this.length) {
            this.start = 0;
            this.length = 0;
        }
        try {
            numberOfCharacters -= this.length;
            while (numberOfCharacters-- > 0) {
                this.lookahead[this.length] = this.reader.nextChar();
                ++this.length;
            }
        }
        catch (EOFException eOFException) {
            // empty catch block
        }
    }

    @Override
    public void start(Reader reader) {
        this.reader.start(reader);
    }

    @Override
    public void stop() {
        this.reader.stop();
    }

    @Override
    public char nextChar() {
        if (this.start >= this.length) {
            return this.reader.nextChar();
        }
        return this.lookahead[this.start++];
    }

    @Override
    public long charCount() {
        return this.reader.charCount();
    }

    @Override
    public long lineCount() {
        return this.reader.lineCount();
    }

    @Override
    public void skipLines(long lineCount) {
        this.reader.skipLines(lineCount);
    }

    @Override
    public void enableNormalizeLineEndings(boolean escaping) {
        this.reader.enableNormalizeLineEndings(escaping);
    }

    @Override
    public String readComment() {
        return this.reader.readComment();
    }

    @Override
    public char[] getLineSeparator() {
        return this.reader.getLineSeparator();
    }

    @Override
    public final char getChar() {
        if (this.start != 0 && this.start >= this.length) {
            return this.reader.getChar();
        }
        return this.lookahead[this.start - 1];
    }

    @Override
    public char skipWhitespace(char ch, char stopChar1, char stopChar2) {
        while (this.start < this.length && ch <= ' ' && ch != stopChar1 && ch != this.newLine && ch != stopChar2 && this.whitespaceRangeStart < ch) {
            ch = this.lookahead[this.start++];
        }
        return this.reader.skipWhitespace(ch, stopChar1, stopChar2);
    }

    @Override
    public String currentParsedContent() {
        return this.reader.currentParsedContent();
    }

    @Override
    public void markRecordStart() {
        this.reader.markRecordStart();
    }

    @Override
    public String getString(char ch, char stop, boolean trim, String nullValue, int maxLength) {
        return this.reader.getString(ch, stop, trim, nullValue, maxLength);
    }

    @Override
    public String getQuotedString(char quote, char escape, char escapeEscape, int maxLength, char stop1, char stop2, boolean keepQuotes, boolean keepEscape, boolean trimLeading, boolean trimTrailing) {
        return this.reader.getQuotedString(quote, escape, escapeEscape, maxLength, stop1, stop2, keepQuotes, keepEscape, trimLeading, trimTrailing);
    }

    @Override
    public int currentParsedContentLength() {
        return this.reader.currentParsedContentLength();
    }

    @Override
    public boolean skipString(char ch, char stop) {
        return this.reader.skipString(ch, stop);
    }

    @Override
    public boolean skipQuotedString(char quote, char escape, char stop1, char stop2) {
        return this.reader.skipQuotedString(quote, escape, stop1, stop2);
    }

    @Override
    public int lastIndexOf(char ch) {
        return this.reader.lastIndexOf(ch);
    }

    public void reloadBuffer() {
        if (this.reader instanceof DefaultCharInputReader) {
            ((DefaultCharInputReader)this.reader).reloadBuffer();
        }
    }
}

