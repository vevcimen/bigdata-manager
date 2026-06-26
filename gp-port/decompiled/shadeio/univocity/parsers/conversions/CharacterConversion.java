/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.conversions.ObjectConversion;

public class CharacterConversion
extends ObjectConversion<Character> {
    public CharacterConversion() {
    }

    public CharacterConversion(Character valueIfStringIsNull, String valueIfObjectIsNull) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
    }

    @Override
    protected Character fromString(String input) {
        if (input.length() != 1) {
            DataProcessingException exception = new DataProcessingException("'{value}' is not a character");
            exception.setValue(input);
            throw exception;
        }
        return Character.valueOf(input.charAt(0));
    }
}

