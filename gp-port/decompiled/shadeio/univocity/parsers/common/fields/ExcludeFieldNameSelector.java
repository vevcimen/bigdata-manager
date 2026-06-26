/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.fields;

import java.util.HashSet;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.fields.FieldSelector;
import shadeio.univocity.parsers.common.fields.FieldSet;

public class ExcludeFieldNameSelector
extends FieldSet<String>
implements FieldSelector,
Cloneable {
    @Override
    public int[] getFieldIndexes(NormalizedString[] headers) {
        if (headers == null) {
            return null;
        }
        Object[] normalizedHeaders = headers;
        HashSet<NormalizedString> chosenFields = NormalizedString.toHashSet(this.get());
        Object[] unknownFields = ArgumentUtils.findMissingElements(normalizedHeaders, chosenFields);
        int[] out = new int[normalizedHeaders.length - (chosenFields.size() - unknownFields.length)];
        int j = 0;
        for (int i = 0; i < normalizedHeaders.length; ++i) {
            if (chosenFields.contains(normalizedHeaders[i])) continue;
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

