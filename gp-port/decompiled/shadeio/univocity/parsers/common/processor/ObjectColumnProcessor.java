/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractObjectColumnProcessor;

public class ObjectColumnProcessor
extends AbstractObjectColumnProcessor<ParsingContext>
implements RowProcessor {
    public ObjectColumnProcessor() {
        this(1000);
    }

    public ObjectColumnProcessor(int expectedRowCount) {
        super(expectedRowCount);
    }
}

