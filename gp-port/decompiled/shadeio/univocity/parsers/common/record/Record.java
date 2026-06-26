/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.record;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import shadeio.univocity.parsers.common.record.RecordMetaData;
import shadeio.univocity.parsers.conversions.Conversion;

public interface Record {
    public RecordMetaData getMetaData();

    public String[] getValues();

    public String[] getValues(String ... var1);

    public String[] getValues(int ... var1);

    public String[] getValues(Enum<?> ... var1);

    public <T> T getValue(String var1, Class<T> var2);

    public <T> T getValue(Enum<?> var1, Class<T> var2);

    public <T> T getValue(int var1, Class<T> var2);

    public <T> T getValue(String var1, Class<T> var2, Conversion ... var3);

    public <T> T getValue(Enum<?> var1, Class<T> var2, Conversion ... var3);

    public <T> T getValue(int var1, Class<T> var2, Conversion ... var3);

    public <T> T getValue(String var1, T var2);

    public <T> T getValue(Enum<?> var1, T var2);

    public <T> T getValue(int var1, T var2);

    public <T> T getValue(String var1, T var2, Conversion ... var3);

    public <T> T getValue(Enum<?> var1, T var2, Conversion ... var3);

    public <T> T getValue(int var1, T var2, Conversion ... var3);

    public String getString(String var1);

    public String getString(Enum<?> var1);

    public String getString(int var1, int var2);

    public String getString(String var1, int var2);

    public String getString(Enum<?> var1, int var2);

    public String getString(int var1);

    public Byte getByte(String var1, String var2, String ... var3);

    public Byte getByte(Enum<?> var1, String var2, String ... var3);

    public Byte getByte(int var1, String var2, String ... var3);

    public Short getShort(String var1, String var2, String ... var3);

    public Short getShort(Enum<?> var1, String var2, String ... var3);

    public Short getShort(int var1, String var2, String ... var3);

    public Integer getInt(String var1, String var2, String ... var3);

    public Integer getInt(Enum<?> var1, String var2, String ... var3);

    public Integer getInt(int var1, String var2, String ... var3);

    public Long getLong(String var1, String var2, String ... var3);

    public Long getLong(Enum<?> var1, String var2, String ... var3);

    public Long getLong(int var1, String var2, String ... var3);

    public Float getFloat(String var1, String var2, String ... var3);

    public Float getFloat(Enum<?> var1, String var2, String ... var3);

    public Float getFloat(int var1, String var2, String ... var3);

    public Double getDouble(String var1, String var2, String ... var3);

    public Double getDouble(Enum<?> var1, String var2, String ... var3);

    public Double getDouble(int var1, String var2, String ... var3);

    public Byte getByte(String var1);

    public Byte getByte(Enum<?> var1);

    public Byte getByte(int var1);

    public Short getShort(String var1);

    public Short getShort(Enum<?> var1);

    public Short getShort(int var1);

    public Integer getInt(String var1);

    public Integer getInt(Enum<?> var1);

    public Integer getInt(int var1);

    public Long getLong(String var1);

    public Long getLong(Enum<?> var1);

    public Long getLong(int var1);

    public Float getFloat(String var1);

    public Float getFloat(Enum<?> var1);

    public Float getFloat(int var1);

    public Double getDouble(String var1);

    public Double getDouble(Enum<?> var1);

    public Double getDouble(int var1);

    public Character getChar(String var1);

    public Character getChar(Enum<?> var1);

    public Character getChar(int var1);

    public Boolean getBoolean(String var1);

    public Boolean getBoolean(Enum<?> var1);

    public Boolean getBoolean(int var1);

    public Boolean getBoolean(String var1, String var2, String var3);

    public Boolean getBoolean(Enum<?> var1, String var2, String var3);

    public Boolean getBoolean(int var1, String var2, String var3);

    public BigInteger getBigInteger(String var1, String var2, String ... var3);

    public BigInteger getBigInteger(Enum<?> var1, String var2, String ... var3);

    public BigInteger getBigInteger(int var1, String var2, String ... var3);

    public BigDecimal getBigDecimal(String var1, String var2, String ... var3);

    public BigDecimal getBigDecimal(Enum<?> var1, String var2, String ... var3);

    public BigDecimal getBigDecimal(int var1, String var2, String ... var3);

    public BigInteger getBigInteger(String var1);

    public BigInteger getBigInteger(Enum<?> var1);

    public BigInteger getBigInteger(int var1);

    public BigDecimal getBigDecimal(String var1);

    public BigDecimal getBigDecimal(Enum<?> var1);

    public BigDecimal getBigDecimal(int var1);

    public Date getDate(String var1, String var2, String ... var3);

    public Date getDate(Enum<?> var1, String var2, String ... var3);

    public Date getDate(int var1, String var2, String ... var3);

    public Calendar getCalendar(String var1, String var2, String ... var3);

    public Calendar getCalendar(Enum<?> var1, String var2, String ... var3);

    public Calendar getCalendar(int var1, String var2, String ... var3);

    public Date getDate(String var1);

    public Date getDate(Enum<?> var1);

    public Date getDate(int var1);

    public Calendar getCalendar(String var1);

    public Calendar getCalendar(Enum<?> var1);

    public Calendar getCalendar(int var1);

    public Map<String, String> toFieldMap(String ... var1);

    public Map<Integer, String> toIndexMap(int ... var1);

    public <T extends Enum<T>> Map<T, String> toEnumMap(Class<T> var1, T ... var2);

    public Map<String, String> fillFieldMap(Map<String, String> var1, String ... var2);

    public Map<Integer, String> fillIndexMap(Map<Integer, String> var1, int ... var2);

    public <T extends Enum<T>> Map<T, String> fillEnumMap(Map<T, String> var1, T ... var2);

    public Map<String, Object> toFieldObjectMap(String ... var1);

    public Map<Integer, Object> toIndexObjectMap(int ... var1);

    public <T extends Enum<T>> Map<T, Object> toEnumObjectMap(Class<T> var1, T ... var2);

    public Map<String, Object> fillFieldObjectMap(Map<String, Object> var1, String ... var2);

    public Map<Integer, Object> fillIndexObjectMap(Map<Integer, Object> var1, int ... var2);

    public <T extends Enum<T>> Map<T, Object> fillEnumObjectMap(Map<T, Object> var1, T ... var2);
}

