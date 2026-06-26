/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.CipherParameters;

public class KeyParameter
implements CipherParameters {
    private byte[] key;

    public KeyParameter(byte[] key) {
        this(key, 0, key.length);
    }

    public KeyParameter(byte[] key, int keyOff, int keyLen) {
        this.key = new byte[keyLen];
        System.arraycopy(key, keyOff, this.key, 0, keyLen);
    }

    public byte[] getKey() {
        return this.key;
    }
}

