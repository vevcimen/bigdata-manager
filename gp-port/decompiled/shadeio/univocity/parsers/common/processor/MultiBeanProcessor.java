/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractMultiBeanProcessor;

public abstract class MultiBeanProcessor
extends AbstractMultiBeanProcessor<ParsingContext>
implements RowProcessor {
    public MultiBeanProcessor(Class ... beanTypes) {
        super(beanTypes);
    }
}

