/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGNotification;
import org.postgresql.PGProperty;
import org.postgresql.core.BaseQueryKey;
import org.postgresql.core.CachedQuery;
import org.postgresql.core.CachedQueryCreateAction;
import org.postgresql.core.CallableQueryKey;
import org.postgresql.core.Encoding;
import org.postgresql.core.PGStream;
import org.postgresql.core.QueryExecutor;
import org.postgresql.core.QueryWithReturningColumnsKey;
import org.postgresql.core.TransactionState;
import org.postgresql.core.Utils;
import org.postgresql.jdbc.AutoSave;
import org.postgresql.jdbc.EscapeSyntaxCallMode;
import org.postgresql.jdbc.PreferQueryMode;
import org.postgresql.util.HostSpec;
import org.postgresql.util.LruCache;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.ServerErrorMessage;

public abstract class QueryExecutorBase
implements QueryExecutor {
    private static final Logger LOGGER = Logger.getLogger(QueryExecutorBase.class.getName());
    protected final PGStream pgStream;
    private final String user;
    private final String database;
    private final int cancelSignalTimeout;
    private int cancelPid;
    private int cancelKey;
    private boolean closed = false;
    private @MonotonicNonNull String serverVersion;
    private int serverVersionNum = 0;
    private TransactionState transactionState = TransactionState.IDLE;
    private final boolean reWriteBatchedInserts;
    private final boolean columnSanitiserDisabled;
    private final EscapeSyntaxCallMode escapeSyntaxCallMode;
    private final boolean quoteReturningIdentifiers;
    private final PreferQueryMode preferQueryMode;
    private AutoSave autoSave;
    private boolean flushCacheOnDeallocate = true;
    protected final boolean logServerErrorDetail;
    private boolean standardConformingStrings = false;
    private @Nullable SQLWarning warnings;
    private final ArrayList<PGNotification> notifications = new ArrayList();
    private final LruCache<Object, CachedQuery> statementCache;
    private final CachedQueryCreateAction cachedQueryCreateAction;
    private final TreeMap<String, String> parameterStatuses = new TreeMap(String.CASE_INSENSITIVE_ORDER);

    protected QueryExecutorBase(PGStream pgStream, int cancelSignalTimeout, Properties info) throws SQLException {
        this.pgStream = pgStream;
        this.user = PGProperty.USER.get(info);
        this.database = PGProperty.PG_DBNAME.get(info);
        this.cancelSignalTimeout = cancelSignalTimeout;
        this.reWriteBatchedInserts = PGProperty.REWRITE_BATCHED_INSERTS.getBoolean(info);
        this.columnSanitiserDisabled = PGProperty.DISABLE_COLUMN_SANITISER.getBoolean(info);
        String callMode = PGProperty.ESCAPE_SYNTAX_CALL_MODE.get(info);
        this.escapeSyntaxCallMode = EscapeSyntaxCallMode.of(callMode);
        this.quoteReturningIdentifiers = PGProperty.QUOTE_RETURNING_IDENTIFIERS.getBoolean(info);
        String preferMode = PGProperty.PREFER_QUERY_MODE.get(info);
        this.preferQueryMode = PreferQueryMode.of(preferMode);
        this.autoSave = AutoSave.of(PGProperty.AUTOSAVE.get(info));
        this.logServerErrorDetail = PGProperty.LOG_SERVER_ERROR_DETAIL.getBoolean(info);
        this.cachedQueryCreateAction = new CachedQueryCreateAction(this);
        this.statementCache = new LruCache<Object, CachedQuery>(Math.max(0, PGProperty.PREPARED_STATEMENT_CACHE_QUERIES.getInt(info)), Math.max(0L, (long)PGProperty.PREPARED_STATEMENT_CACHE_SIZE_MIB.getInt(info) * 1024L * 1024L), false, this.cachedQueryCreateAction, new LruCache.EvictAction<CachedQuery>(){

            @Override
            public void evict(CachedQuery cachedQuery) throws SQLException {
                cachedQuery.query.close();
            }
        });
    }

    protected abstract void sendCloseMessage() throws IOException;

    @Override
    public void setNetworkTimeout(int milliseconds) throws IOException {
        this.pgStream.setNetworkTimeout(milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws IOException {
        return this.pgStream.getNetworkTimeout();
    }

    @Override
    public HostSpec getHostSpec() {
        return this.pgStream.getHostSpec();
    }

    @Override
    public String getUser() {
        return this.user;
    }

    @Override
    public String getDatabase() {
        return this.database;
    }

    public void setBackendKeyData(int cancelPid, int cancelKey) {
        this.cancelPid = cancelPid;
        this.cancelKey = cancelKey;
    }

    @Override
    public int getBackendPID() {
        return this.cancelPid;
    }

    @Override
    public void abort() {
        try {
            this.pgStream.getSocket().close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.closed = true;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        try {
            LOGGER.log(Level.FINEST, " FE=> Terminate");
            this.sendCloseMessage();
            this.pgStream.flush();
            this.pgStream.close();
        }
        catch (IOException ioe) {
            LOGGER.log(Level.FINEST, "Discarding IOException on close:", ioe);
        }
        this.closed = true;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void sendQueryCancel() throws SQLException {
        PGStream cancelStream = null;
        try {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, " FE=> CancelRequest(pid={0},ckey={1})", new Object[]{this.cancelPid, this.cancelKey});
            }
            cancelStream = new PGStream(this.pgStream.getSocketFactory(), this.pgStream.getHostSpec(), this.cancelSignalTimeout);
            if (this.cancelSignalTimeout > 0) {
                cancelStream.setNetworkTimeout(this.cancelSignalTimeout);
            }
            cancelStream.sendInteger4(16);
            cancelStream.sendInteger2(1234);
            cancelStream.sendInteger2(5678);
            cancelStream.sendInteger4(this.cancelPid);
            cancelStream.sendInteger4(this.cancelKey);
            cancelStream.flush();
            cancelStream.receiveEOF();
        }
        catch (IOException e) {
            LOGGER.log(Level.FINEST, "Ignoring exception on cancel request:", e);
        }
        finally {
            if (cancelStream != null) {
                try {
                    cancelStream.close();
                }
                catch (IOException iOException) {}
            }
        }
    }

    public synchronized void addWarning(SQLWarning newWarning) {
        if (this.warnings == null) {
            this.warnings = newWarning;
        } else {
            this.warnings.setNextWarning(newWarning);
        }
    }

    public synchronized void addNotification(PGNotification notification) {
        this.notifications.add(notification);
    }

    @Override
    public synchronized PGNotification[] getNotifications() throws SQLException {
        PGNotification[] array = this.notifications.toArray(new PGNotification[0]);
        this.notifications.clear();
        return array;
    }

    @Override
    public synchronized @Nullable SQLWarning getWarnings() {
        SQLWarning chain = this.warnings;
        this.warnings = null;
        return chain;
    }

    @Override
    public String getServerVersion() {
        String serverVersion = this.serverVersion;
        if (serverVersion == null) {
            throw new IllegalStateException("serverVersion must not be null");
        }
        return serverVersion;
    }

    @Override
    public int getServerVersionNum() {
        if (this.serverVersionNum != 0) {
            return this.serverVersionNum;
        }
        this.serverVersionNum = Utils.parseServerVersionStr(this.getServerVersion());
        return this.serverVersionNum;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public void setServerVersionNum(int serverVersionNum) {
        this.serverVersionNum = serverVersionNum;
    }

    public synchronized void setTransactionState(TransactionState state) {
        this.transactionState = state;
    }

    public synchronized void setStandardConformingStrings(boolean value) {
        this.standardConformingStrings = value;
    }

    @Override
    public synchronized boolean getStandardConformingStrings() {
        return this.standardConformingStrings;
    }

    @Override
    public boolean getQuoteReturningIdentifiers() {
        return this.quoteReturningIdentifiers;
    }

    @Override
    public synchronized TransactionState getTransactionState() {
        return this.transactionState;
    }

    public void setEncoding(Encoding encoding) throws IOException {
        this.pgStream.setEncoding(encoding);
    }

    @Override
    public Encoding getEncoding() {
        return this.pgStream.getEncoding();
    }

    @Override
    public boolean isReWriteBatchedInsertsEnabled() {
        return this.reWriteBatchedInserts;
    }

    @Override
    public final CachedQuery borrowQuery(String sql) throws SQLException {
        return this.statementCache.borrow(sql);
    }

    @Override
    public final CachedQuery borrowCallableQuery(String sql) throws SQLException {
        return this.statementCache.borrow(new CallableQueryKey(sql));
    }

    @Override
    public final CachedQuery borrowReturningQuery(String sql, String @Nullable [] columnNames) throws SQLException {
        return this.statementCache.borrow(new QueryWithReturningColumnsKey(sql, true, true, columnNames));
    }

    @Override
    public CachedQuery borrowQueryByKey(Object key) throws SQLException {
        return this.statementCache.borrow(key);
    }

    @Override
    public void releaseQuery(CachedQuery cachedQuery) {
        this.statementCache.put(cachedQuery.key, cachedQuery);
    }

    @Override
    public final Object createQueryKey(String sql, boolean escapeProcessing, boolean isParameterized, String ... columnNames) {
        Object key = columnNames == null || columnNames.length != 0 ? new QueryWithReturningColumnsKey(sql, isParameterized, escapeProcessing, columnNames) : (isParameterized ? sql : new BaseQueryKey(sql, false, escapeProcessing));
        return key;
    }

    @Override
    public CachedQuery createQueryByKey(Object key) throws SQLException {
        return this.cachedQueryCreateAction.create(key);
    }

    @Override
    public final CachedQuery createQuery(String sql, boolean escapeProcessing, boolean isParameterized, String ... columnNames) throws SQLException {
        Object key = this.createQueryKey(sql, escapeProcessing, isParameterized, columnNames);
        return this.createQueryByKey(key);
    }

    @Override
    public boolean isColumnSanitiserDisabled() {
        return this.columnSanitiserDisabled;
    }

    @Override
    public EscapeSyntaxCallMode getEscapeSyntaxCallMode() {
        return this.escapeSyntaxCallMode;
    }

    @Override
    public PreferQueryMode getPreferQueryMode() {
        return this.preferQueryMode;
    }

    @Override
    public AutoSave getAutoSave() {
        return this.autoSave;
    }

    @Override
    public void setAutoSave(AutoSave autoSave) {
        this.autoSave = autoSave;
    }

    protected boolean willHealViaReparse(SQLException e) {
        if (e == null || e.getSQLState() == null) {
            return false;
        }
        if (PSQLState.INVALID_SQL_STATEMENT_NAME.getState().equals(e.getSQLState())) {
            return true;
        }
        if (!PSQLState.NOT_IMPLEMENTED.getState().equals(e.getSQLState())) {
            return false;
        }
        if (!(e instanceof PSQLException)) {
            return false;
        }
        PSQLException pe = (PSQLException)e;
        ServerErrorMessage serverErrorMessage = pe.getServerErrorMessage();
        if (serverErrorMessage == null) {
            return false;
        }
        String routine = serverErrorMessage.getRoutine();
        return "RevalidateCachedQuery".equals(routine) || "RevalidateCachedPlan".equals(routine);
    }

    @Override
    public boolean willHealOnRetry(SQLException e) {
        if (this.autoSave == AutoSave.NEVER && this.getTransactionState() == TransactionState.FAILED) {
            return false;
        }
        return this.willHealViaReparse(e);
    }

    public boolean isFlushCacheOnDeallocate() {
        return this.flushCacheOnDeallocate;
    }

    @Override
    public void setFlushCacheOnDeallocate(boolean flushCacheOnDeallocate) {
        this.flushCacheOnDeallocate = flushCacheOnDeallocate;
    }

    protected boolean hasNotifications() {
        return this.notifications.size() > 0;
    }

    @Override
    public final Map<String, String> getParameterStatuses() {
        return Collections.unmodifiableMap(this.parameterStatuses);
    }

    @Override
    public final @Nullable String getParameterStatus(String parameterName) {
        return this.parameterStatuses.get(parameterName);
    }

    protected void onParameterStatus(String parameterName, String parameterStatus) {
        if (parameterName == null || parameterName.equals("")) {
            throw new IllegalStateException("attempt to set GUC_REPORT parameter with null or empty-string name");
        }
        this.parameterStatuses.put(parameterName, parameterStatus);
    }
}

