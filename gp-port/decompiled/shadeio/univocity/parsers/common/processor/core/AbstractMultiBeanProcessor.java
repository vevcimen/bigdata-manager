/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import shadeio.univocity.parsers.annotations.helpers.MethodFilter;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.ConversionProcessor;
import shadeio.univocity.parsers.common.fields.FieldSet;
import shadeio.univocity.parsers.common.processor.core.AbstractBeanProcessor;
import shadeio.univocity.parsers.common.processor.core.Processor;
import shadeio.univocity.parsers.conversions.Conversion;

public abstract class AbstractMultiBeanProcessor<C extends Context>
implements Processor<C>,
ConversionProcessor {
    private final AbstractBeanProcessor<?, C>[] beanProcessors;
    private final Map<Class, AbstractBeanProcessor> processorMap = new HashMap<Class, AbstractBeanProcessor>();

    public AbstractMultiBeanProcessor(Class ... beanTypes) {
        ArgumentUtils.noNulls("Bean types", beanTypes);
        this.beanProcessors = new AbstractBeanProcessor[beanTypes.length];
        for (int i = 0; i < beanTypes.length; ++i) {
            final Class type = beanTypes[i];
            this.beanProcessors[i] = new AbstractBeanProcessor<Object, C>(type, MethodFilter.ONLY_SETTERS){

                @Override
                public void beanProcessed(Object bean, C context) {
                    AbstractMultiBeanProcessor.this.beanProcessed(type, bean, context);
                }
            };
            this.processorMap.put(type, this.beanProcessors[i]);
        }
    }

    public final Class[] getBeanClasses() {
        Class[] classes = new Class[this.beanProcessors.length];
        for (int i = 0; i < this.beanProcessors.length; ++i) {
            classes[i] = this.beanProcessors[i].beanClass;
        }
        return classes;
    }

    public <T> AbstractBeanProcessor<T, C> getProcessorOfType(Class<T> type) {
        AbstractBeanProcessor processor = this.processorMap.get(type);
        if (processor == null) {
            throw new IllegalArgumentException("No processor of type '" + type.getName() + "' is available. Supported types are: " + this.processorMap.keySet());
        }
        return processor;
    }

    public abstract void beanProcessed(Class<?> var1, Object var2, C var3);

    @Override
    public void processStarted(C context) {
        for (int i = 0; i < this.beanProcessors.length; ++i) {
            this.beanProcessors[i].processStarted(context);
        }
    }

    @Override
    public final void rowProcessed(String[] row, C context) {
        for (int i = 0; i < this.beanProcessors.length; ++i) {
            this.beanProcessors[i].rowProcessed(row, context);
        }
    }

    @Override
    public void processEnded(C context) {
        for (int i = 0; i < this.beanProcessors.length; ++i) {
            this.beanProcessors[i].processEnded(context);
        }
    }

    @Override
    public FieldSet<Integer> convertIndexes(Conversion ... conversions) {
        ArrayList sets = new ArrayList(this.beanProcessors.length);
        for (int i = 0; i < this.beanProcessors.length; ++i) {
            sets.add(this.beanProcessors[i].convertIndexes(conversions));
        }
        return new FieldSet<Integer>(sets);
    }

    @Override
    public void convertAll(Conversion ... conversions) {
        for (int i = 0; i < this.beanProcessors.length; ++i) {
            this.beanProcessors[i].convertAll(conversions);
        }
    }

    @Override
    public FieldSet<String> convertFields(Conversion ... conversions) {
        ArrayList sets = new ArrayList(this.beanProcessors.length);
        for (int i = 0; i < this.beanProcessors.length; ++i) {
            sets.add(this.beanProcessors[i].convertFields(conversions));
        }
        return new FieldSet<String>(sets);
    }

    @Override
    public void convertType(Class<?> type, Conversion ... conversions) {
        for (int i = 0; i < this.beanProcessors.length; ++i) {
            this.beanProcessors[i].convertType(type, conversions);
        }
    }
}

