/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.fields;

import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.fields.FieldSelector;

public class AllIndexesSelector
implements FieldSelector {
    @Override
    public int[] getFieldIndexes(NormalizedString[] headers) {
        if (headers == null) {
            return null;
        }
        int[] out = new int[headers.length];
        for (int i = 0; i < out.length; ++i) {
            out[i] = i;
        }
        return out;
    }

    @Override
    public String describe() {
        return "all fields";
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int[] getFieldIndexes(String[] headers) {
        return this.getFieldIndexes(NormalizedString.toIdentifierGroupArray(headers));
    }
}

