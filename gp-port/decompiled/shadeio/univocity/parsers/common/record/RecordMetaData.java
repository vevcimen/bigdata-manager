/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.record;

import shadeio.univocity.parsers.common.fields.FieldSet;
import shadeio.univocity.parsers.conversions.Conversion;

public interface RecordMetaData {
    public int indexOf(Enum<?> var1);

    public int indexOf(String var1);

    public Class<?> typeOf(Enum<?> var1);

    public Class<?> typeOf(String var1);

    public Class<?> typeOf(int var1);

    public void setTypeOfColumns(Class<?> var1, Enum ... var2);

    public void setTypeOfColumns(Class<?> var1, String ... var2);

    public void setTypeOfColumns(Class<?> var1, int ... var2);

    public <T> void setDefaultValueOfColumns(T var1, Enum<?> ... var2);

    public <T> void setDefaultValueOfColumns(T var1, String ... var2);

    public <T> void setDefaultValueOfColumns(T var1, int ... var2);

    public Object defaultValueOf(Enum<?> var1);

    public Object defaultValueOf(String var1);

    public Object defaultValueOf(int var1);

    public <T extends Enum<T>> FieldSet<T> convertFields(Class<T> var1, Conversion ... var2);

    public FieldSet<String> convertFields(Conversion ... var1);

    public FieldSet<Integer> convertIndexes(Conversion ... var1);

    public String[] headers();

    public String[] selectedHeaders();

    public boolean containsColumn(String var1);
}

