/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import java.util.List;
import java.util.Map;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.processor.core.ColumnReader;
import shadeio.univocity.parsers.common.processor.core.ColumnSplitter;
import shadeio.univocity.parsers.common.processor.core.Processor;

public abstract class AbstractColumnProcessor<T extends Context>
implements Processor<T>,
ColumnReader<String> {
    private final ColumnSplitter<String> splitter;

    public AbstractColumnProcessor() {
        this(1000);
    }

    public AbstractColumnProcessor(int expectedRowCount) {
        this.splitter = new ColumnSplitter(expectedRowCount);
    }

    @Override
    public void processStarted(T context) {
        this.splitter.reset();
    }

    @Override
    public void rowProcessed(String[] row, T context) {
        this.splitter.addValuesToColumns((T[])row, (Context)context);
    }

    @Override
    public void processEnded(T context) {
    }

    @Override
    public final String[] getHeaders() {
        return this.splitter.getHeaders();
    }

    @Override
    public final List<List<String>> getColumnValuesAsList() {
        return this.splitter.getColumnValues();
    }

    @Override
    public final void putColumnValuesInMapOfNames(Map<String, List<String>> map) {
        this.splitter.putColumnValuesInMapOfNames(map);
    }

    @Override
    public final void putColumnValuesInMapOfIndexes(Map<Integer, List<String>> map) {
        this.splitter.putColumnValuesInMapOfIndexes(map);
    }

    @Override
    public final Map<String, List<String>> getColumnValuesAsMapOfNames() {
        return this.splitter.getColumnValuesAsMapOfNames();
    }

    @Override
    public final Map<Integer, List<String>> getColumnValuesAsMapOfIndexes() {
        return this.splitter.getColumnValuesAsMapOfIndexes();
    }

    @Override
    public List<String> getColumn(String columnName) {
        return this.splitter.getColumnValues(columnName, String.class);
    }

    @Override
    public List<String> getColumn(int columnIndex) {
        return this.splitter.getColumnValues(columnIndex, String.class);
    }
}

