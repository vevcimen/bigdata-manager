/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.processor.RowPlacement;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractMasterDetailListProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractObjectListProcessor;

public abstract class MasterDetailListProcessor
extends AbstractMasterDetailListProcessor<ParsingContext>
implements RowProcessor {
    public MasterDetailListProcessor(RowPlacement rowPlacement, AbstractObjectListProcessor detailProcessor) {
        super(rowPlacement, detailProcessor);
    }

    public MasterDetailListProcessor(AbstractObjectListProcessor detailProcessor) {
        super(detailProcessor);
    }
}

