/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.gssapi;

import org.postgresql.shaded.com.ongres.scram.common.gssapi.Gs2Attributes;
import org.postgresql.shaded.com.ongres.scram.common.util.AbstractCharAttributeValue;

public class Gs2AttributeValue
extends AbstractCharAttributeValue {
    public Gs2AttributeValue(Gs2Attributes attribute, String value) {
        super(attribute, value);
    }

    public static StringBuffer writeTo(StringBuffer sb, Gs2Attributes attribute, String value) {
        return new Gs2AttributeValue(attribute, value).writeTo(sb);
    }

    public static Gs2AttributeValue parse(String value) throws IllegalArgumentException {
        if (null == value) {
            return null;
        }
        if (value.length() < 1 || value.length() == 2 || value.length() > 2 && value.charAt(1) != '=') {
            throw new IllegalArgumentException("Invalid Gs2AttributeValue");
        }
        return new Gs2AttributeValue(Gs2Attributes.byChar(value.charAt(0)), value.length() > 2 ? value.substring(2) : null);
    }
}

