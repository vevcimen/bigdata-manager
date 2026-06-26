/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.fields.FieldSelector;
import shadeio.univocity.parsers.common.fields.FieldSet;
import shadeio.univocity.parsers.conversions.Conversion;

abstract class AbstractConversionMapping<T>
implements Cloneable {
    private Map<FieldSelector, Conversion<String, ?>[]> conversionsMap;
    private List<FieldSelector> conversionSequence;

    AbstractConversionMapping(List<FieldSelector> conversionSequence) {
        this.conversionSequence = conversionSequence;
    }

    public FieldSet<T> registerConversions(Conversion<String, ?> ... conversions) {
        ArgumentUtils.noNulls("Conversions", conversions);
        FieldSelector selector = this.newFieldSelector();
        if (this.conversionsMap == null) {
            this.conversionsMap = new LinkedHashMap<FieldSelector, Conversion<String, ?>[]>();
        }
        this.conversionsMap.put(selector, conversions);
        this.conversionSequence.add(selector);
        if (selector instanceof FieldSet) {
            return (FieldSet)((Object)selector);
        }
        return null;
    }

    protected abstract FieldSelector newFieldSelector();

    public void prepareExecution(boolean writing, FieldSelector selector, Map<Integer, List<Conversion<?, ?>>> conversionsByIndex, String[] values) {
        if (this.conversionsMap == null) {
            return;
        }
        Conversion<String, ?>[] conversions = this.conversionsMap.get(selector);
        if (conversions == null) {
            return;
        }
        int[] fieldIndexes = selector.getFieldIndexes(NormalizedString.toIdentifierGroupArray(values));
        if (fieldIndexes == null) {
            fieldIndexes = ArgumentUtils.toIntArray(conversionsByIndex.keySet());
        }
        for (int fieldIndex : fieldIndexes) {
            List<Conversion<?, ?>> conversionsAtIndex = conversionsByIndex.get(fieldIndex);
            if (conversionsAtIndex == null) {
                conversionsAtIndex = new ArrayList();
                conversionsByIndex.put(fieldIndex, conversionsAtIndex);
            }
            AbstractConversionMapping.validateDuplicates(selector, conversionsAtIndex, conversions);
            conversionsAtIndex.addAll(Arrays.asList(conversions));
        }
    }

    private static void validateDuplicates(FieldSelector selector, List<Conversion<?, ?>> conversionsAtIndex, Conversion<?, ?>[] conversionsToAdd) {
        for (Conversion<?, ?> toAdd : conversionsToAdd) {
            for (Conversion<?, ?> existing : conversionsAtIndex) {
                if (toAdd != existing) continue;
                throw new DataProcessingException("Duplicate conversion " + toAdd.getClass().getName() + " being applied to " + selector.describe());
            }
        }
    }

    public boolean isEmpty() {
        return this.conversionsMap == null || this.conversionsMap.isEmpty();
    }

    public AbstractConversionMapping<T> clone() {
        try {
            return (AbstractConversionMapping)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    public AbstractConversionMapping<T> clone(Map<FieldSelector, FieldSelector> clonedSelectors, List<FieldSelector> clonedConversionSequence) {
        Object out = this.clone();
        ((AbstractConversionMapping)out).conversionSequence = clonedConversionSequence;
        if (this.conversionsMap != null) {
            ((AbstractConversionMapping)out).conversionsMap = new HashMap<FieldSelector, Conversion<String, ?>[]>();
            for (FieldSelector selector : this.conversionSequence) {
                FieldSelector clone = clonedSelectors.get(selector);
                if (clone == null) {
                    throw new IllegalStateException("Internal error cloning conversion mappings");
                }
                Conversion<String, ?>[] conversions = this.conversionsMap.get(selector);
                ((AbstractConversionMapping)out).conversionsMap.put(clone, conversions);
            }
        }
        return out;
    }
}

