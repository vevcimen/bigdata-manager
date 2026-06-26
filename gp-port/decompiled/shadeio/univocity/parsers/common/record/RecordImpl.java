/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.record;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.record.Record;
import shadeio.univocity.parsers.common.record.RecordMetaData;
import shadeio.univocity.parsers.common.record.RecordMetaDataImpl;
import shadeio.univocity.parsers.conversions.Conversion;

class RecordImpl<C extends Context>
implements Record {
    private final String[] data;
    private final RecordMetaDataImpl<C> metaData;

    RecordImpl(String[] data, RecordMetaDataImpl metaData) {
        this.data = data;
        this.metaData = metaData;
    }

    @Override
    public RecordMetaData getMetaData() {
        return this.metaData;
    }

    @Override
    public String[] getValues() {
        return this.data;
    }

    @Override
    public <T> T getValue(String headerName, Class<T> expectedType) {
        return this.metaData.getObjectValue(this.data, headerName, expectedType, null);
    }

    @Override
    public <T> T getValue(Enum<?> column, Class<T> expectedType) {
        return this.metaData.getObjectValue(this.data, column, expectedType, null);
    }

    @Override
    public <T> T getValue(int columnIndex, Class<T> expectedType) {
        return this.metaData.getObjectValue(this.data, columnIndex, expectedType, null);
    }

    @Override
    public <T> T getValue(String headerName, Class<T> expectedType, Conversion ... conversions) {
        return this.metaData.getValue(this.data, headerName, expectedType, conversions);
    }

    @Override
    public <T> T getValue(Enum<?> column, Class<T> expectedType, Conversion ... conversions) {
        return this.metaData.getValue(this.data, column, expectedType, conversions);
    }

    @Override
    public <T> T getValue(int columnIndex, Class<T> expectedType, Conversion ... conversions) {
        return this.metaData.getValue(this.data, columnIndex, expectedType, conversions);
    }

    @Override
    public <T> T getValue(String headerName, T defaultValue) {
        return (T)this.metaData.getObjectValue(this.data, headerName, defaultValue.getClass(), defaultValue);
    }

    @Override
    public <T> T getValue(Enum<?> column, T defaultValue) {
        return (T)this.metaData.getObjectValue(this.data, column, defaultValue.getClass(), defaultValue);
    }

    @Override
    public <T> T getValue(int columnIndex, T defaultValue) {
        return (T)this.metaData.getObjectValue(this.data, columnIndex, defaultValue.getClass(), defaultValue);
    }

    @Override
    public <T> T getValue(String headerName, T defaultValue, Conversion ... conversions) {
        return this.metaData.getValue(this.data, headerName, defaultValue, conversions);
    }

    @Override
    public <T> T getValue(Enum<?> column, T defaultValue, Conversion ... conversions) {
        return this.metaData.getValue(this.data, column, defaultValue, conversions);
    }

    @Override
    public <T> T getValue(int columnIndex, T defaultValue, Conversion ... conversions) {
        return this.metaData.getValue(this.data, columnIndex, defaultValue, conversions);
    }

    @Override
    public String getString(String headerName) {
        return this.metaData.getObjectValue(this.data, headerName, String.class, null);
    }

    @Override
    public String getString(Enum<?> column) {
        return this.metaData.getObjectValue(this.data, column, String.class, null);
    }

    @Override
    public String getString(int columnIndex) {
        return this.metaData.getObjectValue(this.data, columnIndex, String.class, null);
    }

    @Override
    public String getString(String headerName, int maxLength) {
        return this.truncate(this.metaData.getValue(this.data, headerName), maxLength);
    }

    @Override
    public String getString(Enum<?> column, int maxLength) {
        return this.truncate(this.metaData.getValue(this.data, column), maxLength);
    }

    @Override
    public String getString(int columnIndex, int maxLength) {
        return this.truncate(this.metaData.getValue(this.data, columnIndex), maxLength);
    }

    private String truncate(String string, int maxLength) {
        if (string == null) {
            return null;
        }
        if (maxLength < 0) {
            throw new IllegalArgumentException("Maximum length can't be negative");
        }
        if (string.length() > maxLength) {
            return string.substring(0, maxLength);
        }
        return string;
    }

    @Override
    public Byte getByte(String headerName, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, headerName, Byte.class, null, format, formatOptions);
    }

    @Override
    public Byte getByte(Enum<?> column, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, column, Byte.class, null, format, formatOptions);
    }

    @Override
    public Byte getByte(int columnIndex, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, columnIndex, Byte.class, null, format, formatOptions);
    }

    @Override
    public Short getShort(String headerName, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, headerName, Short.class, null, format, formatOptions);
    }

    @Override
    public Short getShort(Enum<?> column, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, column, Short.class, null, format, formatOptions);
    }

    @Override
    public Short getShort(int columnIndex, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, columnIndex, Short.class, null, format, formatOptions);
    }

    @Override
    public Integer getInt(String headerName, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, headerName, Integer.class, null, format, formatOptions);
    }

    @Override
    public Integer getInt(Enum<?> column, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, column, Integer.class, null, format, formatOptions);
    }

    @Override
    public Integer getInt(int columnIndex, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, columnIndex, Integer.class, null, format, formatOptions);
    }

    @Override
    public Long getLong(String headerName, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, headerName, Long.class, null, format, formatOptions);
    }

    @Override
    public Long getLong(Enum<?> column, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, column, Long.class, null, format, formatOptions);
    }

    @Override
    public Long getLong(int columnIndex, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, columnIndex, Long.class, null, format, formatOptions);
    }

    @Override
    public Float getFloat(String headerName, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, headerName, Float.class, null, format, formatOptions);
    }

    @Override
    public Float getFloat(Enum<?> column, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, column, Float.class, null, format, formatOptions);
    }

    @Override
    public Float getFloat(int columnIndex, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, columnIndex, Float.class, null, format, formatOptions);
    }

    @Override
    public Double getDouble(String headerName, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, headerName, Double.class, null, format, formatOptions);
    }

    @Override
    public Double getDouble(Enum<?> column, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, column, Double.class, null, format, formatOptions);
    }

    @Override
    public Double getDouble(int columnIndex, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, columnIndex, Double.class, null, format, formatOptions);
    }

    @Override
    public Character getChar(String headerName) {
        return this.metaData.getObjectValue(this.data, headerName, Character.class, null);
    }

    @Override
    public Character getChar(Enum<?> column) {
        return this.metaData.getObjectValue(this.data, column, Character.class, null);
    }

    @Override
    public Character getChar(int columnIndex) {
        return this.metaData.getObjectValue(this.data, columnIndex, Character.class, null);
    }

    @Override
    public Boolean getBoolean(String headerName) {
        return this.metaData.getObjectValue(this.data, headerName, Boolean.class, null);
    }

    @Override
    public Boolean getBoolean(Enum<?> column) {
        return this.metaData.getObjectValue(this.data, column, Boolean.class, null);
    }

    @Override
    public Boolean getBoolean(int columnIndex) {
        return this.metaData.getObjectValue(this.data, columnIndex, Boolean.class, null);
    }

    @Override
    public Boolean getBoolean(String headerName, String trueString, String falseString) {
        return this.metaData.getObjectValue(this.data, headerName, Boolean.class, Boolean.valueOf(false), trueString, falseString);
    }

    @Override
    public Boolean getBoolean(Enum<?> column, String trueString, String falseString) {
        return this.metaData.getObjectValue(this.data, column, Boolean.class, Boolean.valueOf(false), trueString, falseString);
    }

    @Override
    public Boolean getBoolean(int columnIndex, String trueString, String falseString) {
        return this.metaData.getObjectValue(this.data, columnIndex, Boolean.class, Boolean.valueOf(false), trueString, falseString);
    }

    @Override
    public BigInteger getBigInteger(String headerName, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, headerName, BigInteger.class, null, format, formatOptions);
    }

    @Override
    public BigInteger getBigInteger(Enum<?> column, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, column, BigInteger.class, null, format, formatOptions);
    }

    @Override
    public BigInteger getBigInteger(int columnIndex, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, columnIndex, BigInteger.class, null, format, formatOptions);
    }

    @Override
    public BigDecimal getBigDecimal(String headerName, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, headerName, BigDecimal.class, null, format, formatOptions);
    }

    @Override
    public BigDecimal getBigDecimal(Enum<?> column, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, column, BigDecimal.class, null, format, formatOptions);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, columnIndex, BigDecimal.class, null, format, formatOptions);
    }

    @Override
    public Date getDate(String headerName, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, headerName, Date.class, null, format, formatOptions);
    }

    @Override
    public Date getDate(Enum<?> column, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, column, Date.class, null, format, formatOptions);
    }

    @Override
    public Date getDate(int columnIndex, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, columnIndex, Date.class, null, format, formatOptions);
    }

    @Override
    public Calendar getCalendar(String headerName, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, headerName, Calendar.class, null, format, formatOptions);
    }

    @Override
    public Calendar getCalendar(Enum<?> column, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, column, Calendar.class, null, format, formatOptions);
    }

    @Override
    public Calendar getCalendar(int columnIndex, String format, String ... formatOptions) {
        return this.metaData.getObjectValue(this.data, columnIndex, Calendar.class, null, format, formatOptions);
    }

    private String[] buildSelection(String[] selectedFields) {
        if (selectedFields.length == 0) {
            selectedFields = this.metaData.headers();
        }
        return selectedFields;
    }

    private int[] buildSelection(int[] selectedIndexes) {
        if (selectedIndexes.length == 0) {
            selectedIndexes = new int[this.data.length];
            for (int i = 0; i < this.data.length; ++i) {
                selectedIndexes[i] = i;
            }
        }
        return selectedIndexes;
    }

    public <T extends Enum<T>> T[] buildSelection(Class<T> enumType, T ... selectedColumns) {
        if (selectedColumns.length == 0) {
            selectedColumns = (Enum[])enumType.getEnumConstants();
        }
        return selectedColumns;
    }

    @Override
    public Map<Integer, String> toIndexMap(int ... selectedIndexes) {
        return this.fillIndexMap(new HashMap<Integer, String>(selectedIndexes.length), selectedIndexes);
    }

    @Override
    public Map<String, String> toFieldMap(String ... selectedFields) {
        return this.fillFieldMap(new HashMap<String, String>(selectedFields.length), selectedFields);
    }

    @Override
    public <T extends Enum<T>> Map<T, String> toEnumMap(Class<T> enumType, T ... selectedColumns) {
        return this.fillEnumMap(new EnumMap(enumType), (Enum[])selectedColumns);
    }

    @Override
    public Map<String, String> fillFieldMap(Map<String, String> map, String ... selectedFields) {
        selectedFields = this.buildSelection(selectedFields);
        for (int i = 0; i < selectedFields.length; ++i) {
            map.put(selectedFields[i], this.getString(selectedFields[i]));
        }
        return map;
    }

    @Override
    public Map<Integer, String> fillIndexMap(Map<Integer, String> map, int ... selectedIndexes) {
        selectedIndexes = this.buildSelection(selectedIndexes);
        for (int i = 0; i < selectedIndexes.length; ++i) {
            map.put(selectedIndexes[i], this.getString(selectedIndexes[i]));
        }
        return map;
    }

    @Override
    public <T extends Enum<T>> Map<T, String> fillEnumMap(Map<T, String> map, T ... selectedColumns) {
        for (int i = 0; i < selectedColumns.length; ++i) {
            map.put(selectedColumns[i], this.getString((Enum<?>)selectedColumns[i]));
        }
        return map;
    }

    @Override
    public Map<String, Object> toFieldObjectMap(String ... selectedFields) {
        return this.fillFieldObjectMap(new HashMap<String, Object>(selectedFields.length), selectedFields);
    }

    @Override
    public Map<Integer, Object> toIndexObjectMap(int ... selectedIndex) {
        return this.fillIndexObjectMap(new HashMap<Integer, Object>(selectedIndex.length), selectedIndex);
    }

    @Override
    public <T extends Enum<T>> Map<T, Object> toEnumObjectMap(Class<T> enumType, T ... selectedColumns) {
        return this.fillEnumObjectMap(new EnumMap(enumType), (Enum[])selectedColumns);
    }

    @Override
    public Map<String, Object> fillFieldObjectMap(Map<String, Object> map, String ... selectedFields) {
        selectedFields = this.buildSelection(selectedFields);
        for (int i = 0; i < selectedFields.length; ++i) {
            map.put(selectedFields[i], this.metaData.getObjectValue(this.data, selectedFields[i], null, null));
        }
        return map;
    }

    @Override
    public Map<Integer, Object> fillIndexObjectMap(Map<Integer, Object> map, int ... selectedIndexes) {
        selectedIndexes = this.buildSelection(selectedIndexes);
        for (int i = 0; i < selectedIndexes.length; ++i) {
            map.put(selectedIndexes[i], this.metaData.getObjectValue(this.data, selectedIndexes[i], null, null));
        }
        return map;
    }

    @Override
    public <T extends Enum<T>> Map<T, Object> fillEnumObjectMap(Map<T, Object> map, T ... selectedColumns) {
        selectedColumns = this.buildSelection(selectedColumns.getClass().getComponentType(), (Enum[])selectedColumns);
        for (int i = 0; i < selectedColumns.length; ++i) {
            map.put(selectedColumns[i], this.metaData.getObjectValue(this.data, (Enum<?>)selectedColumns[i], null, null));
        }
        return map;
    }

    @Override
    public BigInteger getBigInteger(String headerName) {
        return this.metaData.getObjectValue(this.data, headerName, BigInteger.class, null);
    }

    @Override
    public BigInteger getBigInteger(Enum<?> column) {
        return this.metaData.getObjectValue(this.data, column, BigInteger.class, null);
    }

    @Override
    public BigInteger getBigInteger(int columnIndex) {
        return this.metaData.getObjectValue(this.data, columnIndex, BigInteger.class, null);
    }

    @Override
    public BigDecimal getBigDecimal(String headerName) {
        return this.metaData.getObjectValue(this.data, headerName, BigDecimal.class, null);
    }

    @Override
    public BigDecimal getBigDecimal(Enum<?> column) {
        return this.metaData.getObjectValue(this.data, column, BigDecimal.class, null);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) {
        return this.metaData.getObjectValue(this.data, columnIndex, BigDecimal.class, null);
    }

    @Override
    public Byte getByte(String headerName) {
        return this.metaData.getObjectValue(this.data, headerName, Byte.class, null);
    }

    @Override
    public Byte getByte(Enum<?> column) {
        return this.metaData.getObjectValue(this.data, column, Byte.class, null);
    }

    @Override
    public Byte getByte(int columnIndex) {
        return this.metaData.getObjectValue(this.data, columnIndex, Byte.class, null);
    }

    @Override
    public Short getShort(String headerName) {
        return this.metaData.getObjectValue(this.data, headerName, Short.class, null);
    }

    @Override
    public Short getShort(Enum<?> column) {
        return this.metaData.getObjectValue(this.data, column, Short.class, null);
    }

    @Override
    public Short getShort(int columnIndex) {
        return this.metaData.getObjectValue(this.data, columnIndex, Short.class, null);
    }

    @Override
    public Integer getInt(String headerName) {
        return this.metaData.getObjectValue(this.data, headerName, Integer.class, null);
    }

    @Override
    public Integer getInt(Enum<?> column) {
        return this.metaData.getObjectValue(this.data, column, Integer.class, null);
    }

    @Override
    public Integer getInt(int columnIndex) {
        return this.metaData.getObjectValue(this.data, columnIndex, Integer.class, null);
    }

    @Override
    public Long getLong(String headerName) {
        return this.metaData.getObjectValue(this.data, headerName, Long.class, null);
    }

    @Override
    public Long getLong(Enum<?> column) {
        return this.metaData.getObjectValue(this.data, column, Long.class, null);
    }

    @Override
    public Long getLong(int columnIndex) {
        return this.metaData.getObjectValue(this.data, columnIndex, Long.class, null);
    }

    @Override
    public Float getFloat(String headerName) {
        return this.metaData.getObjectValue(this.data, headerName, Float.class, null);
    }

    @Override
    public Float getFloat(Enum<?> column) {
        return this.metaData.getObjectValue(this.data, column, Float.class, null);
    }

    @Override
    public Float getFloat(int columnIndex) {
        return this.metaData.getObjectValue(this.data, columnIndex, Float.class, null);
    }

    @Override
    public Double getDouble(String headerName) {
        return this.metaData.getObjectValue(this.data, headerName, Double.class, null);
    }

    @Override
    public Double getDouble(Enum<?> column) {
        return this.metaData.getObjectValue(this.data, column, Double.class, null);
    }

    @Override
    public Double getDouble(int columnIndex) {
        return this.metaData.getObjectValue(this.data, columnIndex, Double.class, null);
    }

    @Override
    public Date getDate(String headerName) {
        return this.metaData.getObjectValue(this.data, headerName, Date.class, null);
    }

    @Override
    public Date getDate(Enum<?> column) {
        return this.metaData.getObjectValue(this.data, column, Date.class, null);
    }

    @Override
    public Date getDate(int columnIndex) {
        return this.metaData.getObjectValue(this.data, columnIndex, Date.class, null);
    }

    @Override
    public Calendar getCalendar(String headerName) {
        return this.metaData.getObjectValue(this.data, headerName, Calendar.class, null);
    }

    @Override
    public Calendar getCalendar(Enum<?> column) {
        return this.metaData.getObjectValue(this.data, column, Calendar.class, null);
    }

    @Override
    public Calendar getCalendar(int columnIndex) {
        return this.metaData.getObjectValue(this.data, columnIndex, Calendar.class, null);
    }

    public String toString() {
        if (this.data == null) {
            return "null";
        }
        if (this.data.length == 0) {
            return "[]";
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < this.data.length; ++i) {
            if (out.length() != 0) {
                out.append(',').append(' ');
            }
            out.append(this.data[i]);
        }
        return out.toString();
    }

    public boolean equals(Object o) {
        return o == this;
    }

    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public String[] getValues(String ... fieldNames) {
        String[] out = new String[fieldNames.length];
        for (int i = 0; i < out.length; ++i) {
            out[i] = this.getString(fieldNames[i]);
        }
        return out;
    }

    @Override
    public String[] getValues(int ... fieldIndexes) {
        String[] out = new String[fieldIndexes.length];
        for (int i = 0; i < out.length; ++i) {
            out[i] = this.getString(fieldIndexes[i]);
        }
        return out;
    }

    @Override
    public String[] getValues(Enum<?> ... fields) {
        String[] out = new String[fields.length];
        for (int i = 0; i < out.length; ++i) {
            out[i] = this.getString(fields[i]);
        }
        return out;
    }
}

