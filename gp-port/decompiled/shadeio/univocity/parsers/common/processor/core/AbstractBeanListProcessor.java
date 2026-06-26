/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import shadeio.univocity.parsers.annotations.helpers.MethodFilter;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.processor.core.AbstractBeanProcessor;

public abstract class AbstractBeanListProcessor<T, C extends Context>
extends AbstractBeanProcessor<T, C> {
    private List<T> beans;
    private String[] headers;
    private final int expectedBeanCount;

    public AbstractBeanListProcessor(Class<T> beanType) {
        this(beanType, 0);
    }

    public AbstractBeanListProcessor(Class<T> beanType, int expectedBeanCount) {
        super(beanType, MethodFilter.ONLY_SETTERS);
        this.expectedBeanCount = expectedBeanCount <= 0 ? 10000 : expectedBeanCount;
    }

    @Override
    public void beanProcessed(T bean, C context) {
        this.beans.add(bean);
    }

    public List<T> getBeans() {
        return this.beans == null ? Collections.emptyList() : this.beans;
    }

    @Override
    public void processStarted(C context) {
        super.processStarted(context);
        this.beans = new ArrayList<T>(this.expectedBeanCount);
    }

    @Override
    public void processEnded(C context) {
        this.headers = context.headers();
        super.processEnded(context);
    }

    public String[] getHeaders() {
        return this.headers;
    }
}

