/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.CompositeProcessor;
import shadeio.univocity.parsers.common.processor.core.Processor;

public class CompositeRowProcessor
extends CompositeProcessor<ParsingContext>
implements RowProcessor {
    public CompositeRowProcessor(Processor ... processors) {
        super(processors);
    }
}

