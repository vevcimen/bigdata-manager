/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

public abstract class Pack {
    public static int bigEndianToInt(byte[] bs, int off) {
        int n = bs[off] << 24;
        n |= (bs[++off] & 0xFF) << 16;
        n |= (bs[++off] & 0xFF) << 8;
        return n |= bs[++off] & 0xFF;
    }

    public static long bigEndianToLong(byte[] bs, int off) {
        int hi = Pack.bigEndianToInt(bs, off);
        int lo = Pack.bigEndianToInt(bs, off + 4);
        return ((long)hi & 0xFFFFFFFFL) << 32 | (long)lo & 0xFFFFFFFFL;
    }

    public static void longToBigEndian(long n, byte[] bs, int off) {
        Pack.intToBigEndian((int)(n >>> 32), bs, off);
        Pack.intToBigEndian((int)(n & 0xFFFFFFFFL), bs, off + 4);
    }

    public static byte[] longToBigEndian(long[] ns) {
        byte[] bs = new byte[8 * ns.length];
        Pack.longToBigEndian(ns, bs, 0);
        return bs;
    }

    public static void longToBigEndian(long[] ns, byte[] bs, int off) {
        for (int i = 0; i < ns.length; ++i) {
            Pack.longToBigEndian(ns[i], bs, off);
            off += 8;
        }
    }

    public static short littleEndianToShort(byte[] bs, int off) {
        int n = bs[off] & 0xFF;
        return (short)(n |= (bs[++off] & 0xFF) << 8);
    }

    public static void intToBigEndian(int n, byte[] bs, int off) {
        bs[off] = (byte)(n >>> 24);
        bs[++off] = (byte)(n >>> 16);
        bs[++off] = (byte)(n >>> 8);
        bs[++off] = (byte)n;
    }
}

