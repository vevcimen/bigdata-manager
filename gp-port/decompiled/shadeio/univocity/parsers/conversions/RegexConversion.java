/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import shadeio.univocity.parsers.conversions.Conversion;

public class RegexConversion
implements Conversion<String, String> {
    private final String replaceRegex;
    private final String replacement;

    public RegexConversion(String replaceRegex, String replacement) {
        this.replaceRegex = replaceRegex;
        this.replacement = replacement;
    }

    @Override
    public String execute(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll(this.replaceRegex, this.replacement);
    }

    @Override
    public String revert(String input) {
        return this.execute(input);
    }
}

