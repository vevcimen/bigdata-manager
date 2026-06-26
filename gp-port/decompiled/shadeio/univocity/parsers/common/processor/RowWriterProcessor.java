/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.NormalizedString;

public interface RowWriterProcessor<T> {
    public Object[] write(T var1, NormalizedString[] var2, int[] var3);
}

