/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.fields;

import java.util.HashSet;
import java.util.Iterator;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.fields.FieldSelector;
import shadeio.univocity.parsers.common.fields.FieldSet;

public class ExcludeFieldIndexSelector
extends FieldSet<Integer>
implements FieldSelector {
    @Override
    public int[] getFieldIndexes(NormalizedString[] columns) {
        if (columns == null) {
            return null;
        }
        HashSet chosenFields = new HashSet(this.get());
        Iterator it = chosenFields.iterator();
        while (it.hasNext()) {
            Integer chosenIndex = (Integer)it.next();
            if (chosenIndex < columns.length && chosenIndex >= 0) continue;
            it.remove();
        }
        int[] out = new int[columns.length - chosenFields.size()];
        int j = 0;
        for (int i = 0; i < columns.length; ++i) {
            if (chosenFields.contains(i)) continue;
            out[j++] = i;
        }
        return out;
    }

    @Override
    public String describe() {
        return "undesired " + super.describe();
    }

    @Override
    public int[] getFieldIndexes(String[] headers) {
        return this.getFieldIndexes(NormalizedString.toIdentifierGroupArray(headers));
    }
}

