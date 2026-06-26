/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common;

import org.postgresql.shaded.com.ongres.scram.common.stringprep.StringPreparation;

public interface ScramMechanism {
    public String getName();

    public byte[] digest(byte[] var1) throws RuntimeException;

    public byte[] hmac(byte[] var1, byte[] var2) throws RuntimeException;

    public int algorithmKeyLength();

    public boolean supportsChannelBinding();

    public byte[] saltedPassword(StringPreparation var1, String var2, byte[] var3, int var4);
}

