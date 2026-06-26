/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.record;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import shadeio.univocity.parsers.annotations.BooleanString;
import shadeio.univocity.parsers.annotations.Format;
import shadeio.univocity.parsers.annotations.helpers.AnnotationHelper;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.fields.FieldConversionMapping;
import shadeio.univocity.parsers.common.fields.FieldSet;
import shadeio.univocity.parsers.common.record.MetaData;
import shadeio.univocity.parsers.common.record.RecordMetaData;
import shadeio.univocity.parsers.conversions.Conversion;

class RecordMetaDataImpl<C extends Context>
implements RecordMetaData {
    final C context;
    private Map<Class, Conversion> conversionByType = new HashMap<Class, Conversion>();
    private Map<Class, Map<Annotation, Conversion>> conversionsByAnnotation = new HashMap<Class, Map<Annotation, Conversion>>();
    private Map<Integer, Annotation> annotationHashes = new HashMap<Integer, Annotation>();
    private MetaData[] indexMap;
    private FieldConversionMapping conversions = null;

    RecordMetaDataImpl(C context) {
        this.context = context;
    }

    private MetaData getMetaData(String name) {
        int index = this.context.indexOf(name);
        if (index == -1) {
            this.getValidatedHeaders();
            throw new IllegalArgumentException("Header name '" + name + "' not found. Available columns are: " + Arrays.asList(this.selectedHeaders()));
        }
        return this.getMetaData(index);
    }

    private NormalizedString[] getValidatedHeaders() {
        NormalizedString[] headers = NormalizedString.toIdentifierGroupArray(this.context.headers());
        if (headers == null || headers.length == 0) {
            throw new IllegalStateException("No headers parsed from input nor provided in the user settings. Only index-based operations are available.");
        }
        return headers;
    }

    private MetaData getMetaData(Enum<?> column) {
        NormalizedString[] headers = NormalizedString.toIdentifierGroupArray(this.context.headers());
        if (headers == null || headers.length == 0) {
            throw new IllegalStateException("No headers parsed from input nor provided in the user settings. Only index-based operations are available.");
        }
        return this.getMetaData(this.context.indexOf(column));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public MetaData getMetaData(int index) {
        if (this.indexMap == null || this.indexMap.length < index + 1 || this.indexMap[index] == null) {
            RecordMetaDataImpl recordMetaDataImpl = this;
            synchronized (recordMetaDataImpl) {
                if (this.indexMap == null || this.indexMap.length < index + 1 || this.indexMap[index] == null) {
                    int startFrom = 0;
                    int lastIndex = index;
                    if (this.indexMap != null) {
                        startFrom = this.indexMap.length;
                        this.indexMap = Arrays.copyOf(this.indexMap, index + 1);
                    } else {
                        int[] indexes;
                        String[] headers = this.context.headers();
                        if (headers != null && lastIndex < headers.length) {
                            lastIndex = headers.length;
                        }
                        if ((indexes = this.context.extractedFieldIndexes()) != null) {
                            for (int i = 0; i < indexes.length; ++i) {
                                if (lastIndex >= indexes[i]) continue;
                                lastIndex = indexes[i];
                            }
                        }
                        this.indexMap = new MetaData[lastIndex + 1];
                    }
                    for (int i = startFrom; i < lastIndex + 1; ++i) {
                        this.indexMap[i] = new MetaData(i);
                    }
                }
            }
        }
        return this.indexMap[index];
    }

    @Override
    public int indexOf(Enum<?> column) {
        return this.getMetaData(column).index;
    }

    MetaData metadataOf(String headerName) {
        return this.getMetaData(headerName);
    }

    MetaData metadataOf(Enum<?> column) {
        return this.getMetaData(column);
    }

    MetaData metadataOf(int columnIndex) {
        return this.getMetaData(columnIndex);
    }

    @Override
    public int indexOf(String headerName) {
        return this.getMetaData((String)headerName).index;
    }

    @Override
    public Class<?> typeOf(Enum<?> column) {
        return this.getMetaData(column).type;
    }

    @Override
    public Class<?> typeOf(String headerName) {
        return this.getMetaData((String)headerName).type;
    }

    @Override
    public Class<?> typeOf(int columnIndex) {
        return this.getMetaData((int)columnIndex).type;
    }

    @Override
    public <T> void setDefaultValueOfColumns(T defaultValue, Enum<?> ... columns) {
        for (Enum<?> column : columns) {
            this.getMetaData(column).defaultValue = defaultValue;
        }
    }

    @Override
    public <T> void setDefaultValueOfColumns(T defaultValue, String ... headerNames) {
        for (String headerName : headerNames) {
            this.getMetaData((String)headerName).defaultValue = defaultValue;
        }
    }

    @Override
    public <T> void setDefaultValueOfColumns(T defaultValue, int ... columnIndexes) {
        for (int columnIndex : columnIndexes) {
            this.getMetaData((int)columnIndex).defaultValue = defaultValue;
        }
    }

    @Override
    public Object defaultValueOf(Enum<?> column) {
        return this.getMetaData(column).defaultValue;
    }

    @Override
    public Object defaultValueOf(String headerName) {
        return this.getMetaData((String)headerName).defaultValue;
    }

    @Override
    public Object defaultValueOf(int columnIndex) {
        return this.getMetaData((int)columnIndex).defaultValue;
    }

    private FieldConversionMapping getConversions() {
        if (this.conversions == null) {
            this.conversions = new FieldConversionMapping();
        }
        return this.conversions;
    }

    @Override
    public <T extends Enum<T>> FieldSet<T> convertFields(Class<T> enumType, Conversion ... conversions) {
        return this.getConversions().applyConversionsOnFieldEnums(conversions);
    }

    @Override
    public FieldSet<String> convertFields(Conversion ... conversions) {
        return this.getConversions().applyConversionsOnFieldNames(conversions);
    }

    @Override
    public FieldSet<Integer> convertIndexes(Conversion ... conversions) {
        return this.getConversions().applyConversionsOnFieldIndexes(conversions);
    }

    @Override
    public String[] headers() {
        return this.context.headers();
    }

    @Override
    public String[] selectedHeaders() {
        return this.context.selectedHeaders();
    }

    String getValue(String[] data, String headerName) {
        MetaData md = this.metadataOf(headerName);
        if (md.index >= data.length) {
            return null;
        }
        return data[md.index];
    }

    String getValue(String[] data, int columnIndex) {
        MetaData md = this.metadataOf(columnIndex);
        return data[md.index];
    }

    String getValue(String[] data, Enum<?> column) {
        MetaData md = this.metadataOf(column);
        return data[md.index];
    }

    private <T> T convert(MetaData md, String[] data, Class<T> expectedType, Conversion[] conversions) {
        return expectedType.cast(RecordMetaDataImpl.convert(md, data, conversions));
    }

    private Object convert(MetaData md, String[] data, Object defaultValue, Conversion[] conversions) {
        Object out = RecordMetaDataImpl.convert(md, data, conversions);
        return out == null ? defaultValue : out;
    }

    private static Object convert(MetaData md, String[] data, Conversion[] conversions) {
        String out = data[md.index];
        for (int i = 0; i < conversions.length; ++i) {
            out = conversions[i].execute(out);
        }
        return out;
    }

    <T> T getValue(String[] data, String headerName, T defaultValue, Conversion[] conversions) {
        return (T)this.convert(this.metadataOf(headerName), data, defaultValue, conversions);
    }

    <T> T getValue(String[] data, int columnIndex, T defaultValue, Conversion[] conversions) {
        return (T)this.convert(this.metadataOf(columnIndex), data, defaultValue, conversions);
    }

    <T> T getValue(String[] data, Enum<?> column, T defaultValue, Conversion[] conversions) {
        return (T)this.convert(this.metadataOf(column), data, defaultValue, conversions);
    }

    <T> T getValue(String[] data, String headerName, Class<T> expectedType, Conversion[] conversions) {
        return this.convert(this.metadataOf(headerName), data, expectedType, conversions);
    }

    <T> T getValue(String[] data, int columnIndex, Class<T> expectedType, Conversion[] conversions) {
        return this.convert(this.metadataOf(columnIndex), data, expectedType, conversions);
    }

    <T> T getValue(String[] data, Enum<?> column, Class<T> expectedType, Conversion[] conversions) {
        return this.convert(this.metadataOf(column), data, expectedType, conversions);
    }

    private <T> T convert(MetaData md, String[] data, Class<T> type, T defaultValue, Annotation annotation) {
        Object out;
        Object object = out = md.index < data.length ? data[md.index] : null;
        if (out == null) {
            Object object2 = out = defaultValue == null ? md.defaultValue : defaultValue;
        }
        if (annotation == null) {
            this.initializeMetadataConversions(data, md);
            out = md.convert(out);
            if (out == null) {
                Object object3 = out = defaultValue == null ? md.defaultValue : defaultValue;
            }
        }
        if (type != null) {
            Conversion conversion;
            if (out != null && type.isAssignableFrom(out.getClass())) {
                return (T)out;
            }
            if (annotation == null) {
                conversion = this.conversionByType.get(type);
                if (conversion == null) {
                    conversion = AnnotationHelper.getDefaultConversion(type, null, null);
                    this.conversionByType.put(type, conversion);
                }
            } else {
                Map<Annotation, Conversion> m3 = this.conversionsByAnnotation.get(type);
                if (m3 == null) {
                    m3 = new HashMap<Annotation, Conversion>();
                    this.conversionsByAnnotation.put(type, m3);
                }
                if ((conversion = m3.get(annotation)) == null) {
                    conversion = AnnotationHelper.getConversion(type, annotation);
                    m3.put(annotation, conversion);
                }
            }
            if (conversion == null) {
                if (type == String.class) {
                    if (out == null) {
                        return null;
                    }
                    return (T)(md.index < data.length ? data[md.index] : null);
                }
                String message = "";
                if (type == Date.class || type == Calendar.class) {
                    message = ". Need to specify format for date";
                }
                DataProcessingException exception = new DataProcessingException("Cannot convert '{value}' to " + type.getName() + message);
                exception.setValue(out);
                exception.setErrorContentLength(this.context.errorContentLength());
                throw exception;
            }
            out = conversion.execute(out);
        }
        if (type == null) {
            return (T)out;
        }
        try {
            return type.cast(out);
        }
        catch (ClassCastException e) {
            DataProcessingException exception = new DataProcessingException("Cannot cast value '{value}' of type " + out.getClass().toString() + " to " + type.getName());
            exception.setValue(out);
            exception.setErrorContentLength(this.context.errorContentLength());
            throw exception;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void initializeMetadataConversions(String[] data, MetaData md) {
        if (this.conversions != null) {
            RecordMetaDataImpl recordMetaDataImpl = this;
            synchronized (recordMetaDataImpl) {
                String[] headers = this.headers();
                if (headers == null) {
                    headers = data;
                }
                this.conversions.prepareExecution(false, headers);
                md.setDefaultConversions(this.conversions.getConversions(md.index, md.type));
            }
        }
    }

    <T> T getObjectValue(String[] data, String headerName, Class<T> type, T defaultValue) {
        return this.convert(this.metadataOf(headerName), data, type, defaultValue, null);
    }

    <T> T getObjectValue(String[] data, int columnIndex, Class<T> type, T defaultValue) {
        return this.convert(this.metadataOf(columnIndex), data, type, defaultValue, null);
    }

    <T> T getObjectValue(String[] data, Enum<?> column, Class<T> type, T defaultValue) {
        return this.convert(this.metadataOf(column), data, type, defaultValue, null);
    }

    <T> T getObjectValue(String[] data, String headerName, Class<T> type, T defaultValue, String format, String ... formatOptions) {
        if (format == null) {
            return this.getObjectValue(data, headerName, type, defaultValue);
        }
        return this.convert(this.metadataOf(headerName), data, type, defaultValue, this.buildAnnotation(type, format, formatOptions));
    }

    <T> T getObjectValue(String[] data, int columnIndex, Class<T> type, T defaultValue, String format, String ... formatOptions) {
        if (format == null) {
            return this.getObjectValue(data, columnIndex, type, defaultValue);
        }
        return this.convert(this.metadataOf(columnIndex), data, type, defaultValue, this.buildAnnotation(type, format, formatOptions));
    }

    <T> T getObjectValue(String[] data, Enum<?> column, Class<T> type, T defaultValue, String format, String ... formatOptions) {
        if (format == null) {
            return this.getObjectValue(data, column, type, defaultValue);
        }
        return this.convert(this.metadataOf(column), data, type, defaultValue, this.buildAnnotation(type, format, formatOptions));
    }

    static Annotation buildBooleanStringAnnotation(final String[] trueStrings, final String[] falseStrings) {
        return new BooleanString(){

            @Override
            public String[] trueStrings() {
                return trueStrings == null ? ArgumentUtils.EMPTY_STRING_ARRAY : trueStrings;
            }

            @Override
            public String[] falseStrings() {
                return falseStrings == null ? ArgumentUtils.EMPTY_STRING_ARRAY : falseStrings;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return BooleanString.class;
            }
        };
    }

    private static Annotation newFormatAnnotation(final String format, final String ... formatOptions) {
        return new Format(){

            @Override
            public String[] formats() {
                return new String[]{format};
            }

            @Override
            public String[] options() {
                return formatOptions;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Format.class;
            }
        };
    }

    <T> Annotation buildAnnotation(Class<T> type, String args1, String ... args2) {
        Integer hash = type.hashCode() * 31 + String.valueOf(args1).hashCode() + 31 * Arrays.toString(args2).hashCode();
        Annotation out = this.annotationHashes.get(hash);
        if (out == null) {
            if (type == Boolean.class || type == Boolean.TYPE) {
                String[] stringArray;
                if (args1 == null) {
                    stringArray = null;
                } else {
                    String[] stringArray2 = new String[1];
                    stringArray = stringArray2;
                    stringArray2[0] = args1;
                }
                out = RecordMetaDataImpl.buildBooleanStringAnnotation(stringArray, args2);
            } else {
                out = RecordMetaDataImpl.newFormatAnnotation(args1, args2);
            }
            this.annotationHashes.put(hash, out);
        }
        return out;
    }

    @Override
    public void setTypeOfColumns(Class<?> type, Enum ... columns) {
        for (int i = 0; i < columns.length; ++i) {
            this.getMetaData(columns[i]).type = type;
        }
    }

    @Override
    public void setTypeOfColumns(Class<?> type, String ... headerNames) {
        for (int i = 0; i < headerNames.length; ++i) {
            this.getMetaData((String)headerNames[i]).type = type;
        }
    }

    @Override
    public void setTypeOfColumns(Class<?> type, int ... columnIndexes) {
        for (int i = 0; i < columnIndexes.length; ++i) {
            this.getMetaData((int)columnIndexes[i]).type = type;
        }
    }

    @Override
    public boolean containsColumn(String headerName) {
        if (headerName == null) {
            return false;
        }
        return this.context.indexOf(headerName) != -1;
    }
}

