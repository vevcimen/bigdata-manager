/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractBatchedObjectColumnProcessor;

public abstract class BatchedObjectColumnProcessor
extends AbstractBatchedObjectColumnProcessor<ParsingContext>
implements RowProcessor {
    public BatchedObjectColumnProcessor(int rowsPerBatch) {
        super(rowsPerBatch);
    }
}

