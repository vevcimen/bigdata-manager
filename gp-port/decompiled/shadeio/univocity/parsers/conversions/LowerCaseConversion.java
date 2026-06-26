/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import shadeio.univocity.parsers.conversions.Conversion;

public class LowerCaseConversion
implements Conversion<String, String> {
    @Override
    public String execute(String input) {
        if (input == null) {
            return null;
        }
        return input.toLowerCase();
    }

    @Override
    public String revert(String input) {
        return this.execute(input);
    }
}

