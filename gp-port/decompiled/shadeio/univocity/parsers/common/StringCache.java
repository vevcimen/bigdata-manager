/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class StringCache<T> {
    private static final int DEFAULT_SIZE_LIMIT = 16384;
    private static final int DEFAULT_MAX_STRING_LENGTH = 0;
    private final Map<String, SoftReference<T>> stringCache = new ConcurrentHashMap<String, SoftReference<T>>();
    private int sizeLimit = 16384;
    private int maxStringLength = 0;

    protected abstract T process(String var1);

    public boolean containsKey(String input) {
        return this.stringCache.containsKey(input);
    }

    public int getSizeLimit() {
        return this.sizeLimit;
    }

    public void setSizeLimit(int sizeLimit) {
        if (sizeLimit <= 0) {
            sizeLimit = 16384;
        }
        this.sizeLimit = sizeLimit;
    }

    public void put(String input, T value) {
        if (input == null || input.length() > this.maxStringLength) {
            return;
        }
        if (this.stringCache.size() >= this.sizeLimit) {
            this.stringCache.clear();
        }
        this.stringCache.put(input, new SoftReference<T>(value));
    }

    public T get(String input) {
        T out;
        if (input == null || this.maxStringLength > 0 && input.length() > this.maxStringLength) {
            return null;
        }
        SoftReference<T> ref = this.stringCache.get(input);
        if (ref == null || ref.get() == null) {
            out = this.process(input);
            ref = new SoftReference<T>(out);
            this.stringCache.put(input, ref);
        } else {
            out = ref.get();
        }
        return out;
    }

    public void clear() {
        this.stringCache.clear();
    }

    public int getMaxStringLength() {
        return this.maxStringLength;
    }

    public void setMaxStringLength(int maxStringLength) {
        this.maxStringLength = maxStringLength;
    }
}

