/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractListProcessor;

public class RowListProcessor
extends AbstractListProcessor<ParsingContext>
implements RowProcessor {
    public RowListProcessor() {
    }

    public RowListProcessor(int expectedRowCount) {
        super(expectedRowCount);
    }
}

