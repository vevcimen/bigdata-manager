/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.exception;

import javax.security.sasl.SaslException;

public class ScramException
extends SaslException {
    public ScramException(String detail) {
        super(detail);
    }

    public ScramException(String detail, Throwable ex) {
        super(detail, ex);
    }
}

