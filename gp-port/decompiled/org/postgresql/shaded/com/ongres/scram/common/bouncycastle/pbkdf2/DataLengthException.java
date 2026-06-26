/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.RuntimeCryptoException;

public class DataLengthException
extends RuntimeCryptoException {
    public DataLengthException() {
    }

    public DataLengthException(String message) {
        super(message);
    }
}

