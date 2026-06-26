/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.DefaultConversionProcessor;
import shadeio.univocity.parsers.common.processor.core.Processor;

public abstract class AbstractObjectProcessor<T extends Context>
extends DefaultConversionProcessor
implements Processor<T> {
    @Override
    public void rowProcessed(String[] row, T context) {
        Object[] objectRow = this.applyConversions(row, (Context)context);
        if (objectRow != null) {
            this.rowProcessed(objectRow, context);
        }
    }

    public abstract void rowProcessed(Object[] var1, T var2);

    @Override
    public void processStarted(T context) {
    }

    @Override
    public void processEnded(T context) {
    }
}

