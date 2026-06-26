/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.DataProcessingException;

public interface ProcessorErrorHandler<T extends Context> {
    public void handleError(DataProcessingException var1, Object[] var2, T var3);
}

