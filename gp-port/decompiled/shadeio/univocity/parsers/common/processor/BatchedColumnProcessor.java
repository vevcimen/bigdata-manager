/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractBatchedColumnProcessor;

public abstract class BatchedColumnProcessor
extends AbstractBatchedColumnProcessor<ParsingContext>
implements RowProcessor {
    public BatchedColumnProcessor(int rowsPerBatch) {
        super(rowsPerBatch);
    }
}

