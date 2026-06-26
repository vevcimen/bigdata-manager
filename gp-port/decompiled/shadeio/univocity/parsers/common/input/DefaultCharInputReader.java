/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input;

import java.io.IOException;
import java.io.Reader;
import shadeio.univocity.parsers.common.input.AbstractCharInputReader;
import shadeio.univocity.parsers.common.input.BomInput;

public class DefaultCharInputReader
extends AbstractCharInputReader {
    private Reader reader;
    private boolean unwrapping = false;

    public DefaultCharInputReader(char normalizedLineSeparator, int bufferSize, int whitespaceRangeStart, boolean closeOnStop) {
        super(normalizedLineSeparator, whitespaceRangeStart, closeOnStop);
        this.buffer = new char[bufferSize];
    }

    public DefaultCharInputReader(char[] lineSeparator, char normalizedLineSeparator, int bufferSize, int whitespaceRangeStart, boolean closeOnStop) {
        super(lineSeparator, normalizedLineSeparator, whitespaceRangeStart, closeOnStop);
        this.buffer = new char[bufferSize];
    }

    @Override
    public void stop() {
        try {
            if (!this.unwrapping && this.closeOnStop && this.reader != null) {
                this.reader.close();
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("Error closing input", e);
        }
    }

    @Override
    protected void setReader(Reader reader) {
        this.reader = reader;
        this.unwrapping = false;
    }

    @Override
    public void reloadBuffer() {
        try {
            this.length = this.reader.read(this.buffer, 0, this.buffer.length);
        }
        catch (IOException e) {
            throw new IllegalStateException("Error reading from input", e);
        }
        catch (BomInput.BytesProcessedNotification notification) {
            this.unwrapping = true;
            this.unwrapInputStream(notification);
        }
    }
}

