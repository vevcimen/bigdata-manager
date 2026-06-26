/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractObjectListProcessor;

public class ObjectRowListProcessor
extends AbstractObjectListProcessor<ParsingContext>
implements RowProcessor {
    public ObjectRowListProcessor() {
    }

    public ObjectRowListProcessor(int expectedRowCount) {
        super(expectedRowCount);
    }
}

