/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import shadeio.univocity.parsers.annotations.EnumOptions;
import shadeio.univocity.parsers.annotations.HeaderTransformer;
import shadeio.univocity.parsers.annotations.Nested;
import shadeio.univocity.parsers.annotations.Parsed;
import shadeio.univocity.parsers.annotations.helpers.AnnotationHelper;
import shadeio.univocity.parsers.annotations.helpers.AnnotationRegistry;
import shadeio.univocity.parsers.annotations.helpers.FieldMapping;
import shadeio.univocity.parsers.annotations.helpers.MethodDescriptor;
import shadeio.univocity.parsers.annotations.helpers.MethodFilter;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.common.DefaultConversionProcessor;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.beans.PropertyWrapper;
import shadeio.univocity.parsers.common.fields.ColumnMapper;
import shadeio.univocity.parsers.common.fields.ColumnMapping;
import shadeio.univocity.parsers.common.fields.FieldConversionMapping;
import shadeio.univocity.parsers.conversions.Conversion;
import shadeio.univocity.parsers.conversions.EnumConversion;

public class BeanConversionProcessor<T>
extends DefaultConversionProcessor {
    final Class<T> beanClass;
    final Constructor<T> constructor;
    protected final Set<FieldMapping> parsedFields = new LinkedHashSet<FieldMapping>();
    private int lastFieldIndexMapped = -1;
    private FieldMapping[] readOrder;
    private FieldMapping[] missing;
    private Object[] valuesForMissing;
    protected boolean initialized = false;
    boolean strictHeaderValidationEnabled = false;
    private NormalizedString[] syntheticHeaders = null;
    private Object[] row;
    private Map<FieldMapping, BeanConversionProcessor<?>> nestedAttributes = null;
    protected final HeaderTransformer transformer;
    protected final MethodFilter methodFilter;
    private ColumnMapping columnMapper = new ColumnMapping();
    private boolean mappingsForWritingValidated = false;

    @Deprecated
    public BeanConversionProcessor(Class<T> beanType) {
        this(beanType, null, MethodFilter.ONLY_SETTERS);
    }

    public BeanConversionProcessor(Class<T> beanType, MethodFilter methodFilter) {
        this(beanType, null, methodFilter);
    }

    BeanConversionProcessor(Class<T> beanType, HeaderTransformer transformer, MethodFilter methodFilter) {
        this.beanClass = beanType;
        this.transformer = transformer;
        this.methodFilter = methodFilter;
        Constructor<?> c = null;
        for (Constructor<?> constructor : this.beanClass.getDeclaredConstructors()) {
            if (constructor.getParameterTypes().length != 0) continue;
            c = constructor;
            break;
        }
        if (c != null && !c.isAccessible()) {
            c.setAccessible(true);
        }
        this.constructor = c;
    }

    public boolean isStrictHeaderValidationEnabled() {
        return this.strictHeaderValidationEnabled;
    }

    public final void initialize() {
        this.initialize((NormalizedString[])null);
    }

    public final ColumnMapper getColumnMapper() {
        return this.columnMapper;
    }

    protected final void initialize(String[] headers) {
        this.initialize(NormalizedString.toArray(headers));
    }

    protected final void initialize(NormalizedString[] headers) {
        if (!this.initialized) {
            this.initialized = true;
            Map<Field, PropertyWrapper> allFields = AnnotationHelper.getAllFields(this.beanClass);
            Set<String> nestedFields = this.columnMapper.getNestedAttributeNames();
            for (String string : nestedFields) {
                for (Map.Entry<Field, PropertyWrapper> e : allFields.entrySet()) {
                    Nested nested;
                    Field field = e.getKey();
                    if (!field.getName().equals(string) || (nested = AnnotationHelper.findAnnotation(field, Nested.class)) != null) continue;
                    this.processNestedField(field.getType(), field, field.getName(), e.getValue(), headers, null);
                }
            }
            for (Map.Entry entry : allFields.entrySet()) {
                Field field = (Field)entry.getKey();
                PropertyWrapper property = (PropertyWrapper)entry.getValue();
                this.processField(field, field.getName(), property, headers);
            }
            for (Method method : AnnotationHelper.getAllMethods(this.beanClass, this.methodFilter)) {
                this.processField(method, method.getName(), null, headers);
            }
            this.readOrder = null;
            this.lastFieldIndexMapped = -1;
            this.identifyLiterals();
            this.validateMappings();
        }
    }

    private void identifyLiterals() {
        int i;
        NormalizedString[] fieldNames = new NormalizedString[this.parsedFields.size()];
        FieldMapping[] fields = this.parsedFields.toArray(new FieldMapping[0]);
        for (i = 0; i < fieldNames.length; ++i) {
            fieldNames[i] = fields[i].getFieldName();
        }
        if (NormalizedString.identifyLiterals(fieldNames)) {
            for (i = 0; i < fieldNames.length; ++i) {
                fields[i].setFieldName(fieldNames[i]);
            }
        }
    }

    public void setStrictHeaderValidationEnabled(boolean strictHeaderValidationEnabled) {
        this.strictHeaderValidationEnabled = strictHeaderValidationEnabled;
    }

    void processField(AnnotatedElement element, String targetName, PropertyWrapper propertyDescriptor, NormalizedString[] headers) {
        Nested nested;
        FieldMapping mapping = null;
        Parsed annotation = AnnotationHelper.findAnnotation(element, Parsed.class);
        if (annotation != null && this.processField(mapping = new FieldMapping(this.beanClass, element, propertyDescriptor, this.transformer, headers))) {
            this.parsedFields.add(mapping);
            this.setupConversions(element, mapping);
        }
        MethodDescriptor descriptor = null;
        if (element instanceof Method) {
            descriptor = this.methodFilter.toDescriptor(this.columnMapper.getPrefix(), (Method)element);
        }
        if (this.columnMapper.isMapped(descriptor, targetName)) {
            if (mapping == null) {
                mapping = new FieldMapping(this.beanClass, element, propertyDescriptor, this.transformer, headers);
                this.columnMapper.updateMapping(mapping, targetName, descriptor);
                this.parsedFields.add(mapping);
                this.setupConversions(element, mapping);
            } else {
                this.columnMapper.updateMapping(mapping, targetName, descriptor);
            }
        }
        if ((nested = AnnotationHelper.findAnnotation(element, Nested.class)) != null) {
            Class<?> nestedType = AnnotationRegistry.getValue(element, nested, "type", nested.type());
            if (nestedType == Object.class) {
                nestedType = AnnotationHelper.getType(element);
            }
            this.processNestedField(nestedType, element, targetName, propertyDescriptor, headers, nested);
        }
    }

    private void processNestedField(Class nestedType, AnnotatedElement element, String targetName, PropertyWrapper propertyDescriptor, NormalizedString[] headers, Nested nested) {
        Class<? extends HeaderTransformer> transformerType;
        HeaderTransformer transformer = null;
        if (nested != null && (transformerType = AnnotationRegistry.getValue(element, nested, "headerTransformer", nested.headerTransformer())) != HeaderTransformer.class) {
            String[] args = AnnotationRegistry.getValue(element, nested, "args", nested.args());
            transformer = AnnotationHelper.newInstance(HeaderTransformer.class, transformerType, args);
        }
        FieldMapping mapping = new FieldMapping(nestedType, element, propertyDescriptor, null, headers);
        BeanConversionProcessor<?> processor = this.createNestedProcessor(nested, nestedType, mapping, transformer);
        processor.conversions = this.conversions == null ? null : this.cloneConversions();
        processor.columnMapper = new ColumnMapping(targetName, this.columnMapper);
        processor.initialize(headers);
        this.getNestedAttributes().put(mapping, processor);
    }

    protected FieldConversionMapping cloneConversions() {
        return this.conversions.clone();
    }

    Map<FieldMapping, BeanConversionProcessor<?>> getNestedAttributes() {
        if (this.nestedAttributes == null) {
            this.nestedAttributes = new LinkedHashMap();
        }
        return this.nestedAttributes;
    }

    BeanConversionProcessor<?> createNestedProcessor(Annotation annotation, Class nestedType, FieldMapping fieldMapping, HeaderTransformer transformer) {
        return new BeanConversionProcessor<T>(nestedType, transformer, this.methodFilter);
    }

    protected boolean processField(FieldMapping field) {
        return true;
    }

    void validateMappings() {
        HashMap<NormalizedString, FieldMapping> mappedNames = new HashMap<NormalizedString, FieldMapping>();
        HashMap<Integer, FieldMapping> mappedIndexes = new HashMap<Integer, FieldMapping>();
        HashSet<FieldMapping> duplicateNames = new HashSet<FieldMapping>();
        HashSet<FieldMapping> duplicateIndexes = new HashSet<FieldMapping>();
        for (FieldMapping mapping : this.parsedFields) {
            NormalizedString name = mapping.getFieldName();
            int index = mapping.getIndex();
            if (index != -1) {
                if (mappedIndexes.containsKey(index)) {
                    duplicateIndexes.add(mapping);
                    duplicateIndexes.add((FieldMapping)mappedIndexes.get(index));
                    continue;
                }
                mappedIndexes.put(index, mapping);
                continue;
            }
            if (mappedNames.containsKey(name)) {
                duplicateNames.add(mapping);
                duplicateNames.add((FieldMapping)mappedNames.get(name));
                continue;
            }
            mappedNames.put(name, mapping);
        }
        if (duplicateIndexes.size() > 0 || duplicateNames.size() > 0) {
            StringBuilder msg = new StringBuilder("Conflicting field mappings defined in annotated class: " + this.getBeanClass().getName());
            for (FieldMapping mapping : duplicateIndexes) {
                msg.append("\n\tIndex: '").append(mapping.getIndex()).append("' of  ").append(BeanConversionProcessor.describeField(mapping.getTarget()));
            }
            for (FieldMapping mapping : duplicateNames) {
                msg.append("\n\tName: '").append(mapping.getFieldName()).append("' of ").append(BeanConversionProcessor.describeField(mapping.getTarget()));
            }
            throw new DataProcessingException(msg.toString());
        }
    }

    static String describeField(AnnotatedElement target) {
        if (target instanceof Method) {
            return "method: " + target;
        }
        return "field '" + AnnotationHelper.getName(target) + "' (" + AnnotationHelper.getType(target).getName() + ')';
    }

    private void setupConversions(AnnotatedElement target, FieldMapping mapping) {
        Conversion defaultConversion;
        Parsed parsed;
        boolean applyDefaultConversion;
        List<Annotation> annotations = AnnotationHelper.findAllAnnotationsInPackage(target, Parsed.class.getPackage());
        Conversion lastConversion = null;
        if (!annotations.isEmpty()) {
            Class<?> targetType = AnnotationHelper.getType(target);
            Parsed parsed2 = target == null ? null : AnnotationHelper.findAnnotation(target, Parsed.class);
            String nullRead = AnnotationHelper.getNullReadValue(target, parsed2);
            String nullWrite = AnnotationHelper.getNullWriteValue(target, parsed2);
            for (Annotation annotation : annotations) {
                try {
                    Conversion conversion = AnnotationHelper.getConversion(targetType, target, annotation, nullRead, nullWrite);
                    if (conversion == null) continue;
                    this.addConversion(conversion, mapping);
                    lastConversion = conversion;
                }
                catch (Throwable ex) {
                    String path = annotation.annotationType().getSimpleName() + "' of field " + mapping;
                    throw new DataProcessingException("Error processing annotation '" + path + ". " + ex.getMessage(), ex);
                }
            }
            if (targetType.isEnum()) {
                boolean hasEnumOptions = false;
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType() != EnumOptions.class) continue;
                    hasEnumOptions = true;
                }
                if (!hasEnumOptions) {
                    EnumConversion conversion = AnnotationHelper.createDefaultEnumConversion(targetType, nullRead, nullWrite);
                    this.addConversion(conversion, mapping);
                    lastConversion = conversion;
                }
            }
        }
        boolean bl = applyDefaultConversion = (parsed = AnnotationHelper.findAnnotation(target, Parsed.class)) == null || AnnotationRegistry.getValue(target, parsed, "applyDefaultConversion", parsed.applyDefaultConversion()) != false;
        if (applyDefaultConversion && this.applyDefaultConversion(lastConversion, defaultConversion = AnnotationHelper.getDefaultConversion(target))) {
            this.addConversion(defaultConversion, mapping);
        }
    }

    private boolean applyDefaultConversion(Conversion lastConversionApplied, Conversion defaultConversion) {
        if (defaultConversion == null) {
            return false;
        }
        if (lastConversionApplied == null) {
            return true;
        }
        if (lastConversionApplied.getClass() == defaultConversion.getClass()) {
            return false;
        }
        Method execute = this.getConversionMethod(lastConversionApplied, "execute");
        Method revert = this.getConversionMethod(lastConversionApplied, "revert");
        Method defaultExecute = this.getConversionMethod(defaultConversion, "execute");
        Method defaultRevert = this.getConversionMethod(defaultConversion, "revert");
        return execute.getReturnType() != defaultExecute.getReturnType() || revert.getReturnType() != defaultRevert.getReturnType();
    }

    private Method getConversionMethod(Conversion conversion, String methodName) {
        Method targetMethod = null;
        for (Method method : conversion.getClass().getMethods()) {
            if (!method.getName().equals(methodName) || method.isSynthetic() || method.isBridge() || (method.getModifiers() & 1) != 1 || method.getParameterTypes().length != 1 || method.getReturnType() == Void.TYPE) continue;
            if (targetMethod != null) {
                throw new DataProcessingException("Unable to convert values for class '" + this.beanClass + "'. Multiple '" + methodName + "' methods defined in conversion " + conversion.getClass() + '.');
            }
            targetMethod = method;
        }
        if (targetMethod != null) {
            return targetMethod;
        }
        throw new DataProcessingException("Unable to convert values for class '" + this.beanClass + "'. Cannot find method '" + methodName + "' in conversion " + conversion.getClass() + '.');
    }

    protected void addConversion(Conversion conversion, FieldMapping mapping) {
        if (conversion == null) {
            return;
        }
        if (mapping.isMappedToIndex()) {
            this.convertIndexes(conversion).add((Integer[])new Integer[]{mapping.getIndex()});
        } else {
            this.convertFields(conversion).add((String[])new String[]{NormalizedString.valueOf(mapping.getFieldName())});
        }
    }

    void mapValuesToFields(T instance, Object[] row, Context context) {
        Object value;
        FieldMapping field;
        int i;
        if (row.length > this.lastFieldIndexMapped) {
            this.lastFieldIndexMapped = row.length;
            this.mapFieldIndexes(context, row, NormalizedString.toIdentifierGroupArray(context.headers()), context.extractedFieldIndexes(), context.columnsReordered());
        }
        int last = row.length < this.readOrder.length ? row.length : this.readOrder.length;
        for (i = 0; i < last; ++i) {
            field = this.readOrder[i];
            if (field == null) continue;
            value = row[i];
            field.write(instance, value);
        }
        if (this.conversions != null && row.length < this.readOrder.length) {
            for (i = last; i < this.readOrder.length; ++i) {
                field = this.readOrder[i];
                if (field == null) continue;
                value = this.conversions.applyConversions(i, null, null);
                field.write(instance, value);
            }
        }
        if (this.missing != null) {
            for (i = 0; i < this.missing.length; ++i) {
                Object value2 = this.valuesForMissing[i];
                if (value2 == null) continue;
                FieldMapping field2 = this.missing[i];
                field2.write(instance, value2);
            }
        }
    }

    private void mapFieldIndexes(Context context, Object[] row, NormalizedString[] headers, int[] indexes, boolean columnsReordered) {
        if (headers == null) {
            headers = ArgumentUtils.EMPTY_NORMALIZED_STRING_ARRAY;
        }
        boolean boundToIndex = false;
        int last = headers.length > row.length ? headers.length : row.length;
        for (FieldMapping mapping : this.parsedFields) {
            int index = mapping.getIndex();
            if (last > index) continue;
            last = index;
            boundToIndex = true;
        }
        if (boundToIndex) {
            ++last;
        }
        FieldMapping[] fieldOrder = new FieldMapping[last];
        TreeSet<NormalizedString> fieldsNotFound = new TreeSet<NormalizedString>();
        for (FieldMapping mapping : this.parsedFields) {
            if (mapping.isMappedToField()) {
                int[] positions = ArgumentUtils.indexesOf(headers, mapping.getFieldName());
                if (positions.length == 0) {
                    fieldsNotFound.add(mapping.getFieldName());
                    continue;
                }
                for (int i = 0; i < positions.length; ++i) {
                    fieldOrder[positions[i]] = mapping;
                }
                continue;
            }
            if (mapping.getIndex() >= fieldOrder.length) continue;
            fieldOrder[mapping.getIndex()] = mapping;
        }
        if (context != null && !fieldsNotFound.isEmpty()) {
            if (headers.length == 0) {
                throw new DataProcessingException("Could not find fields " + fieldsNotFound.toString() + " in input. Please enable header extraction in the parser settings in order to match field names.");
            }
            if (this.strictHeaderValidationEnabled) {
                DataProcessingException exception = new DataProcessingException("Could not find fields " + fieldsNotFound.toString() + "' in input. Names found: {headers}");
                exception.setValue("headers", Arrays.toString(headers));
                throw exception;
            }
        }
        if (indexes != null) {
            for (int i = 0; i < fieldOrder.length; ++i) {
                boolean isIndexUsed = false;
                for (int j = 0; j < indexes.length; ++j) {
                    if (indexes[j] != i) continue;
                    isIndexUsed = true;
                    break;
                }
                if (isIndexUsed) continue;
                fieldOrder[i] = null;
            }
            if (columnsReordered) {
                FieldMapping[] newFieldOrder = new FieldMapping[indexes.length];
                for (int i = 0; i < indexes.length; ++i) {
                    for (int j = 0; j < fieldOrder.length; ++j) {
                        FieldMapping field;
                        int index = indexes[i];
                        if (index == -1) continue;
                        newFieldOrder[i] = field = fieldOrder[index];
                    }
                }
                fieldOrder = newFieldOrder;
            }
        }
        this.readOrder = fieldOrder;
        this.initializeValuesForMissing();
    }

    private int nonNullReadOrderLength() {
        int count = 0;
        for (int i = 0; i < this.readOrder.length; ++i) {
            if (this.readOrder[i] == null) continue;
            ++count;
        }
        return count;
    }

    private void initializeValuesForMissing() {
        if (this.nonNullReadOrderLength() < this.parsedFields.size()) {
            LinkedHashSet<FieldMapping> unmapped = new LinkedHashSet<FieldMapping>(this.parsedFields);
            unmapped.removeAll(Arrays.asList(this.readOrder));
            this.missing = unmapped.toArray(new FieldMapping[0]);
            String[] headers = new String[this.missing.length];
            BeanConversionProcessor tmp = new BeanConversionProcessor(this.getBeanClass(), this.methodFilter){

                @Override
                protected void addConversion(Conversion conversion, FieldMapping mapping) {
                    if (conversion == null) {
                        return;
                    }
                    this.convertFields(conversion).add((String[])new String[]{NormalizedString.valueOf(mapping.getFieldName())});
                }
            };
            for (int i = 0; i < this.missing.length; ++i) {
                FieldMapping mapping = this.missing[i];
                if (this.processField(mapping)) {
                    super.setupConversions(mapping.getTarget(), mapping);
                }
                headers[i] = NormalizedString.valueOf(mapping.getFieldName());
            }
            tmp.initializeConversions(headers, null);
            this.valuesForMissing = tmp.applyConversions(new String[this.missing.length], null);
        } else {
            this.missing = null;
            this.valuesForMissing = null;
        }
    }

    public T createBean(String[] row, Context context) {
        T instance;
        Object[] convertedRow = super.applyConversions(row, context);
        if (convertedRow == null) {
            return null;
        }
        try {
            instance = this.constructor.newInstance(new Object[0]);
        }
        catch (Throwable e) {
            throw new DataProcessingException("Unable to instantiate class '" + this.beanClass.getName() + '\'', row, e);
        }
        this.mapValuesToFields(instance, convertedRow, context);
        if (this.nestedAttributes != null) {
            this.processNestedAttributes(row, instance, context);
        }
        return instance;
    }

    void processNestedAttributes(String[] row, Object instance, Context context) {
        for (Map.Entry<FieldMapping, BeanConversionProcessor<?>> e : this.nestedAttributes.entrySet()) {
            Object nested = e.getValue().createBean(row, context);
            if (nested == null) continue;
            e.getKey().write(instance, nested);
        }
    }

    private void mapFieldsToValues(T instance, Object[] row, NormalizedString[] headers, int[] indexes, boolean columnsReordered) {
        if (row.length > this.lastFieldIndexMapped) {
            this.mapFieldIndexes(null, row, headers, indexes, columnsReordered);
        }
        int last = row.length < this.readOrder.length ? row.length : this.readOrder.length;
        for (int i = 0; i < last; ++i) {
            FieldMapping field = this.readOrder[i];
            if (field == null) continue;
            try {
                row[i] = field.read(instance);
                continue;
            }
            catch (Throwable e) {
                if (!this.beanClass.isAssignableFrom(instance.getClass())) {
                    this.handleConversionError(e, new Object[]{instance}, -1);
                    throw this.toDataProcessingException(e, row, i);
                }
                if (this.handleConversionError(e, row, i)) continue;
                throw this.toDataProcessingException(e, row, i);
            }
        }
    }

    public final Object[] reverseConversions(T bean, NormalizedString[] headers, int[] indexesToWrite) {
        if (!this.mappingsForWritingValidated) {
            this.mappingsForWritingValidated = true;
            this.validateMappingsForWriting();
        }
        if (bean == null) {
            return null;
        }
        if (this.row == null) {
            if (headers != null) {
                this.row = new Object[headers.length];
            } else if (indexesToWrite != null) {
                int minimumRowLength = 0;
                for (int index : indexesToWrite) {
                    if (index + 1 <= minimumRowLength) continue;
                    minimumRowLength = index + 1;
                }
                if (minimumRowLength < indexesToWrite.length) {
                    minimumRowLength = indexesToWrite.length;
                }
                this.row = new Object[minimumRowLength];
            } else {
                HashSet<Integer> assignedIndexes = new HashSet<Integer>();
                int lastIndex = -1;
                for (FieldMapping f : this.parsedFields) {
                    if (lastIndex < f.getIndex() + 1) {
                        lastIndex = f.getIndex() + 1;
                    }
                    assignedIndexes.add(f.getIndex());
                }
                if (lastIndex < this.parsedFields.size()) {
                    lastIndex = this.parsedFields.size();
                }
                this.row = new Object[lastIndex];
                if (this.syntheticHeaders == null) {
                    this.syntheticHeaders = new NormalizedString[lastIndex];
                    Iterator<FieldMapping> it = this.parsedFields.iterator();
                    for (int i = 0; i < lastIndex; ++i) {
                        if (assignedIndexes.contains(i)) continue;
                        NormalizedString fieldName = null;
                        while (it.hasNext() && (fieldName = it.next().getFieldName()) == null) {
                        }
                        this.syntheticHeaders[i] = fieldName;
                    }
                }
            }
        }
        if (this.nestedAttributes != null) {
            for (Map.Entry<FieldMapping, BeanConversionProcessor<?>> e : this.nestedAttributes.entrySet()) {
                Object nested = e.getKey().read(bean);
                if (nested == null) continue;
                BeanConversionProcessor<?> nestedProcessor = e.getValue();
                nestedProcessor.row = this.row;
                nestedProcessor.reverseConversions(nested, headers, indexesToWrite);
            }
        }
        NormalizedString[] normalizedHeaders = NormalizedString.toIdentifierGroupArray(headers);
        if (this.syntheticHeaders != null) {
            normalizedHeaders = this.syntheticHeaders;
        }
        try {
            this.mapFieldsToValues(bean, this.row, normalizedHeaders, indexesToWrite, false);
        }
        catch (Throwable ex) {
            if (ex instanceof DataProcessingException) {
                DataProcessingException error = (DataProcessingException)ex;
                if (error.isHandled()) {
                    return null;
                }
                throw error;
            }
            if (!this.handleConversionError(ex, this.row, -1)) {
                throw this.toDataProcessingException(ex, this.row, -1);
            }
            return null;
        }
        if (super.reverseConversions(true, this.row, normalizedHeaders, indexesToWrite)) {
            return this.row;
        }
        return null;
    }

    public Class<T> getBeanClass() {
        return this.beanClass;
    }

    public void setColumnMapper(ColumnMapper columnMapper) {
        this.columnMapper = columnMapper == null ? new ColumnMapping() : (ColumnMapping)columnMapper.clone();
    }

    private void validateMappingsForWriting() {
        TreeMap<Object, Integer> targetCounts = new TreeMap<Object, Integer>();
        HashMap<Object, String> targetSources = new HashMap<Object, String>();
        this.populateTargetMaps(targetCounts, targetSources);
        StringBuilder msg = new StringBuilder();
        for (Map.Entry e : targetCounts.entrySet()) {
            if ((Integer)e.getValue() <= 1) continue;
            String sources = (String)targetSources.get(e.getKey());
            if (msg.length() > 0) {
                msg.append("\n");
            }
            msg.append('\t');
            msg.append(e.getKey());
            msg.append(": ");
            msg.append(sources);
        }
        if (msg.length() > 0) {
            throw new DataProcessingException("Cannot write object as multiple attributes/methods have been mapped to the same output column:\n" + msg.toString());
        }
    }

    private void populateTargetMaps(Map<Object, Integer> targetCounts, Map<Object, String> targetSources) {
        for (FieldMapping fieldMapping : this.parsedFields) {
            NormalizedString outputColumn = fieldMapping.getIndex() == -1 ? fieldMapping.getFieldName() : NormalizedString.valueOf("Column #" + fieldMapping.getIndex());
            Integer count = targetCounts.get(outputColumn);
            if (count == null) {
                count = 0;
            }
            Integer n = count;
            Integer n2 = count = Integer.valueOf(count + 1);
            targetCounts.put(outputColumn, count);
            String str = targetSources.get(outputColumn);
            String sourceName = fieldMapping.getTarget() instanceof Method ? ((Method)fieldMapping.getTarget()).getName() : ((Field)fieldMapping.getTarget()).getName();
            if (!this.columnMapper.getPrefix().isEmpty()) {
                sourceName = this.columnMapper.getPrefix() + '.' + sourceName;
            }
            str = str == null ? sourceName : str + ", " + sourceName;
            targetSources.put(outputColumn, str);
        }
        if (this.nestedAttributes != null) {
            for (BeanConversionProcessor beanConversionProcessor : this.nestedAttributes.values()) {
                beanConversionProcessor.populateTargetMaps(targetCounts, targetSources);
            }
        }
    }
}

