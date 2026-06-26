/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.record;

import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.record.Record;
import shadeio.univocity.parsers.common.record.RecordMetaData;

public abstract class AbstractRecordFactory<R extends Record, M extends RecordMetaData> {
    protected final M metaData;

    public AbstractRecordFactory(Context context) {
        this.metaData = this.createMetaData(context);
    }

    public abstract R newRecord(String[] var1);

    public abstract M createMetaData(Context var1);

    public final M getRecordMetaData() {
        return this.metaData;
    }
}

