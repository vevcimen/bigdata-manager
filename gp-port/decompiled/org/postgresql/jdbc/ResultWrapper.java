/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.sql.ResultSet;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

public class ResultWrapper {
    private final @Nullable ResultSet rs;
    private final long updateCount;
    private final long insertOID;
    private @Nullable ResultWrapper next;

    public ResultWrapper(@Nullable ResultSet rs) {
        this.rs = rs;
        this.updateCount = -1L;
        this.insertOID = -1L;
    }

    public ResultWrapper(long updateCount, long insertOID) {
        this.rs = null;
        this.updateCount = updateCount;
        this.insertOID = insertOID;
    }

    @Pure
    public @Nullable ResultSet getResultSet() {
        return this.rs;
    }

    public long getUpdateCount() {
        return this.updateCount;
    }

    public long getInsertOID() {
        return this.insertOID;
    }

    public @Nullable ResultWrapper getNext() {
        return this.next;
    }

    public void append(ResultWrapper newResult) {
        ResultWrapper tail = this;
        while (tail.next != null) {
            tail = tail.next;
        }
        tail.next = newResult;
    }
}

