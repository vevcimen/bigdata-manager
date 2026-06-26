/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import shadeio.univocity.parsers.annotations.helpers.MethodFilter;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.processor.core.BeanConversionProcessor;
import shadeio.univocity.parsers.common.processor.core.Processor;

public abstract class AbstractBeanProcessor<T, C extends Context>
extends BeanConversionProcessor<T>
implements Processor<C> {
    public AbstractBeanProcessor(Class<T> beanType, MethodFilter methodFilter) {
        super(beanType, methodFilter);
    }

    @Override
    public final void rowProcessed(String[] row, C context) {
        Object instance = this.createBean(row, (Context)context);
        if (instance != null) {
            this.beanProcessed(instance, context);
        }
    }

    public abstract void beanProcessed(T var1, C var2);

    @Override
    public void processStarted(C context) {
        super.initialize(NormalizedString.toArray(context.headers()));
    }

    @Override
    public void processEnded(C context) {
    }
}

