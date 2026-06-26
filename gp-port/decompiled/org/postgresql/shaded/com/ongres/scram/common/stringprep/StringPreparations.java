/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.stringprep;

import org.postgresql.shaded.com.ongres.saslprep.SaslPrep;
import org.postgresql.shaded.com.ongres.scram.common.stringprep.StringPreparation;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.postgresql.shaded.com.ongres.scram.common.util.UsAsciiUtils;

public enum StringPreparations implements StringPreparation
{
    NO_PREPARATION{

        @Override
        protected String doNormalize(String value) throws IllegalArgumentException {
            return UsAsciiUtils.toPrintable(value);
        }
    }
    ,
    SASL_PREPARATION{

        @Override
        protected String doNormalize(String value) throws IllegalArgumentException {
            return SaslPrep.saslPrep(value, true);
        }
    };


    protected abstract String doNormalize(String var1) throws IllegalArgumentException;

    @Override
    public String normalize(String value) throws IllegalArgumentException {
        Preconditions.checkNotEmpty(value, "value");
        String normalized = this.doNormalize(value);
        if (null == normalized || normalized.isEmpty()) {
            throw new IllegalArgumentException("null or empty value after normalization");
        }
        return normalized;
    }
}

