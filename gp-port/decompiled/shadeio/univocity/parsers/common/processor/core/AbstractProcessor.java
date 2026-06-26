/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.processor.core.Processor;

public abstract class AbstractProcessor<T extends Context>
implements Processor<T> {
    @Override
    public void processStarted(T context) {
    }

    @Override
    public void rowProcessed(String[] row, T context) {
    }

    @Override
    public void processEnded(T context) {
    }
}

