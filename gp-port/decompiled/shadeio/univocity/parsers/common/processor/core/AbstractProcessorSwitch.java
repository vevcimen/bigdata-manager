/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import java.util.HashMap;
import java.util.Map;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.ColumnOrderDependent;
import shadeio.univocity.parsers.common.processor.core.NoopProcessor;
import shadeio.univocity.parsers.common.processor.core.Processor;

public abstract class AbstractProcessorSwitch<T extends Context>
implements Processor<T>,
ColumnOrderDependent {
    private Map<Processor, T> processors;
    private Processor selectedProcessor;
    private T contextForProcessor;

    protected abstract Processor<T> switchRowProcessor(String[] var1, T var2);

    public String[] getHeaders() {
        return null;
    }

    public int[] getIndexes() {
        return null;
    }

    public void processorSwitched(Processor<T> from, Processor<T> to) {
        if (from != null) {
            if (from instanceof RowProcessor && (to == null || to instanceof RowProcessor)) {
                this.rowProcessorSwitched((RowProcessor)from, (RowProcessor)to);
            }
        } else if (to != null && to instanceof RowProcessor) {
            this.rowProcessorSwitched((RowProcessor)from, (RowProcessor)to);
        }
    }

    public void rowProcessorSwitched(RowProcessor from, RowProcessor to) {
    }

    @Override
    public void processStarted(T context) {
        this.processors = new HashMap<Processor, T>();
        this.selectedProcessor = NoopProcessor.instance;
    }

    protected abstract T wrapContext(T var1);

    @Override
    public final void rowProcessed(String[] row, T context) {
        Processor processor = this.switchRowProcessor(row, context);
        if (processor == null) {
            processor = NoopProcessor.instance;
        }
        if (processor != this.selectedProcessor) {
            this.contextForProcessor = (Context)this.processors.get(processor);
            if (processor != NoopProcessor.instance) {
                if (this.contextForProcessor == null) {
                    this.contextForProcessor = this.wrapContext(context);
                    processor.processStarted(this.contextForProcessor);
                    this.processors.put(processor, this.contextForProcessor);
                }
                this.processorSwitched(this.selectedProcessor, processor);
                this.selectedProcessor = processor;
                if (this.getIndexes() != null) {
                    int[] indexes = this.getIndexes();
                    String[] tmp = new String[indexes.length];
                    for (int i = 0; i < indexes.length; ++i) {
                        int index = indexes[i];
                        if (index >= row.length) continue;
                        tmp[i] = row[index];
                    }
                    row = tmp;
                }
                this.selectedProcessor.rowProcessed(row, this.contextForProcessor);
            }
        } else {
            this.selectedProcessor.rowProcessed(row, this.contextForProcessor);
        }
    }

    @Override
    public void processEnded(T context) {
        this.processorSwitched(this.selectedProcessor, null);
        this.selectedProcessor = NoopProcessor.instance;
        for (Map.Entry<Processor, T> e : this.processors.entrySet()) {
            e.getKey().processEnded((Context)e.getValue());
        }
    }

    @Override
    public boolean preventColumnReordering() {
        return true;
    }
}

