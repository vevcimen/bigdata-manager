/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.Digest;

public interface ExtendedDigest
extends Digest {
    public int getByteLength();
}

