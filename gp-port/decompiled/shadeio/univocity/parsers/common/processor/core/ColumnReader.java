/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import java.util.List;
import java.util.Map;

interface ColumnReader<T> {
    public String[] getHeaders();

    public List<List<T>> getColumnValuesAsList();

    public void putColumnValuesInMapOfNames(Map<String, List<T>> var1);

    public void putColumnValuesInMapOfIndexes(Map<Integer, List<T>> var1);

    public Map<String, List<T>> getColumnValuesAsMapOfNames();

    public Map<Integer, List<T>> getColumnValuesAsMapOfIndexes();

    public List<T> getColumn(String var1);

    public List<T> getColumn(int var1);
}

