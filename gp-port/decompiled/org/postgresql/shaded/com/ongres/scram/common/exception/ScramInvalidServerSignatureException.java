/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.exception;

import org.postgresql.shaded.com.ongres.scram.common.exception.ScramException;

public class ScramInvalidServerSignatureException
extends ScramException {
    public ScramInvalidServerSignatureException(String detail) {
        super(detail);
    }

    public ScramInvalidServerSignatureException(String detail, Throwable ex) {
        super(detail, ex);
    }
}

