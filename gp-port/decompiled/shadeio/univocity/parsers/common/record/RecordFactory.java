/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.record;

import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.record.AbstractRecordFactory;
import shadeio.univocity.parsers.common.record.Record;
import shadeio.univocity.parsers.common.record.RecordImpl;
import shadeio.univocity.parsers.common.record.RecordMetaDataImpl;

public class RecordFactory
extends AbstractRecordFactory<Record, RecordMetaDataImpl> {
    public RecordFactory(Context context) {
        super(context);
    }

    @Override
    public Record newRecord(String[] data) {
        return new RecordImpl(data, (RecordMetaDataImpl)this.metaData);
    }

    @Override
    public RecordMetaDataImpl createMetaData(Context context) {
        return new RecordMetaDataImpl<Context>(context);
    }
}

