/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import java.util.Arrays;
import shadeio.univocity.parsers.common.AbstractException;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.common.ProcessorErrorHandler;
import shadeio.univocity.parsers.common.RetryableErrorHandler;
import shadeio.univocity.parsers.common.processor.core.Processor;

class Internal {
    Internal() {
    }

    public static final <C extends Context> void process(String[] row, Processor<C> processor, C context, ProcessorErrorHandler<C> errorHandler) {
        try {
            processor.rowProcessed(row, context);
        }
        catch (DataProcessingException ex) {
            ex.setContext(context);
            if (!ex.isFatal() && !ex.isHandled() && ex.getColumnIndex() > -1 && errorHandler instanceof RetryableErrorHandler) {
                RetryableErrorHandler retry = (RetryableErrorHandler)errorHandler;
                ex.markAsHandled(errorHandler);
                retry.handleError(ex, row, context);
                if (!retry.isRecordSkipped()) {
                    try {
                        processor.rowProcessed(row, context);
                        return;
                    }
                    catch (DataProcessingException e) {
                        ex = e;
                    }
                    catch (Throwable t) {
                        Internal.throwDataProcessingException(processor, t, row, context.errorContentLength());
                    }
                }
            }
            ex.setErrorContentLength(context.errorContentLength());
            if (ex.isFatal()) {
                throw ex;
            }
            ex.markAsHandled(errorHandler);
            errorHandler.handleError(ex, row, context);
        }
        catch (Throwable t) {
            Internal.throwDataProcessingException(processor, t, row, context.errorContentLength());
        }
    }

    private static final void throwDataProcessingException(Processor processor, Throwable t, String[] row, int errorContentLength) throws DataProcessingException {
        DataProcessingException ex = new DataProcessingException("Unexpected error processing input row " + AbstractException.restrictContent(errorContentLength, Arrays.toString(row)) + " using Processor " + processor.getClass().getName() + '.', AbstractException.restrictContent(errorContentLength, row), t);
        ex.restrictContent(errorContentLength);
        throw ex;
    }
}

