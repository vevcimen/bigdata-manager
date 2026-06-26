/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import shadeio.univocity.parsers.conversions.Conversion;

public class TrimConversion
implements Conversion<String, String> {
    private final int length;

    public TrimConversion() {
        this.length = -1;
    }

    public TrimConversion(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Maximum trim length must be positive");
        }
        this.length = length;
    }

    @Override
    public String execute(String input) {
        if (input == null) {
            return null;
        }
        if (input.length() == 0) {
            return input;
        }
        if (this.length != -1) {
            int begin;
            for (begin = 0; begin < input.length() && input.charAt(begin) <= ' '; ++begin) {
            }
            if (begin == input.length()) {
                return "";
            }
            int end = begin + (this.length < input.length() ? this.length : input.length()) - 1;
            if (end >= input.length()) {
                end = input.length() - 1;
            }
            while (input.charAt(end) <= ' ') {
                --end;
            }
            return input.substring(begin, end + 1);
        }
        return input.trim();
    }

    @Override
    public String revert(String input) {
        return this.execute(input);
    }
}

