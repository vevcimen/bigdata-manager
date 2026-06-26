/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.base64;

public class EncoderException
extends IllegalStateException {
    private Throwable cause;

    EncoderException(String msg, Throwable cause) {
        super(msg);
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }
}

