/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import shadeio.univocity.parsers.common.Context;

public interface Processor<T extends Context> {
    public void processStarted(T var1);

    public void rowProcessed(String[] var1, T var2);

    public void processEnded(T var1);
}

