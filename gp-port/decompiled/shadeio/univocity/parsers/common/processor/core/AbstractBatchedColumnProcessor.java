/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import java.util.List;
import java.util.Map;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.processor.core.BatchedColumnReader;
import shadeio.univocity.parsers.common.processor.core.ColumnSplitter;
import shadeio.univocity.parsers.common.processor.core.Processor;

public abstract class AbstractBatchedColumnProcessor<T extends Context>
implements Processor<T>,
BatchedColumnReader<String> {
    private final ColumnSplitter<String> splitter;
    private final int rowsPerBatch;
    private int batchCount;
    private int batchesProcessed;

    public AbstractBatchedColumnProcessor(int rowsPerBatch) {
        this.splitter = new ColumnSplitter(rowsPerBatch);
        this.rowsPerBatch = rowsPerBatch;
    }

    @Override
    public void processStarted(T context) {
        this.splitter.reset();
        this.batchCount = 0;
        this.batchesProcessed = 0;
    }

    @Override
    public void rowProcessed(String[] row, T context) {
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
        if (this.batchCount > 0) {
            this.batchProcessed(this.batchCount);
        }
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

