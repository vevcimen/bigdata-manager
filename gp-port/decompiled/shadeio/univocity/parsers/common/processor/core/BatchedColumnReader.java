/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import shadeio.univocity.parsers.common.processor.core.ColumnReader;

interface BatchedColumnReader<T>
extends ColumnReader<T> {
    public int getRowsPerBatch();

    public int getBatchesProcessed();

    public void batchProcessed(int var1);
}

