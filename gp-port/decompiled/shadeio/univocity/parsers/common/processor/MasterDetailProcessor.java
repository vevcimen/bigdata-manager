/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.processor.ObjectRowListProcessor;
import shadeio.univocity.parsers.common.processor.RowPlacement;
import shadeio.univocity.parsers.common.processor.core.AbstractMasterDetailProcessor;

public abstract class MasterDetailProcessor
extends AbstractMasterDetailProcessor<ParsingContext> {
    public MasterDetailProcessor(RowPlacement rowPlacement, ObjectRowListProcessor detailProcessor) {
        super(rowPlacement, detailProcessor);
    }

    public MasterDetailProcessor(ObjectRowListProcessor detailProcessor) {
        super(RowPlacement.TOP, detailProcessor);
    }
}

