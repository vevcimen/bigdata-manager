/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

public interface Memoable {
    public Memoable copy();

    public void reset(Memoable var1);
}

