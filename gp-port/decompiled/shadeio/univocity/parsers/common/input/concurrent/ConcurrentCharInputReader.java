/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input.concurrent;

import java.io.Reader;
import shadeio.univocity.parsers.common.input.AbstractCharInputReader;
import shadeio.univocity.parsers.common.input.BomInput;
import shadeio.univocity.parsers.common.input.concurrent.CharBucket;
import shadeio.univocity.parsers.common.input.concurrent.ConcurrentCharLoader;

public class ConcurrentCharInputReader
extends AbstractCharInputReader {
    private ConcurrentCharLoader bucketLoader;
    private final int bucketSize;
    private final int bucketQuantity;
    private boolean unwrapping = false;

    public ConcurrentCharInputReader(char normalizedLineSeparator, int bucketSize, int bucketQuantity, int whitespaceRangeStart, boolean closeOnStop) {
        super(normalizedLineSeparator, whitespaceRangeStart, closeOnStop);
        this.bucketSize = bucketSize;
        this.bucketQuantity = bucketQuantity;
    }

    public ConcurrentCharInputReader(char[] lineSeparator, char normalizedLineSeparator, int bucketSize, int bucketQuantity, int whitespaceRangeStart, boolean closeOnStop) {
        super(lineSeparator, normalizedLineSeparator, whitespaceRangeStart, closeOnStop);
        this.bucketSize = bucketSize;
        this.bucketQuantity = bucketQuantity;
    }

    @Override
    public void stop() {
        if (!this.unwrapping && this.bucketLoader != null) {
            this.bucketLoader.stopReading();
            this.bucketLoader.reportError();
        }
    }

    @Override
    protected void setReader(Reader reader) {
        if (!this.unwrapping) {
            this.stop();
            this.bucketLoader = new ConcurrentCharLoader(reader, this.bucketSize, this.bucketQuantity, this.closeOnStop);
            this.bucketLoader.reportError();
        } else {
            this.bucketLoader.reader = reader;
        }
        this.unwrapping = false;
    }

    @Override
    protected void reloadBuffer() {
        try {
            CharBucket currentBucket = this.bucketLoader.nextBucket();
            this.bucketLoader.reportError();
            this.buffer = currentBucket.data;
            this.length = currentBucket.length;
        }
        catch (BomInput.BytesProcessedNotification e) {
            this.unwrapping = true;
            this.unwrapInputStream(e);
        }
    }
}

