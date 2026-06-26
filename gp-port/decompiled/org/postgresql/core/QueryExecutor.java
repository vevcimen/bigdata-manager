/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGNotification;
import org.postgresql.copy.CopyOperation;
import org.postgresql.core.CachedQuery;
import org.postgresql.core.Encoding;
import org.postgresql.core.NativeQuery;
import org.postgresql.core.ParameterList;
import org.postgresql.core.Query;
import org.postgresql.core.ReplicationProtocol;
import org.postgresql.core.ResultCursor;
import org.postgresql.core.ResultHandler;
import org.postgresql.core.TransactionState;
import org.postgresql.core.v3.TypeTransferModeRegistry;
import org.postgresql.jdbc.AutoSave;
import org.postgresql.jdbc.BatchResultHandler;
import org.postgresql.jdbc.EscapeSyntaxCallMode;
import org.postgresql.jdbc.PreferQueryMode;
import org.postgresql.util.HostSpec;

public interface QueryExecutor
extends TypeTransferModeRegistry {
    public static final int QUERY_ONESHOT = 1;
    public static final int QUERY_NO_METADATA = 2;
    public static final int QUERY_NO_RESULTS = 4;
    public static final int QUERY_FORWARD_CURSOR = 8;
    public static final int QUERY_SUPPRESS_BEGIN = 16;
    public static final int QUERY_DESCRIBE_ONLY = 32;
    public static final int QUERY_BOTH_ROWS_AND_STATUS = 64;
    public static final int QUERY_FORCE_DESCRIBE_PORTAL = 512;
    @Deprecated
    public static final int QUERY_DISALLOW_BATCHING = 128;
    public static final int QUERY_NO_BINARY_TRANSFER = 256;
    public static final int QUERY_EXECUTE_AS_SIMPLE = 1024;
    public static final int MAX_SAVE_POINTS = 1000;
    public static final int QUERY_READ_ONLY_HINT = 2048;

    public void execute(Query var1, @Nullable ParameterList var2, ResultHandler var3, int var4, int var5, int var6) throws SQLException;

    public void execute(Query var1, @Nullable ParameterList var2, ResultHandler var3, int var4, int var5, int var6, boolean var7) throws SQLException;

    public void execute(Query[] var1, @Nullable ParameterList[] var2, BatchResultHandler var3, int var4, int var5, int var6) throws SQLException;

    public void execute(Query[] var1, @Nullable ParameterList[] var2, BatchResultHandler var3, int var4, int var5, int var6, boolean var7) throws SQLException;

    public void fetch(ResultCursor var1, ResultHandler var2, int var3, boolean var4) throws SQLException;

    public Query createSimpleQuery(String var1) throws SQLException;

    public boolean isReWriteBatchedInsertsEnabled();

    public CachedQuery createQuery(String var1, boolean var2, boolean var3, String ... var4) throws SQLException;

    public Object createQueryKey(String var1, boolean var2, boolean var3, String ... var4);

    public CachedQuery createQueryByKey(Object var1) throws SQLException;

    public CachedQuery borrowQueryByKey(Object var1) throws SQLException;

    public CachedQuery borrowQuery(String var1) throws SQLException;

    public CachedQuery borrowCallableQuery(String var1) throws SQLException;

    public CachedQuery borrowReturningQuery(String var1, String @Nullable [] var2) throws SQLException;

    public void releaseQuery(CachedQuery var1);

    public Query wrap(List<NativeQuery> var1);

    public void processNotifies() throws SQLException;

    public void processNotifies(int var1) throws SQLException;

    @Deprecated
    public ParameterList createFastpathParameters(int var1);

    @Deprecated
    public byte @Nullable [] fastpathCall(int var1, ParameterList var2, boolean var3) throws SQLException;

    public CopyOperation startCopy(String var1, boolean var2) throws SQLException;

    public int getProtocolVersion();

    public void setBinaryReceiveOids(Set<Integer> var1);

    public void setBinarySendOids(Set<Integer> var1);

    public boolean getIntegerDateTimes();

    public HostSpec getHostSpec();

    public String getUser();

    public String getDatabase();

    public void sendQueryCancel() throws SQLException;

    public int getBackendPID();

    public void abort();

    public void close();

    public boolean isClosed();

    public String getServerVersion();

    public PGNotification[] getNotifications() throws SQLException;

    public @Nullable SQLWarning getWarnings();

    public int getServerVersionNum();

    public TransactionState getTransactionState();

    public boolean getStandardConformingStrings();

    public boolean getQuoteReturningIdentifiers();

    public @Nullable TimeZone getTimeZone();

    public Encoding getEncoding();

    public String getApplicationName();

    public boolean isColumnSanitiserDisabled();

    public EscapeSyntaxCallMode getEscapeSyntaxCallMode();

    public PreferQueryMode getPreferQueryMode();

    public AutoSave getAutoSave();

    public void setAutoSave(AutoSave var1);

    public boolean willHealOnRetry(SQLException var1);

    public void setFlushCacheOnDeallocate(boolean var1);

    public ReplicationProtocol getReplicationProtocol();

    public void setNetworkTimeout(int var1) throws IOException;

    public int getNetworkTimeout() throws IOException;

    public Map<String, String> getParameterStatuses();

    public @Nullable String getParameterStatus(String var1);

    public int getAdaptiveFetchSize(boolean var1, ResultCursor var2);

    public boolean getAdaptiveFetch();

    public void setAdaptiveFetch(boolean var1);

    public void addQueryToAdaptiveFetchCache(boolean var1, ResultCursor var2);

    public void removeQueryFromAdaptiveFetchCache(boolean var1, ResultCursor var2);
}

