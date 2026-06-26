/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.dataflow.qual.Pure;
import org.postgresql.PGConnection;
import org.postgresql.core.CachedQuery;
import org.postgresql.core.Encoding;
import org.postgresql.core.QueryExecutor;
import org.postgresql.core.ReplicationProtocol;
import org.postgresql.core.TransactionState;
import org.postgresql.core.TypeInfo;
import org.postgresql.core.Version;
import org.postgresql.jdbc.FieldMetadata;
import org.postgresql.jdbc.TimestampUtils;
import org.postgresql.util.LruCache;
import org.postgresql.xml.PGXmlFactoryFactory;

public interface BaseConnection
extends PGConnection,
Connection {
    @Override
    public void cancelQuery() throws SQLException;

    public ResultSet execSQLQuery(String var1) throws SQLException;

    public ResultSet execSQLQuery(String var1, int var2, int var3) throws SQLException;

    public void execSQLUpdate(String var1) throws SQLException;

    public QueryExecutor getQueryExecutor();

    public ReplicationProtocol getReplicationProtocol();

    public Object getObject(String var1, @Nullable String var2, byte @Nullable [] var3) throws SQLException;

    @Pure
    public Encoding getEncoding() throws SQLException;

    public TypeInfo getTypeInfo();

    public boolean haveMinimumServerVersion(int var1);

    public boolean haveMinimumServerVersion(Version var1);

    public byte @PolyNull [] encodeString(@PolyNull String var1) throws SQLException;

    public String escapeString(String var1) throws SQLException;

    public boolean getStandardConformingStrings();

    public TimestampUtils getTimestampUtils();

    public Logger getLogger();

    public boolean getStringVarcharFlag();

    public TransactionState getTransactionState();

    public boolean binaryTransferSend(int var1);

    public boolean isColumnSanitiserDisabled();

    public void addTimerTask(TimerTask var1, long var2);

    public void purgeTimerTasks();

    public LruCache<FieldMetadata.Key, FieldMetadata> getFieldMetadataCache();

    public CachedQuery createQuery(String var1, boolean var2, boolean var3, String ... var4) throws SQLException;

    public void setFlushCacheOnDeallocate(boolean var1);

    public boolean hintReadOnly();

    public PGXmlFactoryFactory getXmlFactoryFactory() throws SQLException;

    public boolean getLogServerErrorDetail();
}

