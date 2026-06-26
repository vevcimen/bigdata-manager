/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.replication;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.replication.LogSequenceNumber;

public interface PGReplicationStream
extends AutoCloseable {
    public @Nullable ByteBuffer read() throws SQLException;

    public @Nullable ByteBuffer readPending() throws SQLException;

    public LogSequenceNumber getLastReceiveLSN();

    public LogSequenceNumber getLastFlushedLSN();

    public LogSequenceNumber getLastAppliedLSN();

    public void setFlushedLSN(LogSequenceNumber var1);

    public void setAppliedLSN(LogSequenceNumber var1);

    public void forceUpdateStatus() throws SQLException;

    public boolean isClosed();

    @Override
    public void close() throws SQLException;
}

