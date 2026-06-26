/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.ProcessorErrorHandler;

public abstract class RetryableErrorHandler<T extends Context>
implements ProcessorErrorHandler<T> {
    private Object defaultValue;
    private boolean skipRecord = true;

    public final void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        this.keepRecord();
    }

    public final void keepRecord() {
        this.skipRecord = false;
    }

    public final Object getDefaultValue() {
        return this.defaultValue;
    }

    final void prepareToRun() {
        this.skipRecord = true;
        this.defaultValue = null;
    }

    public final boolean isRecordSkipped() {
        return this.skipRecord;
    }
}

