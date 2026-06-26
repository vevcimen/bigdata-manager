/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import shadeio.univocity.parsers.conversions.ObjectConversion;

public class DoubleConversion
extends ObjectConversion<Double> {
    public DoubleConversion() {
    }

    public DoubleConversion(Double valueIfStringIsNull, String valueIfObjectIsNull) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
    }

    @Override
    protected Double fromString(String input) {
        return Double.valueOf(input);
    }
}

