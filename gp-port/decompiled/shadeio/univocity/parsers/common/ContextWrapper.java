/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.record.Record;
import shadeio.univocity.parsers.common.record.RecordMetaData;

public abstract class ContextWrapper<T extends Context>
implements Context {
    protected final T context;

    public ContextWrapper(T context) {
        this.context = context;
    }

    @Override
    public String[] headers() {
        return this.context.headers();
    }

    @Override
    public int[] extractedFieldIndexes() {
        return this.context.extractedFieldIndexes();
    }

    @Override
    public boolean columnsReordered() {
        return this.context.columnsReordered();
    }

    @Override
    public int indexOf(String header) {
        return this.context.indexOf(header);
    }

    @Override
    public int indexOf(Enum<?> header) {
        return this.context.indexOf(header);
    }

    @Override
    public int currentColumn() {
        return this.context.currentColumn();
    }

    @Override
    public long currentRecord() {
        return this.context.currentRecord();
    }

    @Override
    public void stop() {
        this.context.stop();
    }

    @Override
    public boolean isStopped() {
        return this.context.isStopped();
    }

    @Override
    public String[] selectedHeaders() {
        return this.context.selectedHeaders();
    }

    @Override
    public int errorContentLength() {
        return this.context.errorContentLength();
    }

    @Override
    public Record toRecord(String[] row) {
        return this.context.toRecord(row);
    }

    @Override
    public RecordMetaData recordMetaData() {
        return this.context.recordMetaData();
    }
}

