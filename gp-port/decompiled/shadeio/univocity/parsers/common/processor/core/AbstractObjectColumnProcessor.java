/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import java.util.List;
import java.util.Map;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.processor.core.AbstractObjectProcessor;
import shadeio.univocity.parsers.common.processor.core.ColumnReader;
import shadeio.univocity.parsers.common.processor.core.ColumnSplitter;

public abstract class AbstractObjectColumnProcessor<T extends Context>
extends AbstractObjectProcessor<T>
implements ColumnReader<Object> {
    private final ColumnSplitter<Object> splitter;

    public AbstractObjectColumnProcessor() {
        this(1000);
    }

    public AbstractObjectColumnProcessor(int expectedRowCount) {
        this.splitter = new ColumnSplitter(expectedRowCount);
    }

    @Override
    public final String[] getHeaders() {
        return this.splitter.getHeaders();
    }

    @Override
    public final List<List<Object>> getColumnValuesAsList() {
        return this.splitter.getColumnValues();
    }

    @Override
    public final void putColumnValuesInMapOfNames(Map<String, List<Object>> map) {
        this.splitter.putColumnValuesInMapOfNames(map);
    }

    @Override
    public final void putColumnValuesInMapOfIndexes(Map<Integer, List<Object>> map) {
        this.splitter.putColumnValuesInMapOfIndexes(map);
    }

    @Override
    public final Map<String, List<Object>> getColumnValuesAsMapOfNames() {
        return this.splitter.getColumnValuesAsMapOfNames();
    }

    @Override
    public final Map<Integer, List<Object>> getColumnValuesAsMapOfIndexes() {
        return this.splitter.getColumnValuesAsMapOfIndexes();
    }

    @Override
    public void rowProcessed(Object[] row, T context) {
        this.splitter.addValuesToColumns((T[])row, (Context)context);
    }

    @Override
    public void processStarted(T context) {
        super.processStarted(context);
        this.splitter.reset();
    }

    public <V> List<V> getColumn(String columnName, Class<V> columnType) {
        return this.splitter.getColumnValues(columnName, columnType);
    }

    public <V> List<V> getColumn(int columnIndex, Class<V> columnType) {
        return this.splitter.getColumnValues(columnIndex, columnType);
    }

    @Override
    public List<Object> getColumn(String columnName) {
        return this.splitter.getColumnValues(columnName, Object.class);
    }

    @Override
    public List<Object> getColumn(int columnIndex) {
        return this.splitter.getColumnValues(columnIndex, Object.class);
    }
}

