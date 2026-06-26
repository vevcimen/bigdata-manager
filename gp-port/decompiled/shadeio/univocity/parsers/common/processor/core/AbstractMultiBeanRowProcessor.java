/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import java.util.HashMap;
import java.util.Map;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.processor.core.AbstractMultiBeanProcessor;

public abstract class AbstractMultiBeanRowProcessor<C extends Context>
extends AbstractMultiBeanProcessor<C> {
    private final HashMap<Class<?>, Object> row = new HashMap();
    private long record = -1L;

    public AbstractMultiBeanRowProcessor(Class ... beanTypes) {
        super(beanTypes);
    }

    @Override
    public void processStarted(C context) {
        this.record = -1L;
        this.row.clear();
        super.processStarted(context);
    }

    @Override
    public final void beanProcessed(Class<?> beanType, Object beanInstance, C context) {
        if (this.record != context.currentRecord() && this.record != -1L) {
            this.submitRow(context);
        }
        this.record = context.currentRecord();
        this.row.put(beanType, beanInstance);
    }

    private void submitRow(C context) {
        if (!this.row.isEmpty()) {
            this.rowProcessed(this.row, context);
            this.row.clear();
        }
    }

    @Override
    public void processEnded(C context) {
        this.submitRow(context);
        super.processEnded(context);
    }

    protected abstract void rowProcessed(Map<Class<?>, Object> var1, C var2);
}

