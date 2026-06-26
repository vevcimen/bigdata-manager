/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import shadeio.univocity.parsers.common.fields.FieldSet;
import shadeio.univocity.parsers.conversions.Conversion;

public interface ConversionProcessor {
    public FieldSet<Integer> convertIndexes(Conversion ... var1);

    public void convertAll(Conversion ... var1);

    public FieldSet<String> convertFields(Conversion ... var1);

    public void convertType(Class<?> var1, Conversion ... var2);
}

