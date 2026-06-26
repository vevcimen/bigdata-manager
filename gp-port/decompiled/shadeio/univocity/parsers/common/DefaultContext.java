/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.ColumnMap;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.ParserOutput;
import shadeio.univocity.parsers.common.record.Record;
import shadeio.univocity.parsers.common.record.RecordFactory;
import shadeio.univocity.parsers.common.record.RecordMetaData;

public class DefaultContext
implements Context {
    protected boolean stopped = false;
    final ParserOutput output;
    final ColumnMap columnMap;
    final int errorContentLength;
    protected RecordFactory recordFactory;
    private String[] headers;

    public DefaultContext(int errorContentLength) {
        this(null, errorContentLength);
    }

    public DefaultContext(ParserOutput output, int errorContentLength) {
        this.output = output;
        this.errorContentLength = errorContentLength;
        this.columnMap = new ColumnMap(this, output);
    }

    @Override
    public String[] headers() {
        if (this.headers == null) {
            if (this.output == null) {
                this.headers = ArgumentUtils.EMPTY_STRING_ARRAY;
            }
            this.headers = this.output.getHeaderAsStringArray();
        }
        return this.headers;
    }

    @Override
    public String[] selectedHeaders() {
        int[] extractedFieldIndexes;
        if (this.headers == null) {
            this.headers();
        }
        if ((extractedFieldIndexes = this.extractedFieldIndexes()) != null) {
            String[] extractedFields = new String[extractedFieldIndexes.length];
            String[] headers = this.headers();
            for (int i = 0; i < extractedFieldIndexes.length; ++i) {
                extractedFields[i] = headers[extractedFieldIndexes[i]];
            }
            return extractedFields;
        }
        return this.headers();
    }

    @Override
    public int[] extractedFieldIndexes() {
        if (this.output == null) {
            return null;
        }
        return this.output.getSelectedIndexes();
    }

    @Override
    public boolean columnsReordered() {
        if (this.output == null) {
            return false;
        }
        return this.output.isColumnReorderingEnabled();
    }

    @Override
    public int indexOf(String header) {
        return this.columnMap.indexOf(header);
    }

    @Override
    public int indexOf(Enum<?> header) {
        return this.columnMap.indexOf(header);
    }

    void reset() {
        if (this.output != null) {
            this.output.reset();
        }
        this.recordFactory = null;
        this.columnMap.reset();
    }

    @Override
    public int currentColumn() {
        if (this.output == null) {
            return -1;
        }
        return this.output.getCurrentColumn();
    }

    @Override
    public long currentRecord() {
        if (this.output == null) {
            return -1L;
        }
        return this.output.getCurrentRecord();
    }

    @Override
    public void stop() {
        this.stopped = true;
    }

    @Override
    public boolean isStopped() {
        return this.stopped;
    }

    @Override
    public int errorContentLength() {
        return this.errorContentLength;
    }

    @Override
    public Record toRecord(String[] row) {
        if (this.recordFactory == null) {
            this.recordFactory = new RecordFactory(this);
        }
        return this.recordFactory.newRecord(row);
    }

    @Override
    public RecordMetaData recordMetaData() {
        if (this.recordFactory == null) {
            this.recordFactory = new RecordFactory(this);
        }
        return this.recordFactory.getRecordMetaData();
    }
}

