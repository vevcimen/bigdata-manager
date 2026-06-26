/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractColumnProcessor;

public class ColumnProcessor
extends AbstractColumnProcessor<ParsingContext>
implements RowProcessor {
    public ColumnProcessor() {
        super(1000);
    }

    public ColumnProcessor(int expectedRowCount) {
        super(expectedRowCount);
    }
}

