/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3;

import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.copy.CopyOut;
import org.postgresql.core.v3.CopyOperationImpl;

public class CopyOutImpl
extends CopyOperationImpl
implements CopyOut {
    private byte @Nullable [] currentDataRow;

    @Override
    public byte @Nullable [] readFromCopy() throws SQLException {
        return this.readFromCopy(true);
    }

    @Override
    public byte @Nullable [] readFromCopy(boolean block) throws SQLException {
        this.currentDataRow = null;
        this.getQueryExecutor().readFromCopy(this, block);
        return this.currentDataRow;
    }

    @Override
    protected void handleCopydata(byte[] data) {
        this.currentDataRow = data;
    }
}

