/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Digest {
    private static final byte[] HEX_BYTES = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};

    private MD5Digest() {
    }

    public static byte[] encode(byte[] user, byte[] password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password);
            md.update(user);
            byte[] digest = md.digest();
            byte[] hexDigest = new byte[35];
            MD5Digest.bytesToHex(digest, hexDigest, 0);
            md.update(hexDigest, 0, 32);
            md.update(salt);
            digest = md.digest();
            MD5Digest.bytesToHex(digest, hexDigest, 3);
            hexDigest[0] = 109;
            hexDigest[1] = 100;
            hexDigest[2] = 53;
            return hexDigest;
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to encode password with MD5", e);
        }
    }

    private static void bytesToHex(byte[] bytes, byte[] hex, int offset) {
        int pos = offset;
        for (int i = 0; i < 16; ++i) {
            int c = bytes[i] & 0xFF;
            hex[pos++] = HEX_BYTES[c >> 4];
            hex[pos++] = HEX_BYTES[c & 0xF];
        }
    }
}

