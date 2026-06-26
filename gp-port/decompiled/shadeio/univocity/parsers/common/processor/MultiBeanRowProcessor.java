/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractMultiBeanRowProcessor;

public abstract class MultiBeanRowProcessor
extends AbstractMultiBeanRowProcessor<ParsingContext>
implements RowProcessor {
    public MultiBeanRowProcessor(Class ... beanTypes) {
        super(beanTypes);
    }
}

