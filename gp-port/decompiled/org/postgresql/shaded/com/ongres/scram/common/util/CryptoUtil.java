/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.util;

import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public class CryptoUtil {
    private static final int MIN_ASCII_PRINTABLE_RANGE = 33;
    private static final int MAX_ASCII_PRINTABLE_RANGE = 126;
    private static final int EXCLUDED_CHAR = 44;

    public static String nonce(int size, SecureRandom random) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        char[] chars = new char[size];
        int i = 0;
        while (i < size) {
            int r = random.nextInt(94) + 33;
            if (r == 44) continue;
            chars[i++] = (char)r;
        }
        return new String(chars);
    }

    public static String nonce(int size) {
        return CryptoUtil.nonce(size, SecureRandomHolder.INSTANCE);
    }

    public static byte[] hi(SecretKeyFactory secretKeyFactory, int keyLength, char[] value, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(value, salt, iterations, keyLength);
            SecretKey key = secretKeyFactory.generateSecret(spec);
            return key.getEncoded();
        }
        catch (InvalidKeySpecException e) {
            throw new RuntimeException("Platform error: unsupported PBEKeySpec");
        }
    }

    public static byte[] hmac(SecretKeySpec secretKeySpec, Mac mac, byte[] message) {
        try {
            mac.init(secretKeySpec);
        }
        catch (InvalidKeyException e) {
            throw new RuntimeException("Platform error: unsupported key for HMAC algorithm");
        }
        return mac.doFinal(message);
    }

    public static byte[] xor(byte[] value1, byte[] value2) throws IllegalArgumentException {
        Preconditions.checkNotNull(value1, "value1");
        Preconditions.checkNotNull(value2, "value2");
        Preconditions.checkArgument(value1.length == value2.length, "Both values must have the same length");
        byte[] result = new byte[value1.length];
        for (int i = 0; i < value1.length; ++i) {
            result[i] = (byte)(value1[i] ^ value2[i]);
        }
        return result;
    }

    private static class SecureRandomHolder {
        private static final SecureRandom INSTANCE = new SecureRandom();

        private SecureRandomHolder() {
        }
    }
}

