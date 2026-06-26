/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import shadeio.univocity.parsers.conversions.ObjectConversion;

public class FloatConversion
extends ObjectConversion<Float> {
    public FloatConversion() {
    }

    public FloatConversion(Float valueIfStringIsNull, String valueIfObjectIsNull) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
    }

    @Override
    protected Float fromString(String input) {
        return Float.valueOf(input);
    }
}

