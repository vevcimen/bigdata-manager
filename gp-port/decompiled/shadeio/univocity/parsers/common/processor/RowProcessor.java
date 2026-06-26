/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.processor.core.Processor;

public interface RowProcessor
extends Processor<ParsingContext> {
    @Override
    public void processStarted(ParsingContext var1);

    @Override
    public void rowProcessed(String[] var1, ParsingContext var2);

    @Override
    public void processEnded(ParsingContext var1);
}

