/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.processor.core.AbstractMultiBeanRowProcessor;

public class AbstractMultiBeanListProcessor<C extends Context>
extends AbstractMultiBeanRowProcessor<C> {
    private final Class[] beanTypes;
    private final List[] beans;
    private String[] headers;
    private final int expectedBeanCount;

    public AbstractMultiBeanListProcessor(int expectedBeanCount, Class ... beanTypes) {
        super(beanTypes);
        this.beanTypes = beanTypes;
        this.beans = new List[beanTypes.length];
        this.expectedBeanCount = expectedBeanCount <= 0 ? 10000 : expectedBeanCount;
    }

    public AbstractMultiBeanListProcessor(Class ... beanTypes) {
        this(0, beanTypes);
    }

    @Override
    public final void processStarted(C context) {
        super.processStarted(context);
        for (int i = 0; i < this.beanTypes.length; ++i) {
            this.beans[i] = new ArrayList(this.expectedBeanCount);
        }
    }

    @Override
    protected final void rowProcessed(Map<Class<?>, Object> row, C context) {
        for (int i = 0; i < this.beanTypes.length; ++i) {
            Object bean = row.get(this.beanTypes[i]);
            this.beans[i].add(bean);
        }
    }

    @Override
    public final void processEnded(C context) {
        this.headers = context.headers();
        super.processEnded(context);
    }

    public final String[] getHeaders() {
        return this.headers;
    }

    public <T> List<T> getBeans(Class<T> beanType) {
        int index = ArgumentUtils.indexOf(this.beanTypes, beanType);
        if (index == -1) {
            throw new IllegalArgumentException("Unknown bean type '" + beanType.getSimpleName() + "'. Available types are: " + Arrays.toString(this.beanTypes));
        }
        return this.beans[index];
    }

    public Map<Class<?>, List<?>> getBeans() {
        LinkedHashMap out = new LinkedHashMap();
        for (int i = 0; i < this.beanTypes.length; ++i) {
            out.put(this.beanTypes[i], this.beans[i]);
        }
        return out;
    }
}

