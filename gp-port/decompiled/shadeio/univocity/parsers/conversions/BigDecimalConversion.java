/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import java.math.BigDecimal;
import shadeio.univocity.parsers.conversions.ObjectConversion;

public class BigDecimalConversion
extends ObjectConversion<BigDecimal> {
    public BigDecimalConversion() {
    }

    public BigDecimalConversion(BigDecimal valueIfStringIsNull, String valueIfObjectIsNull) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
    }

    @Override
    protected BigDecimal fromString(String input) {
        return new BigDecimal(input);
    }
}

