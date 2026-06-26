/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import java.util.List;
import java.util.Map;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.processor.core.AbstractObjectProcessor;
import shadeio.univocity.parsers.common.processor.core.BatchedColumnReader;
import shadeio.univocity.parsers.common.processor.core.ColumnSplitter;
import shadeio.univocity.parsers.common.processor.core.Processor;

public abstract class AbstractBatchedObjectColumnProcessor<T extends Context>
extends AbstractObjectProcessor<T>
implements Processor<T>,
BatchedColumnReader<Object> {
    private final ColumnSplitter<Object> splitter;
    private final int rowsPerBatch;
    private int batchCount;
    private int batchesProcessed;

    public AbstractBatchedObjectColumnProcessor(int rowsPerBatch) {
        this.splitter = new ColumnSplitter(rowsPerBatch);
        this.rowsPerBatch = rowsPerBatch;
    }

    @Override
    public void processStarted(T context) {
        super.processStarted(context);
        this.splitter.reset();
        this.batchCount = 0;
        this.batchesProcessed = 0;
    }

    @Override
    public void rowProcessed(Object[] row, T context) {
        this.splitter.addValuesToColumns((T[])row, (Context)context);
        ++this.batchCount;
        if (this.batchCount >= this.rowsPerBatch) {
            this.batchProcessed(this.batchCount);
            this.batchCount = 0;
            this.splitter.clearValues();
            ++this.batchesProcessed;
        }
    }

    @Override
    public void processEnded(T context) {
        super.processEnded(context);
        if (this.batchCount > 0) {
            this.batchProcessed(this.batchCount);
        }
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
    public List<Object> getColumn(String columnName) {
        return this.splitter.getColumnValues(columnName, Object.class);
    }

    @Override
    public List<Object> getColumn(int columnIndex) {
        return this.splitter.getColumnValues(columnIndex, Object.class);
    }

    public <V> List<V> getColumn(String columnName, Class<V> columnType) {
        return this.splitter.getColumnValues(columnName, columnType);
    }

    public <V> List<V> getColumn(int columnIndex, Class<V> columnType) {
        return this.splitter.getColumnValues(columnIndex, columnType);
    }

    @Override
    public int getRowsPerBatch() {
        return this.rowsPerBatch;
    }

    @Override
    public int getBatchesProcessed() {
        return this.batchesProcessed;
    }

    @Override
    public abstract void batchProcessed(int var1);
}

