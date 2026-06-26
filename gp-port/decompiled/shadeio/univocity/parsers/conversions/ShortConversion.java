/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import shadeio.univocity.parsers.conversions.ObjectConversion;

public class ShortConversion
extends ObjectConversion<Short> {
    public ShortConversion() {
    }

    public ShortConversion(Short valueIfStringIsNull, String valueIfObjectIsNull) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
    }

    @Override
    protected Short fromString(String input) {
        return Short.valueOf(input);
    }
}

