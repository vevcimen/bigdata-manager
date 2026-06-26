/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3.replication;

import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.copy.CopyDual;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.ReplicationType;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class V3PGReplicationStream
implements PGReplicationStream {
    private static final Logger LOGGER = Logger.getLogger(V3PGReplicationStream.class.getName());
    public static final long POSTGRES_EPOCH_2000_01_01 = 946684800000L;
    private static final long NANOS_PER_MILLISECOND = 1000000L;
    private final CopyDual copyDual;
    private final long updateInterval;
    private final ReplicationType replicationType;
    private long lastStatusUpdate;
    private boolean closeFlag = false;
    private LogSequenceNumber lastServerLSN = LogSequenceNumber.INVALID_LSN;
    private volatile LogSequenceNumber lastReceiveLSN = LogSequenceNumber.INVALID_LSN;
    private volatile LogSequenceNumber lastAppliedLSN = LogSequenceNumber.INVALID_LSN;
    private volatile LogSequenceNumber lastFlushedLSN = LogSequenceNumber.INVALID_LSN;

    public V3PGReplicationStream(CopyDual copyDual, LogSequenceNumber startLSN, long updateIntervalMs, ReplicationType replicationType) {
        this.copyDual = copyDual;
        this.updateInterval = updateIntervalMs * 1000000L;
        this.lastStatusUpdate = System.nanoTime() - updateIntervalMs * 1000000L;
        this.lastReceiveLSN = startLSN;
        this.replicationType = replicationType;
    }

    @Override
    public @Nullable ByteBuffer read() throws SQLException {
        this.checkClose();
        ByteBuffer payload = null;
        while (payload == null && this.copyDual.isActive()) {
            payload = this.readInternal(true);
        }
        return payload;
    }

    @Override
    public @Nullable ByteBuffer readPending() throws SQLException {
        this.checkClose();
        return this.readInternal(false);
    }

    @Override
    public LogSequenceNumber getLastReceiveLSN() {
        return this.lastReceiveLSN;
    }

    @Override
    public LogSequenceNumber getLastFlushedLSN() {
        return this.lastFlushedLSN;
    }

    @Override
    public LogSequenceNumber getLastAppliedLSN() {
        return this.lastAppliedLSN;
    }

    @Override
    public void setFlushedLSN(LogSequenceNumber flushed) {
        this.lastFlushedLSN = flushed;
    }

    @Override
    public void setAppliedLSN(LogSequenceNumber applied) {
        this.lastAppliedLSN = applied;
    }

    @Override
    public void forceUpdateStatus() throws SQLException {
        this.checkClose();
        this.updateStatusInternal(this.lastReceiveLSN, this.lastFlushedLSN, this.lastAppliedLSN, true);
    }

    @Override
    public boolean isClosed() {
        return this.closeFlag || !this.copyDual.isActive();
    }

    private @Nullable ByteBuffer readInternal(boolean block) throws SQLException {
        boolean updateStatusRequired = false;
        block4: while (this.copyDual.isActive()) {
            ByteBuffer buffer = this.receiveNextData(block);
            if (updateStatusRequired || this.isTimeUpdate()) {
                this.timeUpdateStatus();
            }
            if (buffer == null) {
                return null;
            }
            byte code = buffer.get();
            switch (code) {
                case 107: {
                    updateStatusRequired = this.processKeepAliveMessage(buffer);
                    updateStatusRequired |= this.updateInterval == 0L;
                    continue block4;
                }
                case 119: {
                    return this.processXLogData(buffer);
                }
            }
            throw new PSQLException(GT.tr("Unexpected packet type during replication: {0}", Integer.toString(code)), PSQLState.PROTOCOL_VIOLATION);
        }
        return null;
    }

    private @Nullable ByteBuffer receiveNextData(boolean block) throws SQLException {
        try {
            byte[] message = this.copyDual.readFromCopy(block);
            if (message != null) {
                return ByteBuffer.wrap(message);
            }
            return null;
        }
        catch (PSQLException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                return null;
            }
            throw e;
        }
    }

    private boolean isTimeUpdate() {
        if (this.updateInterval == 0L) {
            return false;
        }
        long diff = System.nanoTime() - this.lastStatusUpdate;
        return diff >= this.updateInterval;
    }

    private void timeUpdateStatus() throws SQLException {
        this.updateStatusInternal(this.lastReceiveLSN, this.lastFlushedLSN, this.lastAppliedLSN, false);
    }

    private void updateStatusInternal(LogSequenceNumber received, LogSequenceNumber flushed, LogSequenceNumber applied, boolean replyRequired) throws SQLException {
        byte[] reply = this.prepareUpdateStatus(received, flushed, applied, replyRequired);
        this.copyDual.writeToCopy(reply, 0, reply.length);
        this.copyDual.flushCopy();
        this.lastStatusUpdate = System.nanoTime();
    }

    private byte[] prepareUpdateStatus(LogSequenceNumber received, LogSequenceNumber flushed, LogSequenceNumber applied, boolean replyRequired) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(34);
        long now = System.nanoTime() / 1000000L;
        long systemClock = TimeUnit.MICROSECONDS.convert(now - 946684800000L, TimeUnit.MICROSECONDS);
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, " FE=> StandbyStatusUpdate(received: {0}, flushed: {1}, applied: {2}, clock: {3})", new Object[]{received.asString(), flushed.asString(), applied.asString(), new Date(now)});
        }
        byteBuffer.put((byte)114);
        byteBuffer.putLong(received.asLong());
        byteBuffer.putLong(flushed.asLong());
        byteBuffer.putLong(applied.asLong());
        byteBuffer.putLong(systemClock);
        if (replyRequired) {
            byteBuffer.put((byte)1);
        } else {
            byteBuffer.put(received == LogSequenceNumber.INVALID_LSN ? (byte)1 : 0);
        }
        this.lastStatusUpdate = now;
        return byteBuffer.array();
    }

    private boolean processKeepAliveMessage(ByteBuffer buffer) {
        boolean replyRequired;
        this.lastServerLSN = LogSequenceNumber.valueOf(buffer.getLong());
        if (this.lastServerLSN.asLong() > this.lastReceiveLSN.asLong()) {
            this.lastReceiveLSN = this.lastServerLSN;
        }
        long lastServerClock = buffer.getLong();
        boolean bl = replyRequired = buffer.get() != 0;
        if (LOGGER.isLoggable(Level.FINEST)) {
            Date clockTime = new Date(TimeUnit.MILLISECONDS.convert(lastServerClock, TimeUnit.MICROSECONDS) + 946684800000L);
            LOGGER.log(Level.FINEST, "  <=BE Keepalive(lastServerWal: {0}, clock: {1} needReply: {2})", new Object[]{this.lastServerLSN.asString(), clockTime, replyRequired});
        }
        return replyRequired;
    }

    private ByteBuffer processXLogData(ByteBuffer buffer) {
        long startLsn = buffer.getLong();
        this.lastServerLSN = LogSequenceNumber.valueOf(buffer.getLong());
        long systemClock = buffer.getLong();
        switch (this.replicationType) {
            case LOGICAL: {
                this.lastReceiveLSN = LogSequenceNumber.valueOf(startLsn);
                break;
            }
            case PHYSICAL: {
                int payloadSize = buffer.limit() - buffer.position();
                this.lastReceiveLSN = LogSequenceNumber.valueOf(startLsn + (long)payloadSize);
            }
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "  <=BE XLogData(currWal: {0}, lastServerWal: {1}, clock: {2})", new Object[]{this.lastReceiveLSN.asString(), this.lastServerLSN.asString(), systemClock});
        }
        return buffer.slice();
    }

    private void checkClose() throws PSQLException {
        if (this.isClosed()) {
            throw new PSQLException(GT.tr("This replication stream has been closed.", new Object[0]), PSQLState.CONNECTION_DOES_NOT_EXIST);
        }
    }

    @Override
    public void close() throws SQLException {
        if (this.isClosed()) {
            return;
        }
        LOGGER.log(Level.FINEST, " FE=> StopReplication");
        this.copyDual.endCopy();
        this.closeFlag = true;
    }
}

