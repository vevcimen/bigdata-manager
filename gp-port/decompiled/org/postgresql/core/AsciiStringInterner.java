/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.Encoding;
import org.postgresql.util.internal.Nullness;

final class AsciiStringInterner {
    final ConcurrentMap<BaseKey, SoftReference<String>> cache = new ConcurrentHashMap<BaseKey, SoftReference<String>>(128);
    final ReferenceQueue<String> refQueue = new ReferenceQueue();

    AsciiStringInterner() {
    }

    public boolean putString(String val) {
        byte[] copy = val.getBytes(StandardCharsets.UTF_8);
        int hash = AsciiStringInterner.hashKey(copy, 0, copy.length);
        if (hash == 0) {
            return false;
        }
        Key key = new Key(copy, hash);
        this.cache.put(key, new SoftReference<String>(val));
        return true;
    }

    public String getString(byte[] bytes, int offset, int length, Encoding encoding) throws IOException {
        String val;
        if (length == 0) {
            return "";
        }
        int hash = AsciiStringInterner.hashKey(bytes, offset, length);
        if (hash == 0) {
            return encoding.decode(bytes, offset, length);
        }
        this.cleanQueue();
        TempKey tempKey = new TempKey(hash, bytes, offset, length);
        SoftReference ref = (SoftReference)this.cache.get(tempKey);
        if (ref != null && (val = (String)ref.get()) != null) {
            return val;
        }
        byte[] copy = Arrays.copyOfRange(bytes, offset, offset + length);
        Key key = new Key(copy, hash);
        String value = new String(copy, StandardCharsets.US_ASCII);
        ref = this.cache.compute(key, (k, v) -> {
            if (v == null) {
                return new StringReference(key, value);
            }
            String val = (String)v.get();
            return val != null ? v : new StringReference(key, value);
        });
        return (String)Nullness.castNonNull(ref.get());
    }

    public String getStringIfPresent(byte[] bytes, int offset, int length, Encoding encoding) throws IOException {
        String val;
        if (length == 0) {
            return "";
        }
        int hash = AsciiStringInterner.hashKey(bytes, offset, length);
        if (hash == 0) {
            return encoding.decode(bytes, offset, length);
        }
        this.cleanQueue();
        TempKey tempKey = new TempKey(hash, bytes, offset, length);
        SoftReference ref = (SoftReference)this.cache.get(tempKey);
        if (ref != null && (val = (String)ref.get()) != null) {
            return val;
        }
        return new String(bytes, offset, length, StandardCharsets.US_ASCII);
    }

    private void cleanQueue() {
        Reference<String> ref;
        while ((ref = this.refQueue.poll()) != null) {
            ((StringReference)ref).dispose();
        }
    }

    private static int hashKey(byte[] bytes, int offset, int length) {
        int result = 1;
        int j = offset + length;
        for (int i = offset; i < j; ++i) {
            byte b = bytes[i];
            if (b < 0) {
                return 0;
            }
            result = 31 * result + b;
        }
        return result;
    }

    static boolean arrayEquals(byte[] a, int aOffset, int aLength, byte[] b, int bOffset, int bLength) {
        if (aLength != bLength) {
            return false;
        }
        for (int i = 0; i < aLength; ++i) {
            if (a[aOffset + i] == b[bOffset + i]) continue;
            return false;
        }
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(32 + 8 * this.cache.size());
        sb.append("AsciiStringInterner [");
        this.cache.forEach((k, v) -> {
            sb.append('\'');
            k.appendString(sb);
            sb.append("', ");
        });
        int length = sb.length();
        if (length > 21) {
            sb.setLength(sb.length() - 2);
        }
        sb.append(']');
        return sb.toString();
    }

    private final class StringReference
    extends SoftReference<String> {
        private final BaseKey key;

        StringReference(BaseKey key, String referent) {
            super(referent, AsciiStringInterner.this.refQueue);
            this.key = key;
        }

        void dispose() {
            AsciiStringInterner.this.cache.remove(this.key, this);
        }
    }

    private static final class Key
    extends BaseKey {
        final byte[] key;

        Key(byte[] key, int hash) {
            super(hash);
            this.key = key;
        }

        @Override
        boolean equalsBytes(BaseKey other) {
            return other.equals(this.key, 0, this.key.length);
        }

        @Override
        public boolean equals(byte[] other, int offset, int length) {
            return AsciiStringInterner.arrayEquals(this.key, 0, this.key.length, other, offset, length);
        }

        @Override
        void appendString(StringBuilder sb) {
            for (int i = 0; i < this.key.length; ++i) {
                sb.append((char)this.key[i]);
            }
        }
    }

    private static class TempKey
    extends BaseKey {
        final byte[] bytes;
        final int offset;
        final int length;

        TempKey(int hash, byte[] bytes, int offset, int length) {
            super(hash);
            this.bytes = bytes;
            this.offset = offset;
            this.length = length;
        }

        @Override
        boolean equalsBytes(BaseKey other) {
            return other.equals(this.bytes, this.offset, this.length);
        }

        @Override
        public boolean equals(byte[] other, int offset, int length) {
            return AsciiStringInterner.arrayEquals(this.bytes, this.offset, this.length, other, offset, length);
        }

        @Override
        void appendString(StringBuilder sb) {
            int j = this.offset + this.length;
            for (int i = this.offset; i < j; ++i) {
                sb.append((char)this.bytes[i]);
            }
        }
    }

    private static abstract class BaseKey {
        private final int hash;

        BaseKey(int hash) {
            this.hash = hash;
        }

        public final int hashCode() {
            return this.hash;
        }

        public final boolean equals(@Nullable Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof BaseKey)) {
                return false;
            }
            BaseKey other = (BaseKey)obj;
            return this.equalsBytes(other);
        }

        abstract boolean equalsBytes(BaseKey var1);

        abstract boolean equals(byte[] var1, int var2, int var3);

        abstract void appendString(StringBuilder var1);
    }
}

