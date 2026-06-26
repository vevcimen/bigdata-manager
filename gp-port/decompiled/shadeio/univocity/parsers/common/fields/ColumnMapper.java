/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.fields;

import java.util.Map;

public interface ColumnMapper
extends Cloneable {
    public void attributeToColumnName(String var1, String var2);

    public void attributeToColumn(String var1, Enum<?> var2);

    public void attributeToIndex(String var1, int var2);

    public void attributesToColumnNames(Map<String, String> var1);

    public void attributesToColumns(Map<String, Enum<?>> var1);

    public void attributesToIndexes(Map<String, Integer> var1);

    public void methodToColumnName(String var1, Class<?> var2, String var3);

    public void methodToColumn(String var1, Class<?> var2, Enum<?> var3);

    public void methodToIndex(String var1, Class<?> var2, int var3);

    public void methodToColumnName(String var1, String var2);

    public void methodToColumn(String var1, Enum<?> var2);

    public void methodToIndex(String var1, int var2);

    public void methodsToColumnNames(Map<String, String> var1);

    public void methodsToColumns(Map<String, Enum<?>> var1);

    public void methodsToIndexes(Map<String, Integer> var1);

    public ColumnMapper clone();

    public void remove(String var1);
}

