/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import shadeio.univocity.parsers.conversions.ObjectConversion;

public class LongConversion
extends ObjectConversion<Long> {
    public LongConversion() {
    }

    public LongConversion(Long valueIfStringIsNull, String valueIfObjectIsNull) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
    }

    @Override
    protected Long fromString(String input) {
        return Long.valueOf(input);
    }
}

