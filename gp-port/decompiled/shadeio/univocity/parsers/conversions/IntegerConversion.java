/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import shadeio.univocity.parsers.conversions.ObjectConversion;

public class IntegerConversion
extends ObjectConversion<Integer> {
    public IntegerConversion() {
    }

    public IntegerConversion(Integer valueIfStringIsNull, String valueIfObjectIsNull) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
    }

    @Override
    protected Integer fromString(String input) {
        return Integer.valueOf(input);
    }
}

