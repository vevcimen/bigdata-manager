/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import shadeio.univocity.parsers.conversions.Conversion;

public class UpperCaseConversion
implements Conversion<String, String> {
    @Override
    public String execute(String input) {
        if (input == null) {
            return null;
        }
        return input.toUpperCase();
    }

    @Override
    public String revert(String input) {
        return this.execute(input);
    }
}

