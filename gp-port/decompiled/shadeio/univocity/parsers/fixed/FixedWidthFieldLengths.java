/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.fixed;

import java.util.LinkedHashMap;
import shadeio.univocity.parsers.fixed.FixedWidthFields;

@Deprecated
public class FixedWidthFieldLengths
extends FixedWidthFields {
    public FixedWidthFieldLengths(LinkedHashMap<String, Integer> fields) {
        super(fields);
    }

    public FixedWidthFieldLengths(String[] headers, int[] lengths) {
        super(headers, lengths);
    }

    public FixedWidthFieldLengths(int ... fieldLengths) {
        super(fieldLengths);
    }
}

