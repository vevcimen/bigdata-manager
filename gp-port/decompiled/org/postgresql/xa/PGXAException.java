/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.xa;

import javax.transaction.xa.XAException;

public class PGXAException
extends XAException {
    PGXAException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    PGXAException(String message, Throwable cause, int errorCode) {
        super(message);
        this.initCause(cause);
        this.errorCode = errorCode;
    }

    PGXAException(Throwable cause, int errorCode) {
        super(errorCode);
        this.initCause(cause);
    }
}

