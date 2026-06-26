/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import java.util.ArrayList;
import java.util.List;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.processor.MasterDetailRecord;
import shadeio.univocity.parsers.common.processor.RowPlacement;
import shadeio.univocity.parsers.common.processor.core.AbstractMasterDetailProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractObjectListProcessor;

public abstract class AbstractMasterDetailListProcessor<T extends Context>
extends AbstractMasterDetailProcessor<T> {
    private final List<MasterDetailRecord> records = new ArrayList<MasterDetailRecord>();
    private String[] headers;

    public AbstractMasterDetailListProcessor(RowPlacement rowPlacement, AbstractObjectListProcessor detailProcessor) {
        super(rowPlacement, detailProcessor);
    }

    public AbstractMasterDetailListProcessor(AbstractObjectListProcessor detailProcessor) {
        super(detailProcessor);
    }

    @Override
    protected void masterDetailRecordProcessed(MasterDetailRecord record, T context) {
        this.records.add(record);
    }

    @Override
    public void processEnded(T context) {
        this.headers = context.headers();
        super.processEnded(context);
    }

    public List<MasterDetailRecord> getRecords() {
        return this.records;
    }

    public String[] getHeaders() {
        return this.headers;
    }
}

