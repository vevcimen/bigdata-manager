/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.common.ProcessorErrorHandler;

public final class NoopProcessorErrorHandler<T extends Context>
implements ProcessorErrorHandler<T> {
    public static final ProcessorErrorHandler instance = new NoopProcessorErrorHandler();

    private NoopProcessorErrorHandler() {
    }

    @Override
    public void handleError(DataProcessingException error, Object[] inputRow, T context) {
        throw error;
    }
}

