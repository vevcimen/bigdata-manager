/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.fields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import shadeio.univocity.parsers.annotations.helpers.AnnotationHelper;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.common.fields.AbstractConversionMapping;
import shadeio.univocity.parsers.common.fields.AllIndexesSelector;
import shadeio.univocity.parsers.common.fields.FieldEnumSelector;
import shadeio.univocity.parsers.common.fields.FieldIndexSelector;
import shadeio.univocity.parsers.common.fields.FieldNameSelector;
import shadeio.univocity.parsers.common.fields.FieldSelector;
import shadeio.univocity.parsers.common.fields.FieldSet;
import shadeio.univocity.parsers.conversions.Conversion;
import shadeio.univocity.parsers.conversions.ValidatedConversion;

public class FieldConversionMapping
implements Cloneable {
    private static final Conversion[] EMPTY_CONVERSION_ARRAY = new Conversion[0];
    public int[] validatedIndexes;
    private List<FieldSelector> conversionSequence = new ArrayList<FieldSelector>();
    private AbstractConversionMapping<String> fieldNameConversionMapping = new AbstractConversionMapping<String>(this.conversionSequence){

        @Override
        protected FieldSelector newFieldSelector() {
            return new FieldNameSelector();
        }
    };
    private AbstractConversionMapping<Integer> fieldIndexConversionMapping = new AbstractConversionMapping<Integer>(this.conversionSequence){

        @Override
        protected FieldSelector newFieldSelector() {
            return new FieldIndexSelector();
        }
    };
    private AbstractConversionMapping<Enum> fieldEnumConversionMapping = new AbstractConversionMapping<Enum>(this.conversionSequence){

        @Override
        protected FieldSelector newFieldSelector() {
            return new FieldEnumSelector();
        }
    };
    private AbstractConversionMapping<Integer> convertAllMapping = new AbstractConversionMapping<Integer>(this.conversionSequence){

        @Override
        protected FieldSelector newFieldSelector() {
            return new AllIndexesSelector();
        }
    };
    private Map<Integer, List<Conversion<?, ?>>> conversionsByIndex = Collections.emptyMap();
    private Map<Integer, List<ValidatedConversion>> validationsByIndex = Collections.emptyMap();

    public void prepareExecution(boolean writing, String[] values) {
        if (this.fieldNameConversionMapping.isEmpty() && this.fieldEnumConversionMapping.isEmpty() && this.fieldIndexConversionMapping.isEmpty() && this.convertAllMapping.isEmpty()) {
            return;
        }
        if (!this.conversionsByIndex.isEmpty()) {
            return;
        }
        this.conversionsByIndex = new HashMap();
        for (FieldSelector next : this.conversionSequence) {
            this.fieldNameConversionMapping.prepareExecution(writing, next, this.conversionsByIndex, values);
            this.fieldIndexConversionMapping.prepareExecution(writing, next, this.conversionsByIndex, values);
            this.fieldEnumConversionMapping.prepareExecution(writing, next, this.conversionsByIndex, values);
            this.convertAllMapping.prepareExecution(writing, next, this.conversionsByIndex, values);
        }
        Iterator<Map.Entry<Integer, List<Conversion<?, ?>>>> entryIterator = this.conversionsByIndex.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<Integer, List<Conversion<?, ?>>> e = entryIterator.next();
            Iterator<Conversion<?, ?>> it = e.getValue().iterator();
            while (it.hasNext()) {
                Conversion<?, ?> conversion = it.next();
                if (!(conversion instanceof ValidatedConversion)) continue;
                if (this.validationsByIndex.isEmpty()) {
                    this.validationsByIndex = new TreeMap<Integer, List<ValidatedConversion>>();
                }
                it.remove();
                List<ValidatedConversion> validations = this.validationsByIndex.get(e.getKey());
                if (validations == null) {
                    validations = new ArrayList<ValidatedConversion>(1);
                    this.validationsByIndex.put(e.getKey(), validations);
                }
                validations.add((ValidatedConversion)conversion);
            }
            if (!e.getValue().isEmpty()) continue;
            entryIterator.remove();
        }
        this.validatedIndexes = ArgumentUtils.toIntArray(this.validationsByIndex.keySet());
    }

    public void applyConversionsOnAllFields(Conversion<String, ?> ... conversions) {
        this.convertAllMapping.registerConversions(conversions);
    }

    public FieldSet<Integer> applyConversionsOnFieldIndexes(Conversion<String, ?> ... conversions) {
        return this.fieldIndexConversionMapping.registerConversions(conversions);
    }

    public FieldSet<String> applyConversionsOnFieldNames(Conversion<String, ?> ... conversions) {
        return this.fieldNameConversionMapping.registerConversions(conversions);
    }

    public FieldSet<Enum> applyConversionsOnFieldEnums(Conversion<String, ?> ... conversions) {
        return this.fieldEnumConversionMapping.registerConversions(conversions);
    }

    public void executeValidations(int index, Object value) {
        List<ValidatedConversion> validations = this.validationsByIndex.get(index);
        if (validations != null) {
            for (int i = 0; i < validations.size(); ++i) {
                validations.get(i).execute(value);
            }
        }
    }

    public Object reverseConversions(boolean executeInReverseOrder, int index, Object value, boolean[] convertedFlags) {
        List<Conversion<?, ?>> conversions = this.conversionsByIndex.get(index);
        if (conversions != null) {
            if (convertedFlags != null) {
                convertedFlags[index] = true;
            }
            Conversion<?, ?> conversion = null;
            try {
                if (executeInReverseOrder) {
                    for (int i = conversions.size() - 1; i >= 0; --i) {
                        conversion = conversions.get(i);
                        value = conversion.revert(value);
                    }
                } else {
                    for (Conversion<?, ?> c : conversions) {
                        conversion = c;
                        value = conversion.revert(value);
                    }
                }
            }
            catch (DataProcessingException ex) {
                ex.setValue(value);
                ex.setColumnIndex(index);
                ex.markAsNonFatal();
                throw ex;
            }
            catch (Throwable ex) {
                DataProcessingException exception = conversion != null ? new DataProcessingException("Error converting value '{value}' using conversion " + conversion.getClass().getName(), ex) : new DataProcessingException("Error converting value '{value}'", ex);
                exception.setValue(value);
                exception.setColumnIndex(index);
                exception.markAsNonFatal();
                throw exception;
            }
        }
        return value;
    }

    public Object applyConversions(int index, String stringValue, boolean[] convertedFlags) {
        List<Conversion<?, ?>> conversions = this.conversionsByIndex.get(index);
        if (conversions != null) {
            if (convertedFlags != null) {
                convertedFlags[index] = true;
            }
            String result = stringValue;
            for (Conversion<?, ?> conversion : conversions) {
                try {
                    result = conversion.execute(result);
                }
                catch (DataProcessingException ex) {
                    ex.setColumnIndex(index);
                    ex.markAsNonFatal();
                    throw ex;
                }
                catch (Throwable ex) {
                    DataProcessingException exception = new DataProcessingException("Error converting value '{value}' using conversion " + conversion.getClass().getName(), ex);
                    exception.setValue(result);
                    exception.setColumnIndex(index);
                    exception.markAsNonFatal();
                    throw exception;
                }
            }
            return result;
        }
        return stringValue;
    }

    public Conversion[] getConversions(int index, Class<?> expectedType) {
        Conversion[] out;
        List<Conversion<?, ?>> conversions = this.conversionsByIndex.get(index);
        if (conversions != null) {
            out = new Conversion[conversions.size()];
            int i = 0;
            for (Conversion<?, ?> conversion : conversions) {
                out[i++] = conversion;
            }
        } else {
            if (expectedType == String.class) {
                return EMPTY_CONVERSION_ARRAY;
            }
            out = new Conversion[]{AnnotationHelper.getDefaultConversion(expectedType, null, null)};
            if (out[0] == null) {
                return EMPTY_CONVERSION_ARRAY;
            }
        }
        return out;
    }

    public FieldConversionMapping clone() {
        try {
            FieldConversionMapping out = (FieldConversionMapping)super.clone();
            out.validatedIndexes = this.validatedIndexes == null ? null : (int[])this.validatedIndexes.clone();
            out.conversionSequence = new ArrayList<FieldSelector>();
            HashMap<FieldSelector, FieldSelector> clonedSelectors = new HashMap<FieldSelector, FieldSelector>();
            for (FieldSelector selector : this.conversionSequence) {
                FieldSelector clone = (FieldSelector)selector.clone();
                out.conversionSequence.add(clone);
                clonedSelectors.put(selector, clone);
            }
            out.fieldNameConversionMapping = this.fieldNameConversionMapping.clone(clonedSelectors, out.conversionSequence);
            out.fieldIndexConversionMapping = this.fieldIndexConversionMapping.clone(clonedSelectors, out.conversionSequence);
            out.fieldEnumConversionMapping = this.fieldEnumConversionMapping.clone(clonedSelectors, out.conversionSequence);
            out.convertAllMapping = this.convertAllMapping.clone(clonedSelectors, out.conversionSequence);
            out.conversionsByIndex = new HashMap(this.conversionsByIndex);
            out.validationsByIndex = new TreeMap<Integer, List<ValidatedConversion>>(this.validationsByIndex);
            return out;
        }
        catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}

