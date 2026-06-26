/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractBeanListProcessor;

public class BeanListProcessor<T>
extends AbstractBeanListProcessor<T, ParsingContext>
implements RowProcessor {
    public BeanListProcessor(Class<T> beanType) {
        super(beanType);
    }

    public BeanListProcessor(Class<T> beanType, int expectedBeanCount) {
        super(beanType, expectedBeanCount);
    }
}

