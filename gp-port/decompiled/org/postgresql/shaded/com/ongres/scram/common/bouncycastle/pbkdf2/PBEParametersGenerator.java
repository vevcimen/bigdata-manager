/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.CipherParameters;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.Strings;

public abstract class PBEParametersGenerator {
    protected byte[] password;
    protected byte[] salt;
    protected int iterationCount;

    protected PBEParametersGenerator() {
    }

    public void init(byte[] password, byte[] salt, int iterationCount) {
        this.password = password;
        this.salt = salt;
        this.iterationCount = iterationCount;
    }

    public byte[] getPassword() {
        return this.password;
    }

    public byte[] getSalt() {
        return this.salt;
    }

    public int getIterationCount() {
        return this.iterationCount;
    }

    public abstract CipherParameters generateDerivedParameters(int var1);

    public static byte[] PKCS5PasswordToUTF8Bytes(char[] password) {
        if (password != null) {
            return Strings.toUTF8ByteArray(password);
        }
        return new byte[0];
    }
}

