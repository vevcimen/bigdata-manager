/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common;

import org.postgresql.shaded.com.ongres.scram.common.ScramAttributes;
import org.postgresql.shaded.com.ongres.scram.common.exception.ScramParseException;
import org.postgresql.shaded.com.ongres.scram.common.util.AbstractCharAttributeValue;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public class ScramAttributeValue
extends AbstractCharAttributeValue {
    public ScramAttributeValue(ScramAttributes attribute, String value) {
        super(attribute, Preconditions.checkNotNull(value, "value"));
    }

    public static StringBuffer writeTo(StringBuffer sb, ScramAttributes attribute, String value) {
        return new ScramAttributeValue(attribute, value).writeTo(sb);
    }

    public static ScramAttributeValue parse(String value) throws ScramParseException {
        if (null == value || value.length() < 3 || value.charAt(1) != '=') {
            throw new ScramParseException("Invalid ScramAttributeValue '" + value + "'");
        }
        return new ScramAttributeValue(ScramAttributes.byChar(value.charAt(0)), value.substring(2));
    }
}

