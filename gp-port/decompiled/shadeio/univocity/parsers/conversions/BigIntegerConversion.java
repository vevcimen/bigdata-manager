/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import java.math.BigInteger;
import shadeio.univocity.parsers.conversions.ObjectConversion;

public class BigIntegerConversion
extends ObjectConversion<BigInteger> {
    public BigIntegerConversion() {
    }

    public BigIntegerConversion(BigInteger valueIfStringIsNull, String valueIfObjectIsNull) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
    }

    @Override
    protected BigInteger fromString(String input) {
        return new BigInteger(input);
    }
}

