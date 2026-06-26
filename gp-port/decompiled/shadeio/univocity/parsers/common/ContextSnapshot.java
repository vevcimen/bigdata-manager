/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.ContextWrapper;

public abstract class ContextSnapshot<T extends Context>
extends ContextWrapper<T> {
    private final int currentColumn;
    private final long currentRecord;

    public ContextSnapshot(T context) {
        super(context);
        this.currentColumn = context.currentColumn();
        this.currentRecord = context.currentRecord();
    }

    @Override
    public int currentColumn() {
        return this.currentColumn;
    }

    @Override
    public long currentRecord() {
        return this.currentRecord;
    }
}

