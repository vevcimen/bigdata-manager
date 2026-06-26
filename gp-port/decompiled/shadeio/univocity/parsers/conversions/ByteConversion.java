/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import shadeio.univocity.parsers.conversions.ObjectConversion;

public class ByteConversion
extends ObjectConversion<Byte> {
    public ByteConversion() {
    }

    public ByteConversion(Byte valueIfStringIsNull, String valueIfObjectIsNull) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
    }

    @Override
    protected Byte fromString(String input) {
        return Byte.valueOf(input);
    }
}

