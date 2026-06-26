/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input.concurrent;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

class CharBucket {
    final char[] data;
    int length = -1;

    public CharBucket(int bucketSize) {
        this.data = bucketSize > 0 ? new char[bucketSize] : new char[0];
    }

    public CharBucket(int bucketSize, char fillWith) {
        this(bucketSize);
        if (bucketSize > 0) {
            Arrays.fill(this.data, fillWith);
        }
    }

    public int fill(Reader reader) throws IOException {
        this.length = reader.read(this.data, 0, this.data.length);
        return this.length;
    }

    public boolean isEmpty() {
        return this.length <= 0;
    }
}

