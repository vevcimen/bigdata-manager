/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import java.util.ArrayList;
import java.util.List;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.processor.MasterDetailRecord;
import shadeio.univocity.parsers.common.processor.RowPlacement;
import shadeio.univocity.parsers.common.processor.core.AbstractObjectListProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractObjectProcessor;

public abstract class AbstractMasterDetailProcessor<T extends Context>
extends AbstractObjectProcessor<T> {
    private final AbstractObjectListProcessor detailProcessor;
    private MasterDetailRecord record;
    private final boolean isMasterRowAboveDetail;

    public AbstractMasterDetailProcessor(RowPlacement rowPlacement, AbstractObjectListProcessor detailProcessor) {
        ArgumentUtils.noNulls("Row processor for reading detail rows", detailProcessor);
        this.detailProcessor = detailProcessor;
        this.isMasterRowAboveDetail = rowPlacement == RowPlacement.TOP;
    }

    public AbstractMasterDetailProcessor(AbstractObjectListProcessor detailProcessor) {
        this(RowPlacement.TOP, detailProcessor);
    }

    @Override
    public void processStarted(T context) {
        this.detailProcessor.processStarted(context);
    }

    @Override
    public final void rowProcessed(String[] row, T context) {
        if (this.isMasterRecord(row, context)) {
            super.rowProcessed(row, context);
        } else {
            if (this.isMasterRowAboveDetail && this.record == null) {
                return;
            }
            this.detailProcessor.rowProcessed(row, context);
        }
    }

    @Override
    public final void rowProcessed(Object[] row, T context) {
        if (this.record == null) {
            this.record = new MasterDetailRecord();
            this.record.setMasterRow(row);
            if (this.isMasterRowAboveDetail) {
                return;
            }
        }
        this.processRecord(row, context);
    }

    private void processRecord(Object[] row, T context) {
        List<Object[]> detailRows = this.detailProcessor.getRows();
        this.record.setDetailRows(new ArrayList<Object[]>(detailRows));
        if (!this.isMasterRowAboveDetail) {
            this.record.setMasterRow(row);
        }
        if (this.record.getMasterRow() != null) {
            this.masterDetailRecordProcessed(this.record.clone(), context);
            this.record.clear();
        }
        detailRows.clear();
        if (this.isMasterRowAboveDetail) {
            this.record.setMasterRow(row);
        }
    }

    @Override
    public void processEnded(T context) {
        super.processEnded(context);
        this.detailProcessor.processEnded(context);
        if (this.isMasterRowAboveDetail) {
            this.processRecord(null, context);
        }
    }

    protected abstract boolean isMasterRecord(String[] var1, T var2);

    protected abstract void masterDetailRecordProcessed(MasterDetailRecord var1, T var2);
}

