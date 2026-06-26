/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractMultiBeanListProcessor;

public class MultiBeanListProcessor
extends AbstractMultiBeanListProcessor<ParsingContext>
implements RowProcessor {
    public MultiBeanListProcessor(int expectedBeanCount, Class ... beanTypes) {
        super(expectedBeanCount, beanTypes);
    }

    public MultiBeanListProcessor(Class ... beanTypes) {
        super(beanTypes);
    }
}

