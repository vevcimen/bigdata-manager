/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.fields;

import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.fields.ExcludeFieldNameSelector;
import shadeio.univocity.parsers.common.fields.FieldSelector;
import shadeio.univocity.parsers.common.fields.FieldSet;

public class ExcludeFieldEnumSelector
extends FieldSet<Enum>
implements FieldSelector {
    private ExcludeFieldNameSelector names = new ExcludeFieldNameSelector();

    @Override
    public int[] getFieldIndexes(NormalizedString[] headers) {
        if (headers == null) {
            return null;
        }
        this.names.set(ArgumentUtils.toArray(this.get()));
        return this.names.getFieldIndexes(headers);
    }

    @Override
    public String describe() {
        return "undesired " + super.describe();
    }

    @Override
    public ExcludeFieldEnumSelector clone() {
        ExcludeFieldEnumSelector out = (ExcludeFieldEnumSelector)super.clone();
        out.names = (ExcludeFieldNameSelector)this.names.clone();
        return out;
    }

    @Override
    public int[] getFieldIndexes(String[] headers) {
        return this.getFieldIndexes(NormalizedString.toIdentifierGroupArray(headers));
    }
}

