/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.fields;

import java.util.List;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.fields.FieldSelector;
import shadeio.univocity.parsers.common.fields.FieldSet;

public class FieldIndexSelector
extends FieldSet<Integer>
implements FieldSelector {
    @Override
    public int[] getFieldIndexes(NormalizedString[] columns) {
        List chosenIndexes = this.get();
        int[] out = new int[chosenIndexes.size()];
        int i = 0;
        for (Integer index : chosenIndexes) {
            out[i++] = index;
        }
        return out;
    }

    @Override
    public int[] getFieldIndexes(String[] headers) {
        return this.getFieldIndexes(NormalizedString.toIdentifierGroupArray(headers));
    }
}

