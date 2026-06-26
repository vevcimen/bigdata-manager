/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.RowProcessorErrorHandler;

final class NoopRowProcessorErrorHandler
implements RowProcessorErrorHandler {
    public static final RowProcessorErrorHandler instance = new NoopRowProcessorErrorHandler();

    private NoopRowProcessorErrorHandler() {
    }

    @Override
    public void handleError(DataProcessingException error, Object[] inputRow, ParsingContext context) {
        throw error;
    }
}

