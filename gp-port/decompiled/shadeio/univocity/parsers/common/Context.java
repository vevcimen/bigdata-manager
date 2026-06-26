/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import shadeio.univocity.parsers.common.record.Record;
import shadeio.univocity.parsers.common.record.RecordMetaData;

public interface Context {
    public String[] headers();

    public String[] selectedHeaders();

    public int[] extractedFieldIndexes();

    public boolean columnsReordered();

    public int indexOf(String var1);

    public int indexOf(Enum<?> var1);

    public int currentColumn();

    public long currentRecord();

    public void stop();

    public boolean isStopped();

    public int errorContentLength();

    public Record toRecord(String[] var1);

    public RecordMetaData recordMetaData();
}

