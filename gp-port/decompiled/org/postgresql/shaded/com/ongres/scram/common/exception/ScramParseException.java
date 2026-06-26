/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.exception;

import org.postgresql.shaded.com.ongres.scram.common.exception.ScramException;

public class ScramParseException
extends ScramException {
    public ScramParseException(String detail) {
        super(detail);
    }

    public ScramParseException(String detail, Throwable ex) {
        super(detail, ex);
    }
}

