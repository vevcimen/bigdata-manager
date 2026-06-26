/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.processor.core.Processor;

public class CompositeProcessor<C extends Context>
implements Processor<C> {
    private final Processor[] processors;

    public CompositeProcessor(Processor ... processors) {
        this.processors = processors;
    }

    @Override
    public void processStarted(C context) {
        for (int i = 0; i < this.processors.length; ++i) {
            this.processors[i].processStarted(context);
        }
    }

    @Override
    public void rowProcessed(String[] row, C context) {
        for (int i = 0; i < this.processors.length; ++i) {
            this.processors[i].rowProcessed(row, context);
        }
    }

    @Override
    public void processEnded(C context) {
        for (int i = 0; i < this.processors.length; ++i) {
            this.processors[i].processEnded(context);
        }
    }
}

