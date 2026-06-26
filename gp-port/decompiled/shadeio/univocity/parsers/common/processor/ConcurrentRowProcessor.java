/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.ParsingContextSnapshot;
import shadeio.univocity.parsers.common.ParsingContextWrapper;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractConcurrentProcessor;

public class ConcurrentRowProcessor
extends AbstractConcurrentProcessor<ParsingContext>
implements RowProcessor {
    public ConcurrentRowProcessor(RowProcessor rowProcessor) {
        super(rowProcessor);
    }

    public ConcurrentRowProcessor(RowProcessor rowProcessor, int limit) {
        super(rowProcessor, limit);
    }

    @Override
    protected ParsingContext copyContext(ParsingContext context) {
        return new ParsingContextSnapshot(context);
    }

    @Override
    protected ParsingContext wrapContext(ParsingContext context) {
        return new ParsingContextWrapper(context){

            @Override
            public long currentRecord() {
                return ConcurrentRowProcessor.this.getRowCount();
            }
        };
    }
}

