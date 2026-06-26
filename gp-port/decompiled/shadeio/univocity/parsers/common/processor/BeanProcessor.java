/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.annotations.helpers.MethodFilter;
import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractBeanProcessor;

public abstract class BeanProcessor<T>
extends AbstractBeanProcessor<T, ParsingContext>
implements RowProcessor {
    public BeanProcessor(Class<T> beanType) {
        super(beanType, MethodFilter.ONLY_SETTERS);
    }
}

