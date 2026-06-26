/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.fields;

import java.util.ArrayList;
import java.util.Arrays;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.fields.FieldSelector;
import shadeio.univocity.parsers.common.fields.FieldSet;

public class FieldNameSelector
extends FieldSet<String>
implements FieldSelector,
Cloneable {
    public int getFieldIndex(String header) {
        return this.getFieldIndexes(new NormalizedString[]{NormalizedString.valueOf(header)})[0];
    }

    @Override
    public int[] getFieldIndexes(NormalizedString[] headers) {
        if (headers == null) {
            return null;
        }
        Object[] normalizedHeader = headers;
        ArrayList<NormalizedString> selection = NormalizedString.toArrayList(this.get());
        Object[] chosenFields = selection.toArray(new NormalizedString[0]);
        Object[] unknownFields = ArgumentUtils.findMissingElements(normalizedHeader, chosenFields);
        if (unknownFields.length > 0 && !selection.containsAll(Arrays.asList(normalizedHeader)) && unknownFields.length == chosenFields.length) {
            return new int[0];
        }
        int[] out = new int[selection.size()];
        Arrays.fill(out, -1);
        int i = 0;
        for (NormalizedString chosenField : selection) {
            int[] indexes = ArgumentUtils.indexesOf(normalizedHeader, chosenField);
            if (indexes.length > 1) {
                out = Arrays.copyOf(out, out.length + indexes.length - 1);
            }
            if (indexes.length != 0) {
                for (int j = 0; j < indexes.length; ++j) {
                    out[i++] = indexes[j];
                }
                continue;
            }
            ++i;
        }
        return out;
    }

    @Override
    public int[] getFieldIndexes(String[] headers) {
        return this.getFieldIndexes(NormalizedString.toIdentifierGroupArray(headers));
    }
}

