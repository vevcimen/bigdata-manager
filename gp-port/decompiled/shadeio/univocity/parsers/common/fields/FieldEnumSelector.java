/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.fields;

import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.fields.FieldNameSelector;
import shadeio.univocity.parsers.common.fields.FieldSelector;
import shadeio.univocity.parsers.common.fields.FieldSet;

public class FieldEnumSelector
extends FieldSet<Enum>
implements FieldSelector {
    private FieldNameSelector names = new FieldNameSelector();

    public int getFieldIndex(Enum column) {
        return this.names.getFieldIndex(column.toString());
    }

    @Override
    public int[] getFieldIndexes(NormalizedString[] headers) {
        if (headers == null) {
            return null;
        }
        this.names.set(ArgumentUtils.toArray(this.get()));
        return this.names.getFieldIndexes(headers);
    }

    @Override
    public FieldEnumSelector clone() {
        FieldEnumSelector out = (FieldEnumSelector)super.clone();
        out.names = (FieldNameSelector)this.names.clone();
        return out;
    }

    @Override
    public int[] getFieldIndexes(String[] headers) {
        return this.getFieldIndexes(NormalizedString.toIdentifierGroupArray(headers));
    }
}

