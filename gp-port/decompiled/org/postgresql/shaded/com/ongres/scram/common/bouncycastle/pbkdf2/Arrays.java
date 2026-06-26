/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

public final class Arrays {
    private Arrays() {
    }

    public static byte[] copyOfRange(byte[] data, int from, int to) {
        int newLength = Arrays.getLength(from, to);
        byte[] tmp = new byte[newLength];
        if (data.length - from < newLength) {
            System.arraycopy(data, from, tmp, 0, data.length - from);
        } else {
            System.arraycopy(data, from, tmp, 0, newLength);
        }
        return tmp;
    }

    private static int getLength(int from, int to) {
        int newLength = to - from;
        if (newLength < 0) {
            StringBuffer sb = new StringBuffer(from);
            sb.append(" > ").append(to);
            throw new IllegalArgumentException(sb.toString());
        }
        return newLength;
    }
}

