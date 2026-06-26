/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import shadeio.univocity.parsers.common.Format;
import shadeio.univocity.parsers.common.input.BomInput;
import shadeio.univocity.parsers.common.input.CharInputReader;
import shadeio.univocity.parsers.common.input.EOFException;
import shadeio.univocity.parsers.common.input.ExpandingCharAppender;
import shadeio.univocity.parsers.common.input.InputAnalysisProcess;
import shadeio.univocity.parsers.common.input.LineSeparatorDetector;

public abstract class AbstractCharInputReader
implements CharInputReader {
    private final ExpandingCharAppender tmp;
    private boolean lineSeparatorDetected;
    private final boolean detectLineSeparator;
    private List<InputAnalysisProcess> inputAnalysisProcesses;
    private char lineSeparator1;
    private char lineSeparator2;
    private final char normalizedLineSeparator;
    private long lineCount;
    private long charCount;
    private int recordStart;
    final int whitespaceRangeStart;
    private boolean skipping = false;
    private boolean commentProcessing = false;
    protected final boolean closeOnStop;
    public int i;
    private char ch;
    public char[] buffer;
    public int length = -1;
    private boolean incrementLineCount;
    private boolean normalizeLineEndings = true;

    public AbstractCharInputReader(char normalizedLineSeparator, int whitespaceRangeStart, boolean closeOnStop) {
        this(null, normalizedLineSeparator, whitespaceRangeStart, closeOnStop);
    }

    public AbstractCharInputReader(char[] lineSeparator, char normalizedLineSeparator, int whitespaceRangeStart, boolean closeOnStop) {
        this.whitespaceRangeStart = whitespaceRangeStart;
        this.tmp = new ExpandingCharAppender(4096, null, whitespaceRangeStart);
        if (lineSeparator == null) {
            this.detectLineSeparator = true;
            this.submitLineSeparatorDetector();
            this.lineSeparator1 = '\u0000';
            this.lineSeparator2 = '\u0000';
        } else {
            this.setLineSeparator(lineSeparator);
            this.detectLineSeparator = false;
        }
        this.normalizedLineSeparator = normalizedLineSeparator;
        this.closeOnStop = closeOnStop;
    }

    private void submitLineSeparatorDetector() {
        if (this.detectLineSeparator && !this.lineSeparatorDetected) {
            this.addInputAnalysisProcess(new LineSeparatorDetector(){

                @Override
                protected void apply(char separator1, char separator2) {
                    if (separator1 != '\u0000') {
                        AbstractCharInputReader.this.lineSeparatorDetected = true;
                        AbstractCharInputReader.this.lineSeparator1 = separator1;
                        AbstractCharInputReader.this.lineSeparator2 = separator2;
                    } else {
                        AbstractCharInputReader.this.setLineSeparator(Format.getSystemLineSeparator());
                    }
                }
            });
        }
    }

    private void setLineSeparator(char[] lineSeparator) {
        if (lineSeparator == null || lineSeparator.length == 0) {
            throw new IllegalArgumentException("Invalid line separator. Expected 1 to 2 characters");
        }
        if (lineSeparator.length > 2) {
            throw new IllegalArgumentException("Invalid line separator. Up to 2 characters are expected. Got " + lineSeparator.length + " characters.");
        }
        this.lineSeparator1 = lineSeparator[0];
        this.lineSeparator2 = lineSeparator.length == 2 ? lineSeparator[1] : (char)'\u0000';
    }

    protected abstract void setReader(Reader var1);

    protected abstract void reloadBuffer();

    protected final void unwrapInputStream(BomInput.BytesProcessedNotification notification) {
        InputStream inputStream = notification.input;
        String encoding = notification.encoding;
        if (encoding != null) {
            try {
                this.start(new InputStreamReader(inputStream, encoding));
            }
            catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } else {
            this.length = -1;
            this.start(new InputStreamReader(inputStream), false);
        }
    }

    private void start(Reader reader, boolean resetTmp) {
        if (resetTmp) {
            this.tmp.reset();
        }
        this.stop();
        this.setReader(reader);
        this.lineCount = 0L;
        this.lineSeparatorDetected = false;
        this.submitLineSeparatorDetector();
        this.updateBuffer();
        if (this.length > 0 && this.buffer[0] == '\ufeff') {
            ++this.i;
        }
    }

    @Override
    public final void start(Reader reader) {
        this.start(reader, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateBuffer() {
        if (!this.commentProcessing && this.length - this.recordStart > 0 && this.buffer != null && !this.skipping) {
            this.tmp.append(this.buffer, this.recordStart, this.length - this.recordStart);
        }
        this.recordStart = 0;
        this.reloadBuffer();
        this.charCount += (long)this.i;
        this.i = 0;
        if (this.length == -1) {
            this.stop();
            this.incrementLineCount = true;
        }
        if (this.inputAnalysisProcesses != null) {
            if (this.length > 0 && this.length <= 4) {
                int tmpLength = this.length;
                char[] tmp = Arrays.copyOfRange(this.buffer, 0, this.length + 1);
                List<InputAnalysisProcess> processes = this.inputAnalysisProcesses;
                this.inputAnalysisProcesses = null;
                this.reloadBuffer();
                this.inputAnalysisProcesses = processes;
                if (this.length != -1) {
                    char[] newBuffer = new char[tmpLength + this.buffer.length];
                    System.arraycopy(tmp, 0, newBuffer, 0, tmpLength);
                    System.arraycopy(this.buffer, 0, newBuffer, tmpLength, this.length);
                    this.buffer = newBuffer;
                    this.length += tmpLength;
                } else {
                    this.buffer = tmp;
                    this.length = tmpLength;
                }
            }
            try {
                for (InputAnalysisProcess process : this.inputAnalysisProcesses) {
                    process.execute(this.buffer, this.length);
                }
            }
            finally {
                if (this.length > 4) {
                    this.inputAnalysisProcesses = null;
                }
            }
        }
    }

    public final void addInputAnalysisProcess(InputAnalysisProcess inputAnalysisProcess) {
        if (inputAnalysisProcess == null) {
            return;
        }
        if (this.inputAnalysisProcesses == null) {
            this.inputAnalysisProcesses = new ArrayList<InputAnalysisProcess>();
        }
        this.inputAnalysisProcesses.add(inputAnalysisProcess);
    }

    private void throwEOFException() {
        if (this.incrementLineCount) {
            ++this.lineCount;
        }
        this.ch = '\u0000';
        throw new EOFException();
    }

    @Override
    public final char nextChar() {
        if (this.length == -1) {
            this.throwEOFException();
        }
        this.ch = this.buffer[this.i++];
        if (this.i >= this.length) {
            this.updateBuffer();
        }
        if (this.lineSeparator1 == this.ch && (this.lineSeparator2 == '\u0000' || this.length != -1 && this.lineSeparator2 == this.buffer[this.i])) {
            ++this.lineCount;
            if (this.normalizeLineEndings) {
                this.ch = this.normalizedLineSeparator;
                if (this.lineSeparator2 == '\u0000') {
                    return this.ch;
                }
                if (++this.i >= this.length) {
                    if (this.length != -1) {
                        this.updateBuffer();
                    } else {
                        this.throwEOFException();
                    }
                }
            }
        }
        return this.ch;
    }

    @Override
    public final char getChar() {
        return this.ch;
    }

    @Override
    public final long lineCount() {
        return this.lineCount;
    }

    @Override
    public final void skipLines(long lines) {
        if (lines < 1L) {
            this.skipping = false;
            return;
        }
        this.skipping = true;
        long expectedLineCount = this.lineCount + lines;
        try {
            do {
                this.nextChar();
            } while (this.lineCount < expectedLineCount);
            this.skipping = false;
        }
        catch (EOFException ex) {
            this.skipping = false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public String readComment() {
        long expectedLineCount = this.lineCount + 1L;
        this.commentProcessing = true;
        this.tmp.reset();
        try {
            while (true) {
                char ch;
                if ((ch = this.nextChar()) <= ' ' && this.whitespaceRangeStart < ch) {
                    ch = this.skipWhitespace(ch, this.normalizedLineSeparator, this.normalizedLineSeparator);
                }
                this.tmp.appendUntil(ch, this, this.normalizedLineSeparator, this.normalizedLineSeparator);
                if (this.lineCount >= expectedLineCount) {
                    this.tmp.updateWhitespace();
                    String string = this.tmp.getAndReset();
                    return string;
                }
                this.tmp.appendIgnoringWhitespace(this.nextChar());
                continue;
                break;
            }
        }
        catch (EOFException ex) {
            this.tmp.updateWhitespace();
            String string = this.tmp.getAndReset();
            return string;
        }
        finally {
            this.commentProcessing = false;
        }
    }

    @Override
    public final long charCount() {
        return this.charCount + (long)this.i;
    }

    @Override
    public final void enableNormalizeLineEndings(boolean normalizeLineEndings) {
        this.normalizeLineEndings = normalizeLineEndings;
    }

    @Override
    public char[] getLineSeparator() {
        if (this.lineSeparator2 != '\u0000') {
            return new char[]{this.lineSeparator1, this.lineSeparator2};
        }
        return new char[]{this.lineSeparator1};
    }

    @Override
    public final char skipWhitespace(char ch, char stopChar1, char stopChar2) {
        while (ch <= ' ' && ch != stopChar1 && ch != this.normalizedLineSeparator && ch != stopChar2 && this.whitespaceRangeStart < ch) {
            ch = this.nextChar();
        }
        return ch;
    }

    @Override
    public final int currentParsedContentLength() {
        return this.i - this.recordStart + this.tmp.length();
    }

    @Override
    public final String currentParsedContent() {
        if (this.tmp.length() == 0) {
            if (this.i > this.recordStart) {
                return new String(this.buffer, this.recordStart, this.i - this.recordStart);
            }
            return null;
        }
        if (this.i > this.recordStart) {
            this.tmp.append(this.buffer, this.recordStart, this.i - this.recordStart);
        }
        return this.tmp.getAndReset();
    }

    @Override
    public final int lastIndexOf(char ch) {
        if (this.tmp.length() == 0) {
            if (this.i > this.recordStart) {
                int x = this.i - 1;
                int c = 0;
                while (x >= this.recordStart) {
                    if (this.buffer[x] == ch) {
                        return this.recordStart + c;
                    }
                    --x;
                    ++c;
                }
            }
            return -1;
        }
        if (this.i > this.recordStart) {
            int x = this.i - 1;
            int c = 0;
            while (x >= this.recordStart) {
                if (this.buffer[x] == ch) {
                    return this.tmp.length() + this.recordStart + c;
                }
                --x;
                ++c;
            }
        }
        return this.tmp.lastIndexOf(ch);
    }

    @Override
    public final void markRecordStart() {
        this.tmp.reset();
        this.recordStart = this.i % this.length;
    }

    @Override
    public final boolean skipString(char ch, char stop) {
        if (this.i == 0) {
            return false;
        }
        int i = this.i;
        while (ch != stop) {
            if (i >= this.length) {
                return false;
            }
            if (this.lineSeparator1 == ch && (this.lineSeparator2 == '\u0000' || this.lineSeparator2 == this.buffer[i])) break;
            ch = this.buffer[i++];
        }
        this.i = i - 1;
        this.nextChar();
        return true;
    }

    @Override
    public final String getString(char ch, char stop, boolean trim, String nullValue, int maxLength) {
        if (this.i == 0) {
            return null;
        }
        int i = this.i;
        while (ch != stop) {
            if (i >= this.length) {
                return null;
            }
            if (this.lineSeparator1 == ch && (this.lineSeparator2 == '\u0000' || this.lineSeparator2 == this.buffer[i])) break;
            ch = this.buffer[i++];
        }
        int pos = this.i - 1;
        int len = i - this.i;
        if (maxLength != -1 && len > maxLength) {
            return null;
        }
        this.i = i - 1;
        if (trim) {
            i -= 2;
            while (this.buffer[i] <= ' ' && this.whitespaceRangeStart < this.buffer[i]) {
                --len;
                --i;
            }
        }
        String out = len <= 0 ? nullValue : new String(this.buffer, pos, len);
        this.nextChar();
        return out;
    }

    @Override
    public final String getQuotedString(char quote, char escape, char escapeEscape, int maxLength, char stop1, char stop2, boolean keepQuotes, boolean keepEscape, boolean trimLeading, boolean trimTrailing) {
        int len;
        if (this.i == 0) {
            return null;
        }
        int i = this.i;
        while (true) {
            char next;
            if (i >= this.length) {
                return null;
            }
            this.ch = this.buffer[i];
            if (this.ch == quote) {
                if (this.buffer[i - 1] == escape) {
                    if (keepEscape) {
                        ++i;
                        continue;
                    }
                    return null;
                }
                if (i + 1 < this.length && ((next = this.buffer[i + 1]) == stop1 || next == stop2)) break;
                return null;
            }
            if (this.ch == escape && !keepEscape ? i + 1 < this.length && ((next = this.buffer[i + 1]) == quote || next == escapeEscape) : this.lineSeparator1 == this.ch && this.normalizeLineEndings && (this.lineSeparator2 == '\u0000' || i + 1 < this.length && this.lineSeparator2 == this.buffer[i + 1])) {
                return null;
            }
            ++i;
        }
        int pos = this.i;
        if (maxLength != -1 && len > maxLength) {
            return null;
        }
        if (keepQuotes) {
            --pos;
            len += 2;
        } else {
            if (trimTrailing) {
                for (len = i - this.i; len > 0 && this.buffer[pos + len - 1] <= ' '; --len) {
                }
            }
            if (trimLeading) {
                while (len > 0 && this.buffer[pos] <= ' ') {
                    ++pos;
                    --len;
                }
            }
        }
        this.i = i + 1;
        String out = len <= 0 ? "" : new String(this.buffer, pos, len);
        if (this.i >= this.length) {
            this.updateBuffer();
        }
        return out;
    }

    @Override
    public final boolean skipQuotedString(char quote, char escape, char stop1, char stop2) {
        if (this.i == 0) {
            return false;
        }
        int i = this.i;
        while (true) {
            if (i >= this.length) {
                return false;
            }
            this.ch = this.buffer[i];
            if (this.ch == quote) {
                char next;
                if (this.buffer[i - 1] == escape) {
                    ++i;
                    continue;
                }
                if (i + 1 < this.length && ((next = this.buffer[i + 1]) == stop1 || next == stop2)) break;
                return false;
            }
            if (this.lineSeparator1 == this.ch && this.normalizeLineEndings && (this.lineSeparator2 == '\u0000' || i + 1 < this.length && this.lineSeparator2 == this.buffer[i + 1])) {
                return false;
            }
            ++i;
        }
        this.i = i + 1;
        if (this.i >= this.length) {
            this.updateBuffer();
        }
        return true;
    }
}

