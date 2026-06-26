/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3;

import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGProperty;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyOperation;
import org.postgresql.copy.CopyOut;
import org.postgresql.core.CommandCompleteParser;
import org.postgresql.core.Encoding;
import org.postgresql.core.EncodingPredictor;
import org.postgresql.core.Field;
import org.postgresql.core.NativeQuery;
import org.postgresql.core.Notification;
import org.postgresql.core.Oid;
import org.postgresql.core.PGBindException;
import org.postgresql.core.PGStream;
import org.postgresql.core.ParameterList;
import org.postgresql.core.Parser;
import org.postgresql.core.Query;
import org.postgresql.core.QueryExecutorBase;
import org.postgresql.core.ReplicationProtocol;
import org.postgresql.core.ResultCursor;
import org.postgresql.core.ResultHandler;
import org.postgresql.core.ResultHandlerBase;
import org.postgresql.core.ResultHandlerDelegate;
import org.postgresql.core.SqlCommand;
import org.postgresql.core.SqlCommandType;
import org.postgresql.core.TransactionState;
import org.postgresql.core.Tuple;
import org.postgresql.core.v3.BatchedQuery;
import org.postgresql.core.v3.CompositeQuery;
import org.postgresql.core.v3.CopyDualImpl;
import org.postgresql.core.v3.CopyInImpl;
import org.postgresql.core.v3.CopyOperationImpl;
import org.postgresql.core.v3.CopyOutImpl;
import org.postgresql.core.v3.DescribeRequest;
import org.postgresql.core.v3.ExecuteRequest;
import org.postgresql.core.v3.Portal;
import org.postgresql.core.v3.SimpleParameterList;
import org.postgresql.core.v3.SimpleQuery;
import org.postgresql.core.v3.V3ParameterList;
import org.postgresql.core.v3.adaptivefetch.AdaptiveFetchCache;
import org.postgresql.core.v3.replication.V3ReplicationProtocol;
import org.postgresql.jdbc.AutoSave;
import org.postgresql.jdbc.BatchResultHandler;
import org.postgresql.jdbc.TimestampUtils;
import org.postgresql.util.ByteStreamWriter;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.PSQLWarning;
import org.postgresql.util.ServerErrorMessage;
import org.postgresql.util.internal.Nullness;

public class QueryExecutorImpl
extends QueryExecutorBase {
    private static final Logger LOGGER = Logger.getLogger(QueryExecutorImpl.class.getName());
    private static final Field[] NO_FIELDS = new Field[0];
    private @Nullable TimeZone timeZone;
    private @Nullable String applicationName;
    private boolean integerDateTimes;
    private final Set<Integer> useBinaryReceiveForOids = new HashSet<Integer>();
    private final Set<Integer> useBinarySendForOids = new HashSet<Integer>();
    private final SimpleQuery sync;
    private short deallocateEpoch;
    private @Nullable String lastSetSearchPathQuery;
    private @Nullable SQLException transactionFailCause;
    private final ReplicationProtocol replicationProtocol;
    private final CommandCompleteParser commandCompleteParser;
    private final AdaptiveFetchCache adaptiveFetchCache;
    private @Nullable Object lockedFor;
    private static final int MAX_BUFFERED_RECV_BYTES = 64000;
    private static final int NODATA_QUERY_RESPONSE_SIZE_BYTES = 250;
    AtomicBoolean processingCopyResults;
    private final HashMap<PhantomReference<SimpleQuery>, String> parsedQueryMap;
    private final ReferenceQueue<SimpleQuery> parsedQueryCleanupQueue;
    private final HashMap<PhantomReference<Portal>, String> openPortalMap;
    private final ReferenceQueue<Portal> openPortalCleanupQueue;
    private static final Portal UNNAMED_PORTAL;
    private final Deque<SimpleQuery> pendingParseQueue;
    private final Deque<Portal> pendingBindQueue;
    private final Deque<ExecuteRequest> pendingExecuteQueue;
    private final Deque<DescribeRequest> pendingDescribeStatementQueue;
    private final Deque<SimpleQuery> pendingDescribePortalQueue;
    private long nextUniqueID;
    private final boolean allowEncodingChanges;
    private final boolean cleanupSavePoints;
    private int estimatedReceiveBufferBytes;
    private final SimpleQuery beginTransactionQuery;
    private final SimpleQuery beginReadOnlyTransactionQuery;
    private final SimpleQuery emptyQuery;
    private final SimpleQuery autoSaveQuery;
    private final SimpleQuery releaseAutoSave;
    private final SimpleQuery restoreToAutoSave;

    public QueryExecutorImpl(PGStream pgStream, int cancelSignalTimeout, Properties info) throws SQLException, IOException {
        super(pgStream, cancelSignalTimeout, info);
        this.sync = (SimpleQuery)this.createQuery((String)"SYNC", (boolean)false, (boolean)true, (String[])new String[0]).query;
        this.commandCompleteParser = new CommandCompleteParser();
        this.processingCopyResults = new AtomicBoolean(false);
        this.parsedQueryMap = new HashMap();
        this.parsedQueryCleanupQueue = new ReferenceQueue();
        this.openPortalMap = new HashMap();
        this.openPortalCleanupQueue = new ReferenceQueue();
        this.pendingParseQueue = new ArrayDeque<SimpleQuery>();
        this.pendingBindQueue = new ArrayDeque<Portal>();
        this.pendingExecuteQueue = new ArrayDeque<ExecuteRequest>();
        this.pendingDescribeStatementQueue = new ArrayDeque<DescribeRequest>();
        this.pendingDescribePortalQueue = new ArrayDeque<SimpleQuery>();
        this.nextUniqueID = 1L;
        this.estimatedReceiveBufferBytes = 0;
        this.beginTransactionQuery = new SimpleQuery(new NativeQuery("BEGIN", new int[0], false, SqlCommand.BLANK), null, false);
        this.beginReadOnlyTransactionQuery = new SimpleQuery(new NativeQuery("BEGIN READ ONLY", new int[0], false, SqlCommand.BLANK), null, false);
        this.emptyQuery = new SimpleQuery(new NativeQuery("", new int[0], false, SqlCommand.createStatementTypeInfo(SqlCommandType.BLANK)), null, false);
        this.autoSaveQuery = new SimpleQuery(new NativeQuery("SAVEPOINT PGJDBC_AUTOSAVE", new int[0], false, SqlCommand.BLANK), null, false);
        this.releaseAutoSave = new SimpleQuery(new NativeQuery("RELEASE SAVEPOINT PGJDBC_AUTOSAVE", new int[0], false, SqlCommand.BLANK), null, false);
        this.restoreToAutoSave = new SimpleQuery(new NativeQuery("ROLLBACK TO SAVEPOINT PGJDBC_AUTOSAVE", new int[0], false, SqlCommand.BLANK), null, false);
        long maxResultBuffer = pgStream.getMaxResultBuffer();
        this.adaptiveFetchCache = new AdaptiveFetchCache(maxResultBuffer, info);
        this.allowEncodingChanges = PGProperty.ALLOW_ENCODING_CHANGES.getBoolean(info);
        this.cleanupSavePoints = PGProperty.CLEANUP_SAVEPOINTS.getBoolean(info);
        this.replicationProtocol = new V3ReplicationProtocol(this, pgStream);
        this.readStartupMessages();
    }

    @Override
    public int getProtocolVersion() {
        return 3;
    }

    private void lock(Object obtainer) throws PSQLException {
        if (this.lockedFor == obtainer) {
            throw new PSQLException(GT.tr("Tried to obtain lock while already holding it", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        this.waitOnLock();
        this.lockedFor = obtainer;
    }

    private void unlock(Object holder) throws PSQLException {
        if (this.lockedFor != holder) {
            throw new PSQLException(GT.tr("Tried to break lock on database connection", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        this.lockedFor = null;
        this.notify();
    }

    private void waitOnLock() throws PSQLException {
        while (this.lockedFor != null) {
            try {
                this.wait();
            }
            catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new PSQLException(GT.tr("Interrupted while waiting to obtain lock on database connection", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE, (Throwable)ie);
            }
        }
    }

    boolean hasLock(@Nullable Object holder) {
        return this.lockedFor == holder;
    }

    @Override
    public Query createSimpleQuery(String sql) throws SQLException {
        List<NativeQuery> queries = Parser.parseJdbcSql(sql, this.getStandardConformingStrings(), false, true, this.isReWriteBatchedInsertsEnabled(), this.getQuoteReturningIdentifiers(), new String[0]);
        return this.wrap(queries);
    }

    @Override
    public Query wrap(List<NativeQuery> queries) {
        if (queries.isEmpty()) {
            return this.emptyQuery;
        }
        if (queries.size() == 1) {
            NativeQuery firstQuery = queries.get(0);
            if (this.isReWriteBatchedInsertsEnabled() && firstQuery.getCommand().isBatchedReWriteCompatible()) {
                int valuesBraceOpenPosition = firstQuery.getCommand().getBatchRewriteValuesBraceOpenPosition();
                int valuesBraceClosePosition = firstQuery.getCommand().getBatchRewriteValuesBraceClosePosition();
                return new BatchedQuery(firstQuery, this, valuesBraceOpenPosition, valuesBraceClosePosition, this.isColumnSanitiserDisabled());
            }
            return new SimpleQuery(firstQuery, this, this.isColumnSanitiserDisabled());
        }
        SimpleQuery[] subqueries = new SimpleQuery[queries.size()];
        int[] offsets = new int[subqueries.length];
        int offset = 0;
        for (int i = 0; i < queries.size(); ++i) {
            NativeQuery nativeQuery = queries.get(i);
            offsets[i] = offset;
            subqueries[i] = new SimpleQuery(nativeQuery, this, this.isColumnSanitiserDisabled());
            offset += nativeQuery.bindPositions.length;
        }
        return new CompositeQuery(subqueries, offsets);
    }

    private int updateQueryMode(int flags) {
        switch (this.getPreferQueryMode()) {
            case SIMPLE: {
                return flags | 0x400;
            }
            case EXTENDED: {
                return flags & 0xFFFFFBFF;
            }
        }
        return flags;
    }

    @Override
    public synchronized void execute(Query query, @Nullable ParameterList parameters, ResultHandler handler, int maxRows, int fetchSize, int flags) throws SQLException {
        this.execute(query, parameters, handler, maxRows, fetchSize, flags, false);
    }

    @Override
    public synchronized void execute(Query query, @Nullable ParameterList parameters, ResultHandler handler, int maxRows, int fetchSize, int flags, boolean adaptiveFetch) throws SQLException {
        this.waitOnLock();
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "  simple execute, handler={0}, maxRows={1}, fetchSize={2}, flags={3}", new Object[]{handler, maxRows, fetchSize, flags});
        }
        if (parameters == null) {
            parameters = SimpleQuery.NO_PARAMETERS;
        }
        boolean describeOnly = (0x20 & (flags = this.updateQueryMode(flags))) != 0;
        ((V3ParameterList)parameters).convertFunctionOutParameters();
        if (!describeOnly) {
            ((V3ParameterList)parameters).checkAllParametersSet();
        }
        boolean autosave = false;
        try {
            try {
                handler = this.sendQueryPreamble(handler, flags);
                autosave = this.sendAutomaticSavepoint(query, flags);
                this.sendQuery(query, (V3ParameterList)parameters, maxRows, fetchSize, flags, handler, null, adaptiveFetch);
                if ((flags & 0x400) == 0) {
                    this.sendSync();
                }
                this.processResults(handler, flags, adaptiveFetch);
                this.estimatedReceiveBufferBytes = 0;
            }
            catch (PGBindException se) {
                this.sendSync();
                this.processResults(handler, flags, adaptiveFetch);
                this.estimatedReceiveBufferBytes = 0;
                handler.handleError(new PSQLException(GT.tr("Unable to bind parameter values for statement.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE, (Throwable)se.getIOException()));
            }
        }
        catch (IOException e) {
            this.abort();
            handler.handleError(new PSQLException(GT.tr("An I/O error occurred while sending to the backend.", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)e));
        }
        try {
            handler.handleCompletion();
            if (this.cleanupSavePoints) {
                this.releaseSavePoint(autosave, flags);
            }
        }
        catch (SQLException e) {
            this.rollbackIfRequired(autosave, e);
        }
    }

    private boolean sendAutomaticSavepoint(Query query, int flags) throws IOException {
        if (!((flags & 0x10) != 0 && this.getTransactionState() != TransactionState.OPEN || query == this.restoreToAutoSave || query.getNativeSql().equalsIgnoreCase("COMMIT") || this.getAutoSave() == AutoSave.NEVER || this.getAutoSave() != AutoSave.ALWAYS && query instanceof SimpleQuery && ((SimpleQuery)query).getFields() == null)) {
            this.sendOneQuery(this.autoSaveQuery, SimpleQuery.NO_PARAMETERS, 1, 0, 1030);
            return true;
        }
        return false;
    }

    private void releaseSavePoint(boolean autosave, int flags) throws SQLException {
        if (autosave && this.getAutoSave() == AutoSave.ALWAYS && this.getTransactionState() == TransactionState.OPEN) {
            try {
                this.sendOneQuery(this.releaseAutoSave, SimpleQuery.NO_PARAMETERS, 1, 0, 1030);
            }
            catch (IOException ex) {
                throw new PSQLException(GT.tr("Error releasing savepoint", new Object[0]), PSQLState.IO_ERROR);
            }
        }
    }

    private void rollbackIfRequired(boolean autosave, SQLException e) throws SQLException {
        if (autosave && this.getTransactionState() == TransactionState.FAILED && (this.getAutoSave() == AutoSave.ALWAYS || this.willHealOnRetry(e))) {
            try {
                this.execute(this.restoreToAutoSave, SimpleQuery.NO_PARAMETERS, new ResultHandlerDelegate(null), 1, 0, 1030);
            }
            catch (SQLException e2) {
                e.setNextException(e2);
            }
        }
        throw e;
    }

    @Override
    public synchronized void execute(Query[] queries, @Nullable ParameterList[] parameterLists, BatchResultHandler batchHandler, int maxRows, int fetchSize, int flags) throws SQLException {
        this.execute(queries, parameterLists, batchHandler, maxRows, fetchSize, flags, false);
    }

    @Override
    public synchronized void execute(Query[] queries, @Nullable ParameterList[] parameterLists, BatchResultHandler batchHandler, int maxRows, int fetchSize, int flags, boolean adaptiveFetch) throws SQLException {
        boolean describeOnly;
        this.waitOnLock();
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "  batch execute {0} queries, handler={1}, maxRows={2}, fetchSize={3}, flags={4}", new Object[]{queries.length, batchHandler, maxRows, fetchSize, flags});
        }
        boolean bl = describeOnly = (0x20 & (flags = this.updateQueryMode(flags))) != 0;
        if (!describeOnly) {
            for (ParameterList parameterList : parameterLists) {
                if (parameterList == null) continue;
                ((V3ParameterList)parameterList).checkAllParametersSet();
            }
        }
        boolean autosave = false;
        ResultHandler handler = batchHandler;
        try {
            handler = this.sendQueryPreamble(batchHandler, flags);
            autosave = this.sendAutomaticSavepoint(queries[0], flags);
            this.estimatedReceiveBufferBytes = 0;
            for (int i = 0; i < queries.length; ++i) {
                Query query = queries[i];
                V3ParameterList parameters = (V3ParameterList)parameterLists[i];
                if (parameters == null) {
                    parameters = SimpleQuery.NO_PARAMETERS;
                }
                this.sendQuery(query, parameters, maxRows, fetchSize, flags, handler, batchHandler, adaptiveFetch);
                if (handler.getException() != null) break;
            }
            if (handler.getException() == null) {
                if ((flags & 0x400) == 0) {
                    this.sendSync();
                }
                this.processResults(handler, flags, adaptiveFetch);
                this.estimatedReceiveBufferBytes = 0;
            }
        }
        catch (IOException e) {
            this.abort();
            handler.handleError(new PSQLException(GT.tr("An I/O error occurred while sending to the backend.", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)e));
        }
        try {
            handler.handleCompletion();
            if (this.cleanupSavePoints) {
                this.releaseSavePoint(autosave, flags);
            }
        }
        catch (SQLException e) {
            this.rollbackIfRequired(autosave, e);
        }
    }

    private ResultHandler sendQueryPreamble(ResultHandler delegateHandler, int flags) throws IOException {
        this.processDeadParsedQueries();
        this.processDeadPortals();
        if ((flags & 0x10) != 0 || this.getTransactionState() != TransactionState.IDLE) {
            return delegateHandler;
        }
        int beginFlags = 2;
        if ((flags & 1) != 0) {
            beginFlags |= 1;
        }
        beginFlags |= 0x400;
        beginFlags = this.updateQueryMode(beginFlags);
        SimpleQuery beginQuery = (flags & 0x800) == 0 ? this.beginTransactionQuery : this.beginReadOnlyTransactionQuery;
        this.sendOneQuery(beginQuery, SimpleQuery.NO_PARAMETERS, 0, 0, beginFlags);
        return new ResultHandlerDelegate(delegateHandler){
            private boolean sawBegin;
            {
                this.sawBegin = false;
            }

            @Override
            public void handleResultRows(Query fromQuery, Field[] fields, List<Tuple> tuples, @Nullable ResultCursor cursor) {
                if (this.sawBegin) {
                    super.handleResultRows(fromQuery, fields, tuples, cursor);
                }
            }

            @Override
            public void handleCommandStatus(String status, long updateCount, long insertOID) {
                if (!this.sawBegin) {
                    this.sawBegin = true;
                    if (!status.equals("BEGIN")) {
                        this.handleError(new PSQLException(GT.tr("Expected command status BEGIN, got {0}.", status), PSQLState.PROTOCOL_VIOLATION));
                    }
                } else {
                    super.handleCommandStatus(status, updateCount, insertOID);
                }
            }
        };
    }

    @Override
    public synchronized byte @Nullable [] fastpathCall(int fnid, ParameterList parameters, boolean suppressBegin) throws SQLException {
        this.waitOnLock();
        if (!suppressBegin) {
            this.doSubprotocolBegin();
        }
        try {
            this.sendFastpathCall(fnid, (SimpleParameterList)parameters);
            return this.receiveFastpathResult();
        }
        catch (IOException ioe) {
            this.abort();
            throw new PSQLException(GT.tr("An I/O error occurred while sending to the backend.", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)ioe);
        }
    }

    public void doSubprotocolBegin() throws SQLException {
        if (this.getTransactionState() == TransactionState.IDLE) {
            LOGGER.log(Level.FINEST, "Issuing BEGIN before fastpath or copy call.");
            ResultHandlerBase handler = new ResultHandlerBase(){
                private boolean sawBegin = false;

                @Override
                public void handleCommandStatus(String status, long updateCount, long insertOID) {
                    if (!this.sawBegin) {
                        if (!status.equals("BEGIN")) {
                            this.handleError(new PSQLException(GT.tr("Expected command status BEGIN, got {0}.", status), PSQLState.PROTOCOL_VIOLATION));
                        }
                        this.sawBegin = true;
                    } else {
                        this.handleError(new PSQLException(GT.tr("Unexpected command status: {0}.", status), PSQLState.PROTOCOL_VIOLATION));
                    }
                }

                @Override
                public void handleWarning(SQLWarning warning) {
                    this.handleError(warning);
                }
            };
            try {
                int beginFlags = 1027;
                beginFlags = this.updateQueryMode(beginFlags);
                this.sendOneQuery(this.beginTransactionQuery, SimpleQuery.NO_PARAMETERS, 0, 0, beginFlags);
                this.sendSync();
                this.processResults(handler, 0);
                this.estimatedReceiveBufferBytes = 0;
            }
            catch (IOException ioe) {
                throw new PSQLException(GT.tr("An I/O error occurred while sending to the backend.", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)ioe);
            }
        }
    }

    @Override
    public ParameterList createFastpathParameters(int count) {
        return new SimpleParameterList(count, this);
    }

    private void sendFastpathCall(int fnid, SimpleParameterList params) throws SQLException, IOException {
        int i;
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, " FE=> FunctionCall({0}, {1} params)", new Object[]{fnid, params.getParameterCount()});
        }
        int paramCount = params.getParameterCount();
        int encodedSize = 0;
        for (i = 1; i <= paramCount; ++i) {
            if (params.isNull(i)) {
                encodedSize += 4;
                continue;
            }
            encodedSize += 4 + params.getV3Length(i);
        }
        this.pgStream.sendChar(70);
        this.pgStream.sendInteger4(10 + 2 * paramCount + 2 + encodedSize + 2);
        this.pgStream.sendInteger4(fnid);
        this.pgStream.sendInteger2(paramCount);
        for (i = 1; i <= paramCount; ++i) {
            this.pgStream.sendInteger2(params.isBinary(i) ? 1 : 0);
        }
        this.pgStream.sendInteger2(paramCount);
        for (i = 1; i <= paramCount; ++i) {
            if (params.isNull(i)) {
                this.pgStream.sendInteger4(-1);
                continue;
            }
            this.pgStream.sendInteger4(params.getV3Length(i));
            params.writeV3Value(i, this.pgStream);
        }
        this.pgStream.sendInteger2(1);
        this.pgStream.flush();
    }

    @Override
    public synchronized void processNotifies() throws SQLException {
        this.processNotifies(-1);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public synchronized void processNotifies(int timeoutMillis) throws SQLException {
        this.waitOnLock();
        if (this.getTransactionState() != TransactionState.IDLE) {
            return;
        }
        if (this.hasNotifications()) {
            timeoutMillis = -1;
        }
        boolean useTimeout = timeoutMillis > 0;
        long startTime = 0L;
        int oldTimeout = 0;
        if (useTimeout) {
            startTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
            try {
                oldTimeout = this.pgStream.getSocket().getSoTimeout();
            }
            catch (SocketException e) {
                throw new PSQLException(GT.tr("An error occurred while trying to get the socket timeout.", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)e);
            }
        }
        try {
            block14: while (true) {
                if (timeoutMillis < 0) {
                    if (!this.pgStream.hasMessagePending()) return;
                }
                if (useTimeout && timeoutMillis >= 0) {
                    this.setSocketTimeout(timeoutMillis);
                }
                int c = this.pgStream.receiveChar();
                if (useTimeout && timeoutMillis >= 0) {
                    this.setSocketTimeout(0);
                }
                switch (c) {
                    case 65: {
                        this.receiveAsyncNotify();
                        timeoutMillis = -1;
                        continue block14;
                    }
                    case 69: {
                        throw this.receiveErrorResponse();
                    }
                    case 78: {
                        SQLWarning warning = this.receiveNoticeResponse();
                        this.addWarning(warning);
                        if (!useTimeout) continue block14;
                        long newTimeMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
                        timeoutMillis = (int)((long)timeoutMillis + (startTime - newTimeMillis));
                        startTime = newTimeMillis;
                        if (timeoutMillis != 0) continue block14;
                        timeoutMillis = -1;
                        continue block14;
                    }
                }
                throw new PSQLException(GT.tr("Unknown Response Type {0}.", Character.valueOf((char)c)), PSQLState.CONNECTION_FAILURE);
            }
        }
        catch (SocketTimeoutException c) {
            return;
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("An I/O error occurred while sending to the backend.", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)ioe);
        }
        finally {
            if (useTimeout) {
                this.setSocketTimeout(oldTimeout);
            }
        }
    }

    private void setSocketTimeout(int millis) throws PSQLException {
        try {
            Socket s2 = this.pgStream.getSocket();
            if (!s2.isClosed()) {
                this.pgStream.setNetworkTimeout(millis);
            }
        }
        catch (IOException e) {
            throw new PSQLException(GT.tr("An error occurred while trying to reset the socket timeout.", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)e);
        }
    }

    private byte @Nullable [] receiveFastpathResult() throws IOException, SQLException {
        boolean endQuery = false;
        SQLException error = null;
        byte[] returnValue = null;
        block10: while (!endQuery) {
            int c = this.pgStream.receiveChar();
            switch (c) {
                case 65: {
                    this.receiveAsyncNotify();
                    continue block10;
                }
                case 69: {
                    SQLException newError = this.receiveErrorResponse();
                    if (error == null) {
                        error = newError;
                        continue block10;
                    }
                    error.setNextException(newError);
                    continue block10;
                }
                case 78: {
                    SQLWarning warning = this.receiveNoticeResponse();
                    this.addWarning(warning);
                    continue block10;
                }
                case 90: {
                    this.receiveRFQ();
                    endQuery = true;
                    continue block10;
                }
                case 86: {
                    int msgLen = this.pgStream.receiveInteger4();
                    int valueLen = this.pgStream.receiveInteger4();
                    LOGGER.log(Level.FINEST, " <=BE FunctionCallResponse({0} bytes)", valueLen);
                    if (valueLen == -1) continue block10;
                    byte[] buf = new byte[valueLen];
                    this.pgStream.receive(buf, 0, valueLen);
                    returnValue = buf;
                    continue block10;
                }
                case 83: {
                    try {
                        this.receiveParameterStatus();
                    }
                    catch (SQLException e) {
                        if (error == null) {
                            error = e;
                        } else {
                            error.setNextException(e);
                        }
                        endQuery = true;
                    }
                    continue block10;
                }
            }
            throw new PSQLException(GT.tr("Unknown Response Type {0}.", Character.valueOf((char)c)), PSQLState.CONNECTION_FAILURE);
        }
        if (error != null) {
            throw error;
        }
        return returnValue;
    }

    @Override
    public synchronized CopyOperation startCopy(String sql, boolean suppressBegin) throws SQLException {
        this.waitOnLock();
        if (!suppressBegin) {
            this.doSubprotocolBegin();
        }
        byte[] buf = sql.getBytes(StandardCharsets.UTF_8);
        try {
            LOGGER.log(Level.FINEST, " FE=> Query(CopyStart)");
            this.pgStream.sendChar(81);
            this.pgStream.sendInteger4(buf.length + 4 + 1);
            this.pgStream.send(buf);
            this.pgStream.sendChar(0);
            this.pgStream.flush();
            return Nullness.castNonNull(this.processCopyResults(null, true));
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("Database connection failed when starting copy", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)ioe);
        }
    }

    private synchronized void initCopy(CopyOperationImpl op) throws SQLException, IOException {
        this.pgStream.receiveInteger4();
        int rowFormat = this.pgStream.receiveChar();
        int numFields = this.pgStream.receiveInteger2();
        int[] fieldFormats = new int[numFields];
        for (int i = 0; i < numFields; ++i) {
            fieldFormats[i] = this.pgStream.receiveInteger2();
        }
        this.lock(op);
        op.init(this, rowFormat, fieldFormats);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void cancelCopy(CopyOperationImpl op) throws SQLException {
        int errors;
        SQLException error;
        block26: {
            if (!this.hasLock(op)) {
                throw new PSQLException(GT.tr("Tried to cancel an inactive copy operation", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
            }
            error = null;
            errors = 0;
            try {
                if (op instanceof CopyIn) {
                    QueryExecutorImpl queryExecutorImpl = this;
                    synchronized (queryExecutorImpl) {
                        LOGGER.log(Level.FINEST, "FE => CopyFail");
                        byte[] msg = "Copy cancel requested".getBytes(StandardCharsets.US_ASCII);
                        this.pgStream.sendChar(102);
                        this.pgStream.sendInteger4(5 + msg.length);
                        this.pgStream.send(msg);
                        this.pgStream.sendChar(0);
                        this.pgStream.flush();
                        do {
                            try {
                                this.processCopyResults(op, true);
                            }
                            catch (SQLException se) {
                                ++errors;
                                if (error != null) {
                                    SQLException next;
                                    SQLException e = se;
                                    while ((next = e.getNextException()) != null) {
                                        e = next;
                                    }
                                    e.setNextException(error);
                                }
                                error = se;
                            }
                        } while (this.hasLock(op));
                        break block26;
                    }
                }
                if (op instanceof CopyOut) {
                    this.sendQueryCancel();
                }
            }
            catch (IOException ioe) {
                throw new PSQLException(GT.tr("Database connection failed when canceling copy operation", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)ioe);
            }
            finally {
                QueryExecutorImpl queryExecutorImpl = this;
                synchronized (queryExecutorImpl) {
                    if (this.hasLock(op)) {
                        this.unlock(op);
                    }
                }
            }
        }
        if (op instanceof CopyIn) {
            if (errors < 1) {
                throw new PSQLException(GT.tr("Missing expected error response to copy cancel request", new Object[0]), PSQLState.COMMUNICATION_ERROR);
            }
            if (errors > 1) {
                throw new PSQLException(GT.tr("Got {0} error responses to single copy cancel request", String.valueOf(errors)), PSQLState.COMMUNICATION_ERROR, error);
            }
        }
    }

    public synchronized long endCopy(CopyOperationImpl op) throws SQLException {
        if (!this.hasLock(op)) {
            throw new PSQLException(GT.tr("Tried to end inactive copy", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        try {
            LOGGER.log(Level.FINEST, " FE=> CopyDone");
            this.pgStream.sendChar(99);
            this.pgStream.sendInteger4(4);
            this.pgStream.flush();
            do {
                this.processCopyResults(op, true);
            } while (this.hasLock(op));
            return op.getHandledRowCount();
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("Database connection failed when ending copy", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)ioe);
        }
    }

    public synchronized void writeToCopy(CopyOperationImpl op, byte[] data, int off, int siz) throws SQLException {
        if (!this.hasLock(op)) {
            throw new PSQLException(GT.tr("Tried to write to an inactive copy operation", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        LOGGER.log(Level.FINEST, " FE=> CopyData({0})", siz);
        try {
            this.pgStream.sendChar(100);
            this.pgStream.sendInteger4(siz + 4);
            this.pgStream.send(data, off, siz);
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("Database connection failed when writing to copy", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)ioe);
        }
    }

    public synchronized void writeToCopy(CopyOperationImpl op, ByteStreamWriter from) throws SQLException {
        if (!this.hasLock(op)) {
            throw new PSQLException(GT.tr("Tried to write to an inactive copy operation", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        int siz = from.getLength();
        LOGGER.log(Level.FINEST, " FE=> CopyData({0})", siz);
        try {
            this.pgStream.sendChar(100);
            this.pgStream.sendInteger4(siz + 4);
            this.pgStream.send(from);
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("Database connection failed when writing to copy", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)ioe);
        }
    }

    public synchronized void flushCopy(CopyOperationImpl op) throws SQLException {
        if (!this.hasLock(op)) {
            throw new PSQLException(GT.tr("Tried to write to an inactive copy operation", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        try {
            this.pgStream.flush();
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("Database connection failed when writing to copy", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)ioe);
        }
    }

    synchronized void readFromCopy(CopyOperationImpl op, boolean block) throws SQLException {
        if (!this.hasLock(op)) {
            throw new PSQLException(GT.tr("Tried to read from inactive copy", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        try {
            this.processCopyResults(op, block);
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("Database connection failed when reading from copy", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)ioe);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Nullable CopyOperationImpl processCopyResults(@Nullable CopyOperationImpl op, boolean block) throws SQLException, IOException {
        if (this.pgStream.isClosed()) {
            throw new PSQLException(GT.tr("PGStream is closed", new Object[0]), PSQLState.CONNECTION_DOES_NOT_EXIST);
        }
        if (!this.processingCopyResults.compareAndSet(false, true)) {
            LOGGER.log(Level.INFO, "Ignoring request to process copy results, already processing");
            return null;
        }
        try {
            boolean endReceiving = false;
            SQLException error = null;
            SQLException errors = null;
            while (!endReceiving && (block || this.pgStream.hasMessagePending())) {
                int c;
                if (!block && (c = this.pgStream.peekChar()) == 67) {
                    LOGGER.log(Level.FINEST, " <=BE CommandStatus, Ignored until CopyDone");
                    break;
                }
                c = this.pgStream.receiveChar();
                switch (c) {
                    case 65: {
                        LOGGER.log(Level.FINEST, " <=BE Asynchronous Notification while copying");
                        this.receiveAsyncNotify();
                        break;
                    }
                    case 78: {
                        LOGGER.log(Level.FINEST, " <=BE Notification while copying");
                        this.addWarning(this.receiveNoticeResponse());
                        break;
                    }
                    case 67: {
                        String status = this.receiveCommandStatus();
                        try {
                            if (op == null) {
                                throw new PSQLException(GT.tr("Received CommandComplete ''{0}'' without an active copy operation", status), PSQLState.OBJECT_NOT_IN_STATE);
                            }
                            op.handleCommandStatus(status);
                        }
                        catch (SQLException se) {
                            error = se;
                        }
                        block = true;
                        break;
                    }
                    case 69: {
                        error = this.receiveErrorResponse();
                        block = true;
                        break;
                    }
                    case 71: {
                        LOGGER.log(Level.FINEST, " <=BE CopyInResponse");
                        if (op != null) {
                            error = new PSQLException(GT.tr("Got CopyInResponse from server during an active {0}", op.getClass().getName()), PSQLState.OBJECT_NOT_IN_STATE);
                        }
                        op = new CopyInImpl();
                        this.initCopy(op);
                        endReceiving = true;
                        break;
                    }
                    case 72: {
                        LOGGER.log(Level.FINEST, " <=BE CopyOutResponse");
                        if (op != null) {
                            error = new PSQLException(GT.tr("Got CopyOutResponse from server during an active {0}", op.getClass().getName()), PSQLState.OBJECT_NOT_IN_STATE);
                        }
                        op = new CopyOutImpl();
                        this.initCopy(op);
                        endReceiving = true;
                        break;
                    }
                    case 87: {
                        LOGGER.log(Level.FINEST, " <=BE CopyBothResponse");
                        if (op != null) {
                            error = new PSQLException(GT.tr("Got CopyBothResponse from server during an active {0}", op.getClass().getName()), PSQLState.OBJECT_NOT_IN_STATE);
                        }
                        op = new CopyDualImpl();
                        this.initCopy(op);
                        endReceiving = true;
                        break;
                    }
                    case 100: {
                        LOGGER.log(Level.FINEST, " <=BE CopyData");
                        int len = this.pgStream.receiveInteger4() - 4;
                        assert (len > 0) : "Copy Data length must be greater than 4";
                        byte[] buf = this.pgStream.receive(len);
                        if (op == null) {
                            error = new PSQLException(GT.tr("Got CopyData without an active copy operation", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
                        } else if (!(op instanceof CopyOut)) {
                            error = new PSQLException(GT.tr("Unexpected copydata from server for {0}", op.getClass().getName()), PSQLState.COMMUNICATION_ERROR);
                        } else {
                            op.handleCopydata(buf);
                        }
                        endReceiving = true;
                        break;
                    }
                    case 99: {
                        LOGGER.log(Level.FINEST, " <=BE CopyDone");
                        int len = this.pgStream.receiveInteger4() - 4;
                        if (len > 0) {
                            this.pgStream.receive(len);
                        }
                        if (!(op instanceof CopyOut)) {
                            error = new PSQLException("Got CopyDone while not copying from server", PSQLState.OBJECT_NOT_IN_STATE);
                        }
                        block = true;
                        break;
                    }
                    case 83: {
                        try {
                            this.receiveParameterStatus();
                        }
                        catch (SQLException e) {
                            error = e;
                            endReceiving = true;
                        }
                        break;
                    }
                    case 90: {
                        this.receiveRFQ();
                        if (op != null && this.hasLock(op)) {
                            this.unlock(op);
                        }
                        op = null;
                        endReceiving = true;
                        break;
                    }
                    case 84: {
                        LOGGER.log(Level.FINEST, " <=BE RowDescription (during copy ignored)");
                        this.skipMessage();
                        break;
                    }
                    case 68: {
                        LOGGER.log(Level.FINEST, " <=BE DataRow (during copy ignored)");
                        this.skipMessage();
                        break;
                    }
                    default: {
                        throw new IOException(GT.tr("Unexpected packet type during copy: {0}", Integer.toString(c)));
                    }
                }
                if (error == null) continue;
                if (errors != null) {
                    error.setNextException(errors);
                }
                errors = error;
                error = null;
            }
            if (errors != null) {
                throw errors;
            }
            CopyOperationImpl copyOperationImpl = op;
            return copyOperationImpl;
        }
        finally {
            this.processingCopyResults.set(false);
        }
    }

    private void flushIfDeadlockRisk(Query query, boolean disallowBatching, ResultHandler resultHandler, @Nullable BatchResultHandler batchHandler, int flags) throws IOException {
        this.estimatedReceiveBufferBytes += 250;
        SimpleQuery sq = (SimpleQuery)query;
        if (sq.isStatementDescribed()) {
            int maxResultRowSize = sq.getMaxResultRowSize();
            if (maxResultRowSize >= 0) {
                this.estimatedReceiveBufferBytes += maxResultRowSize;
            } else {
                LOGGER.log(Level.FINEST, "Couldn't estimate result size or result size unbounded, disabling batching for this query.");
                disallowBatching = true;
            }
        }
        if (disallowBatching || this.estimatedReceiveBufferBytes >= 64000) {
            LOGGER.log(Level.FINEST, "Forcing Sync, receive buffer full or batching disallowed");
            this.sendSync();
            this.processResults(resultHandler, flags);
            this.estimatedReceiveBufferBytes = 0;
            if (batchHandler != null) {
                batchHandler.secureProgress();
            }
        }
    }

    private void sendQuery(Query query, V3ParameterList parameters, int maxRows, int fetchSize, int flags, ResultHandler resultHandler, @Nullable BatchResultHandler batchHandler, boolean adaptiveFetch) throws IOException, SQLException {
        boolean disallowBatching;
        Query[] subqueries = query.org_postgresql_core_Query_arr_getSubqueries();
        SimpleParameterList[] subparams = parameters.getSubparams();
        boolean bl = disallowBatching = (flags & 0x80) != 0;
        if (subqueries == null) {
            this.flushIfDeadlockRisk(query, disallowBatching, resultHandler, batchHandler, flags);
            if (resultHandler.getException() == null) {
                if (fetchSize != 0) {
                    this.adaptiveFetchCache.addNewQuery(adaptiveFetch, query);
                }
                this.sendOneQuery((SimpleQuery)query, (SimpleParameterList)parameters, maxRows, fetchSize, flags);
            }
        } else {
            for (int i = 0; i < subqueries.length; ++i) {
                Query subquery = subqueries[i];
                this.flushIfDeadlockRisk(subquery, disallowBatching, resultHandler, batchHandler, flags);
                if (resultHandler.getException() != null) break;
                SimpleParameterList subparam = SimpleQuery.NO_PARAMETERS;
                if (subparams != null) {
                    subparam = subparams[i];
                }
                if (fetchSize != 0) {
                    this.adaptiveFetchCache.addNewQuery(adaptiveFetch, subquery);
                }
                this.sendOneQuery((SimpleQuery)subquery, subparam, maxRows, fetchSize, flags);
            }
        }
    }

    private void sendSync() throws IOException {
        LOGGER.log(Level.FINEST, " FE=> Sync");
        this.pgStream.sendChar(83);
        this.pgStream.sendInteger4(4);
        this.pgStream.flush();
        this.pendingExecuteQueue.add(new ExecuteRequest(this.sync, null, true));
        this.pendingDescribePortalQueue.add(this.sync);
    }

    private void sendParse(SimpleQuery query, SimpleParameterList params, boolean oneShot) throws IOException {
        int[] typeOIDs = params.getTypeOIDs();
        if (query.isPreparedFor(typeOIDs, this.deallocateEpoch)) {
            return;
        }
        query.unprepare();
        this.processDeadParsedQueries();
        query.setFields(null);
        String statementName = null;
        if (!oneShot) {
            statementName = "S_" + this.nextUniqueID++;
            query.setStatementName(statementName, this.deallocateEpoch);
            query.setPrepareTypes(typeOIDs);
            this.registerParsedQuery(query, statementName);
        }
        byte[] encodedStatementName = query.getEncodedStatementName();
        String nativeSql = query.getNativeSql();
        if (LOGGER.isLoggable(Level.FINEST)) {
            StringBuilder sbuf = new StringBuilder(" FE=> Parse(stmt=" + statementName + ",query=\"");
            sbuf.append(nativeSql);
            sbuf.append("\",oids={");
            for (int i = 1; i <= params.getParameterCount(); ++i) {
                if (i != 1) {
                    sbuf.append(",");
                }
                sbuf.append(params.getTypeOID(i));
            }
            sbuf.append("})");
            LOGGER.log(Level.FINEST, sbuf.toString());
        }
        byte[] queryUtf8 = nativeSql.getBytes(StandardCharsets.UTF_8);
        int encodedSize = 4 + (encodedStatementName == null ? 0 : encodedStatementName.length) + 1 + queryUtf8.length + 1 + 2 + 4 * params.getParameterCount();
        this.pgStream.sendChar(80);
        this.pgStream.sendInteger4(encodedSize);
        if (encodedStatementName != null) {
            this.pgStream.send(encodedStatementName);
        }
        this.pgStream.sendChar(0);
        this.pgStream.send(queryUtf8);
        this.pgStream.sendChar(0);
        this.pgStream.sendInteger2(params.getParameterCount());
        for (int i = 1; i <= params.getParameterCount(); ++i) {
            this.pgStream.sendInteger4(params.getTypeOID(i));
        }
        this.pendingParseQueue.add(query);
    }

    private void sendBind(SimpleQuery query, SimpleParameterList params, @Nullable Portal portal, boolean noBinaryTransfer) throws IOException {
        int i;
        byte[] encodedPortalName;
        String statementName = query.getStatementName();
        byte[] encodedStatementName = query.getEncodedStatementName();
        byte[] byArray = encodedPortalName = portal == null ? null : portal.getEncodedPortalName();
        if (LOGGER.isLoggable(Level.FINEST)) {
            StringBuilder sbuf = new StringBuilder(" FE=> Bind(stmt=" + statementName + ",portal=" + portal);
            for (int i2 = 1; i2 <= params.getParameterCount(); ++i2) {
                sbuf.append(",$").append(i2).append("=<").append(params.toString(i2, true)).append(">,type=").append(Oid.toString(params.getTypeOID(i2)));
            }
            sbuf.append(")");
            LOGGER.log(Level.FINEST, sbuf.toString());
        }
        long encodedSize = 0L;
        for (int i3 = 1; i3 <= params.getParameterCount(); ++i3) {
            if (params.isNull(i3)) {
                encodedSize += 4L;
                continue;
            }
            encodedSize += 4L + (long)params.getV3Length(i3);
        }
        Field[] fields = query.getFields();
        if (!noBinaryTransfer && query.needUpdateFieldFormats() && fields != null) {
            for (Field field : fields) {
                if (!this.useBinary(field)) continue;
                field.setFormat(1);
                query.setHasBinaryFields(true);
            }
        }
        if (noBinaryTransfer && query.hasBinaryFields() && fields != null) {
            for (Field field : fields) {
                if (field.getFormat() == 0) continue;
                field.setFormat(0);
            }
            query.resetNeedUpdateFieldFormats();
            query.setHasBinaryFields(false);
        }
        int numBinaryFields = !noBinaryTransfer && query.hasBinaryFields() && fields != null ? fields.length : 0;
        encodedSize = (long)(4 + (encodedPortalName == null ? 0 : encodedPortalName.length) + 1 + (encodedStatementName == null ? 0 : encodedStatementName.length) + 1 + 2 + params.getParameterCount() * 2 + 2) + encodedSize + 2L + (long)(numBinaryFields * 2);
        if (encodedSize > 0x3FFFFFFFL) {
            throw new PGBindException(new IOException(GT.tr("Bind message length {0} too long.  This can be caused by very large or incorrect length specifications on InputStream parameters.", encodedSize)));
        }
        this.pgStream.sendChar(66);
        this.pgStream.sendInteger4((int)encodedSize);
        if (encodedPortalName != null) {
            this.pgStream.send(encodedPortalName);
        }
        this.pgStream.sendChar(0);
        if (encodedStatementName != null) {
            this.pgStream.send(encodedStatementName);
        }
        this.pgStream.sendChar(0);
        this.pgStream.sendInteger2(params.getParameterCount());
        for (int i4 = 1; i4 <= params.getParameterCount(); ++i4) {
            this.pgStream.sendInteger2(params.isBinary(i4) ? 1 : 0);
        }
        this.pgStream.sendInteger2(params.getParameterCount());
        PGBindException bindException = null;
        for (i = 1; i <= params.getParameterCount(); ++i) {
            if (params.isNull(i)) {
                this.pgStream.sendInteger4(-1);
                continue;
            }
            this.pgStream.sendInteger4(params.getV3Length(i));
            try {
                params.writeV3Value(i, this.pgStream);
                continue;
            }
            catch (PGBindException be) {
                bindException = be;
            }
        }
        this.pgStream.sendInteger2(numBinaryFields);
        for (i = 0; fields != null && i < numBinaryFields; ++i) {
            this.pgStream.sendInteger2(fields[i].getFormat());
        }
        this.pendingBindQueue.add(portal == null ? UNNAMED_PORTAL : portal);
        if (bindException != null) {
            throw bindException;
        }
    }

    private boolean useBinary(Field field) {
        int oid = field.getOID();
        return this.useBinaryForReceive(oid);
    }

    private void sendDescribePortal(SimpleQuery query, @Nullable Portal portal) throws IOException {
        LOGGER.log(Level.FINEST, " FE=> Describe(portal={0})", portal);
        byte[] encodedPortalName = portal == null ? null : portal.getEncodedPortalName();
        int encodedSize = 5 + (encodedPortalName == null ? 0 : encodedPortalName.length) + 1;
        this.pgStream.sendChar(68);
        this.pgStream.sendInteger4(encodedSize);
        this.pgStream.sendChar(80);
        if (encodedPortalName != null) {
            this.pgStream.send(encodedPortalName);
        }
        this.pgStream.sendChar(0);
        this.pendingDescribePortalQueue.add(query);
        query.setPortalDescribed(true);
    }

    private void sendDescribeStatement(SimpleQuery query, SimpleParameterList params, boolean describeOnly) throws IOException {
        LOGGER.log(Level.FINEST, " FE=> Describe(statement={0})", query.getStatementName());
        byte[] encodedStatementName = query.getEncodedStatementName();
        int encodedSize = 5 + (encodedStatementName == null ? 0 : encodedStatementName.length) + 1;
        this.pgStream.sendChar(68);
        this.pgStream.sendInteger4(encodedSize);
        this.pgStream.sendChar(83);
        if (encodedStatementName != null) {
            this.pgStream.send(encodedStatementName);
        }
        this.pgStream.sendChar(0);
        this.pendingDescribeStatementQueue.add(new DescribeRequest(query, params, describeOnly, query.getStatementName()));
        this.pendingDescribePortalQueue.add(query);
        query.setStatementDescribed(true);
        query.setPortalDescribed(true);
    }

    private void sendExecute(SimpleQuery query, @Nullable Portal portal, int limit) throws IOException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, " FE=> Execute(portal={0},limit={1})", new Object[]{portal, limit});
        }
        byte[] encodedPortalName = portal == null ? null : portal.getEncodedPortalName();
        int encodedSize = encodedPortalName == null ? 0 : encodedPortalName.length;
        this.pgStream.sendChar(69);
        this.pgStream.sendInteger4(5 + encodedSize + 4);
        if (encodedPortalName != null) {
            this.pgStream.send(encodedPortalName);
        }
        this.pgStream.sendChar(0);
        this.pgStream.sendInteger4(limit);
        this.pendingExecuteQueue.add(new ExecuteRequest(query, portal, false));
    }

    private void sendClosePortal(String portalName) throws IOException {
        LOGGER.log(Level.FINEST, " FE=> ClosePortal({0})", portalName);
        byte[] encodedPortalName = portalName == null ? null : portalName.getBytes(StandardCharsets.UTF_8);
        int encodedSize = encodedPortalName == null ? 0 : encodedPortalName.length;
        this.pgStream.sendChar(67);
        this.pgStream.sendInteger4(6 + encodedSize);
        this.pgStream.sendChar(80);
        if (encodedPortalName != null) {
            this.pgStream.send(encodedPortalName);
        }
        this.pgStream.sendChar(0);
    }

    private void sendCloseStatement(String statementName) throws IOException {
        LOGGER.log(Level.FINEST, " FE=> CloseStatement({0})", statementName);
        byte[] encodedStatementName = statementName.getBytes(StandardCharsets.UTF_8);
        this.pgStream.sendChar(67);
        this.pgStream.sendInteger4(5 + encodedStatementName.length + 1);
        this.pgStream.sendChar(83);
        this.pgStream.send(encodedStatementName);
        this.pgStream.sendChar(0);
    }

    private void sendOneQuery(SimpleQuery query, SimpleParameterList params, int maxRows, int fetchSize, int flags) throws IOException {
        boolean describeStatement;
        boolean forceDescribePortal;
        boolean asSimple;
        boolean bl = asSimple = (flags & 0x400) != 0;
        if (asSimple) {
            assert ((flags & 0x20) == 0) : "Simple mode does not support describe requests. sql = " + query.getNativeSql() + ", flags = " + flags;
            this.sendSimpleQuery(query, params);
            return;
        }
        assert (!query.getNativeQuery().multiStatement) : "Queries that might contain ; must be executed with QueryExecutor.QUERY_EXECUTE_AS_SIMPLE mode. Given query is " + query.getNativeSql();
        boolean noResults = (flags & 4) != 0;
        boolean noMeta = (flags & 2) != 0;
        boolean describeOnly = (flags & 0x20) != 0;
        boolean usePortal = (flags & 8) != 0 && !noResults && !noMeta && fetchSize > 0 && !describeOnly;
        boolean oneShot = (flags & 1) != 0;
        boolean noBinaryTransfer = (flags & 0x100) != 0;
        boolean bl2 = forceDescribePortal = (flags & 0x200) != 0;
        int rows = noResults ? 1 : (!usePortal ? maxRows : (maxRows != 0 && fetchSize > maxRows ? maxRows : fetchSize));
        this.sendParse(query, params, oneShot);
        boolean queryHasUnknown = query.hasUnresolvedTypes();
        boolean paramsHasUnknown = params.hasUnresolvedTypes();
        boolean bl3 = describeStatement = describeOnly || !oneShot && paramsHasUnknown && queryHasUnknown && !query.isStatementDescribed();
        if (!describeStatement && paramsHasUnknown && !queryHasUnknown) {
            int[] queryOIDs = Nullness.castNonNull(query.getPrepareTypes());
            int[] paramOIDs = params.getTypeOIDs();
            for (int i = 0; i < paramOIDs.length; ++i) {
                if (paramOIDs[i] != 0) continue;
                params.setResolvedType(i + 1, queryOIDs[i]);
            }
        }
        if (describeStatement) {
            this.sendDescribeStatement(query, params, describeOnly);
            if (describeOnly) {
                return;
            }
        }
        Portal portal = null;
        if (usePortal) {
            String portalName = "C_" + this.nextUniqueID++;
            portal = new Portal(query, portalName);
        }
        this.sendBind(query, params, portal, noBinaryTransfer);
        if (!(noMeta || describeStatement || query.isPortalDescribed() && !forceDescribePortal)) {
            this.sendDescribePortal(query, portal);
        }
        this.sendExecute(query, portal, rows);
    }

    private void sendSimpleQuery(SimpleQuery query, SimpleParameterList params) throws IOException {
        String nativeSql = query.toString(params);
        LOGGER.log(Level.FINEST, " FE=> SimpleQuery(query=\"{0}\")", nativeSql);
        Encoding encoding = this.pgStream.getEncoding();
        byte[] encoded = encoding.encode(nativeSql);
        this.pgStream.sendChar(81);
        this.pgStream.sendInteger4(encoded.length + 4 + 1);
        this.pgStream.send(encoded);
        this.pgStream.sendChar(0);
        this.pgStream.flush();
        this.pendingExecuteQueue.add(new ExecuteRequest(query, null, true));
        this.pendingDescribePortalQueue.add(query);
    }

    private void registerParsedQuery(SimpleQuery query, String statementName) {
        if (statementName == null) {
            return;
        }
        PhantomReference<SimpleQuery> cleanupRef = new PhantomReference<SimpleQuery>(query, this.parsedQueryCleanupQueue);
        this.parsedQueryMap.put(cleanupRef, statementName);
        query.setCleanupRef(cleanupRef);
    }

    private void processDeadParsedQueries() throws IOException {
        Reference<SimpleQuery> deadQuery;
        while ((deadQuery = this.parsedQueryCleanupQueue.poll()) != null) {
            String statementName = Nullness.castNonNull(this.parsedQueryMap.remove(deadQuery));
            this.sendCloseStatement(statementName);
            deadQuery.clear();
        }
    }

    private void registerOpenPortal(Portal portal) {
        if (portal == UNNAMED_PORTAL) {
            return;
        }
        String portalName = portal.getPortalName();
        PhantomReference<Portal> cleanupRef = new PhantomReference<Portal>(portal, this.openPortalCleanupQueue);
        this.openPortalMap.put(cleanupRef, portalName);
        portal.setCleanupRef(cleanupRef);
    }

    private void processDeadPortals() throws IOException {
        Reference<Portal> deadPortal;
        while ((deadPortal = this.openPortalCleanupQueue.poll()) != null) {
            String portalName = Nullness.castNonNull(this.openPortalMap.remove(deadPortal));
            this.sendClosePortal(portalName);
            deadPortal.clear();
        }
    }

    protected void processResults(ResultHandler handler, int flags) throws IOException {
        this.processResults(handler, flags, false);
    }

    protected void processResults(ResultHandler handler, int flags, boolean adaptiveFetch) throws IOException {
        boolean noResults = (flags & 4) != 0;
        boolean bothRowsAndStatus = (flags & 0x40) != 0;
        ArrayList<Tuple> tuples = null;
        boolean endQuery = false;
        boolean doneAfterRowDescNoData = false;
        block26: while (!endQuery) {
            int c = this.pgStream.receiveChar();
            switch (c) {
                case 65: {
                    this.receiveAsyncNotify();
                    continue block26;
                }
                case 49: {
                    this.pgStream.receiveInteger4();
                    SimpleQuery parsedQuery = this.pendingParseQueue.removeFirst();
                    String parsedStatementName = parsedQuery.getStatementName();
                    LOGGER.log(Level.FINEST, " <=BE ParseComplete [{0}]", parsedStatementName);
                    continue block26;
                }
                case 116: {
                    this.pgStream.receiveInteger4();
                    LOGGER.log(Level.FINEST, " <=BE ParameterDescription");
                    DescribeRequest describeData = this.pendingDescribeStatementQueue.getFirst();
                    SimpleQuery query = describeData.query;
                    SimpleParameterList params = describeData.parameterList;
                    boolean describeOnly = describeData.describeOnly;
                    String origStatementName = describeData.statementName;
                    int numParams = this.pgStream.receiveInteger2();
                    for (int i = 1; i <= numParams; ++i) {
                        int typeOid = this.pgStream.receiveInteger4();
                        params.setResolvedType(i, typeOid);
                    }
                    if (origStatementName == null && query.getStatementName() == null || origStatementName != null && origStatementName.equals(query.getStatementName())) {
                        query.setPrepareTypes(params.getTypeOIDs());
                    }
                    if (describeOnly) {
                        doneAfterRowDescNoData = true;
                        continue block26;
                    }
                    this.pendingDescribeStatementQueue.removeFirst();
                    continue block26;
                }
                case 50: {
                    this.pgStream.receiveInteger4();
                    Portal boundPortal = this.pendingBindQueue.removeFirst();
                    LOGGER.log(Level.FINEST, " <=BE BindComplete [{0}]", boundPortal);
                    this.registerOpenPortal(boundPortal);
                    continue block26;
                }
                case 51: {
                    this.pgStream.receiveInteger4();
                    LOGGER.log(Level.FINEST, " <=BE CloseComplete");
                    continue block26;
                }
                case 110: {
                    this.pgStream.receiveInteger4();
                    LOGGER.log(Level.FINEST, " <=BE NoData");
                    this.pendingDescribePortalQueue.removeFirst();
                    if (!doneAfterRowDescNoData) continue block26;
                    DescribeRequest describeData = this.pendingDescribeStatementQueue.removeFirst();
                    SimpleQuery currentQuery = describeData.query;
                    Field[] fields = currentQuery.getFields();
                    if (fields == null) continue block26;
                    tuples = new ArrayList();
                    handler.handleResultRows(currentQuery, fields, tuples, null);
                    tuples = null;
                    continue block26;
                }
                case 115: {
                    this.pgStream.receiveInteger4();
                    LOGGER.log(Level.FINEST, " <=BE PortalSuspended");
                    ExecuteRequest executeData = this.pendingExecuteQueue.removeFirst();
                    SimpleQuery currentQuery = executeData.query;
                    Portal currentPortal = executeData.portal;
                    if (currentPortal != null) {
                        this.adaptiveFetchCache.updateQueryFetchSize(adaptiveFetch, currentQuery, this.pgStream.getMaxRowSizeBytes());
                    }
                    this.pgStream.clearMaxRowSizeBytes();
                    Field[] fields = currentQuery.getFields();
                    if (fields != null && tuples == null) {
                        ArrayList<Tuple> arrayList = tuples = noResults ? Collections.emptyList() : new ArrayList<Tuple>();
                    }
                    if (fields != null && tuples != null) {
                        handler.handleResultRows(currentQuery, fields, tuples, currentPortal);
                    }
                    tuples = null;
                    continue block26;
                }
                case 67: {
                    String nativeSql;
                    String status = this.receiveCommandStatus();
                    if (this.isFlushCacheOnDeallocate() && (status.startsWith("DEALLOCATE ALL") || status.startsWith("DISCARD ALL"))) {
                        this.deallocateEpoch = (short)(this.deallocateEpoch + 1);
                    }
                    doneAfterRowDescNoData = false;
                    ExecuteRequest executeData = Nullness.castNonNull(this.pendingExecuteQueue.peekFirst());
                    SimpleQuery currentQuery = executeData.query;
                    Portal currentPortal = executeData.portal;
                    if (currentPortal != null) {
                        this.adaptiveFetchCache.removeQuery(adaptiveFetch, currentQuery);
                        this.adaptiveFetchCache.updateQueryFetchSize(adaptiveFetch, currentQuery, this.pgStream.getMaxRowSizeBytes());
                    }
                    this.pgStream.clearMaxRowSizeBytes();
                    if (status.startsWith("SET") && (nativeSql = currentQuery.getNativeQuery().nativeSql).lastIndexOf("search_path", 1024) != -1 && !nativeSql.equals(this.lastSetSearchPathQuery)) {
                        this.lastSetSearchPathQuery = nativeSql;
                        this.deallocateEpoch = (short)(this.deallocateEpoch + 1);
                    }
                    if (!executeData.asSimple) {
                        this.pendingExecuteQueue.removeFirst();
                    }
                    if (currentQuery == this.autoSaveQuery || currentQuery == this.releaseAutoSave) continue block26;
                    Field[] fields = currentQuery.getFields();
                    if (fields != null && tuples == null) {
                        ArrayList<Tuple> arrayList = tuples = noResults ? Collections.emptyList() : new ArrayList<Tuple>();
                    }
                    if (fields == null && tuples != null) {
                        throw new IllegalStateException("Received resultset tuples, but no field structure for them");
                    }
                    if (fields != null && tuples != null) {
                        handler.handleResultRows(currentQuery, fields, tuples, null);
                        tuples = null;
                        if (bothRowsAndStatus) {
                            this.interpretCommandStatus(status, handler);
                        }
                    } else {
                        this.interpretCommandStatus(status, handler);
                    }
                    if (executeData.asSimple) {
                        currentQuery.setFields(null);
                    }
                    if (currentPortal == null) continue block26;
                    currentPortal.close();
                    continue block26;
                }
                case 68: {
                    Tuple tuple = null;
                    try {
                        tuple = this.pgStream.receiveTupleV3();
                    }
                    catch (OutOfMemoryError oome) {
                        if (!noResults) {
                            handler.handleError(new PSQLException(GT.tr("Ran out of memory retrieving query results.", new Object[0]), PSQLState.OUT_OF_MEMORY, (Throwable)oome));
                        }
                    }
                    catch (SQLException e) {
                        handler.handleError(e);
                    }
                    if (!noResults) {
                        if (tuples == null) {
                            tuples = new ArrayList();
                        }
                        if (tuple != null) {
                            tuples.add(tuple);
                        }
                    }
                    if (!LOGGER.isLoggable(Level.FINEST)) continue block26;
                    int length = tuple == null ? -1 : tuple.length();
                    LOGGER.log(Level.FINEST, " <=BE DataRow(len={0})", length);
                    continue block26;
                }
                case 69: {
                    SQLException error = this.receiveErrorResponse();
                    handler.handleError(error);
                    if (!this.willHealViaReparse(error)) continue block26;
                    this.deallocateEpoch = (short)(this.deallocateEpoch + 1);
                    if (!LOGGER.isLoggable(Level.FINEST)) continue block26;
                    LOGGER.log(Level.FINEST, " FE: received {0}, will invalidate statements. deallocateEpoch is now {1}", new Object[]{error.getSQLState(), this.deallocateEpoch});
                    continue block26;
                }
                case 73: {
                    this.pgStream.receiveInteger4();
                    LOGGER.log(Level.FINEST, " <=BE EmptyQuery");
                    ExecuteRequest executeData = this.pendingExecuteQueue.removeFirst();
                    Portal currentPortal = executeData.portal;
                    handler.handleCommandStatus("EMPTY", 0L, 0L);
                    if (currentPortal == null) continue block26;
                    currentPortal.close();
                    continue block26;
                }
                case 78: {
                    SQLWarning warning = this.receiveNoticeResponse();
                    handler.handleWarning(warning);
                    continue block26;
                }
                case 83: {
                    try {
                        this.receiveParameterStatus();
                    }
                    catch (SQLException e) {
                        handler.handleError(e);
                        endQuery = true;
                    }
                    continue block26;
                }
                case 84: {
                    Field[] fields = this.receiveFields();
                    tuples = new ArrayList<Tuple>();
                    SimpleQuery query = Nullness.castNonNull(this.pendingDescribePortalQueue.peekFirst());
                    if (!this.pendingExecuteQueue.isEmpty() && !Nullness.castNonNull(this.pendingExecuteQueue.peekFirst()).asSimple) {
                        this.pendingDescribePortalQueue.removeFirst();
                    }
                    query.setFields(fields);
                    if (!doneAfterRowDescNoData) continue block26;
                    DescribeRequest describeData = this.pendingDescribeStatementQueue.removeFirst();
                    SimpleQuery currentQuery = describeData.query;
                    currentQuery.setFields(fields);
                    handler.handleResultRows(currentQuery, fields, tuples, null);
                    tuples = null;
                    continue block26;
                }
                case 90: {
                    this.receiveRFQ();
                    if (!this.pendingExecuteQueue.isEmpty() && Nullness.castNonNull(this.pendingExecuteQueue.peekFirst()).asSimple) {
                        tuples = null;
                        this.pgStream.clearResultBufferCount();
                        ExecuteRequest executeRequest = this.pendingExecuteQueue.removeFirst();
                        executeRequest.query.setFields(null);
                        this.pendingDescribePortalQueue.removeFirst();
                        if (!this.pendingExecuteQueue.isEmpty()) {
                            if (this.getTransactionState() != TransactionState.IDLE) continue block26;
                            handler.secureProgress();
                            continue block26;
                        }
                    }
                    endQuery = true;
                    while (!this.pendingParseQueue.isEmpty()) {
                        SimpleQuery failedQuery = this.pendingParseQueue.removeFirst();
                        failedQuery.unprepare();
                    }
                    this.pendingParseQueue.clear();
                    while (!this.pendingDescribeStatementQueue.isEmpty()) {
                        DescribeRequest request = this.pendingDescribeStatementQueue.removeFirst();
                        LOGGER.log(Level.FINEST, " FE marking setStatementDescribed(false) for query {0}", request.query);
                        request.query.setStatementDescribed(false);
                    }
                    while (!this.pendingDescribePortalQueue.isEmpty()) {
                        SimpleQuery describePortalQuery = this.pendingDescribePortalQueue.removeFirst();
                        LOGGER.log(Level.FINEST, " FE marking setPortalDescribed(false) for query {0}", describePortalQuery);
                        describePortalQuery.setPortalDescribed(false);
                    }
                    this.pendingBindQueue.clear();
                    this.pendingExecuteQueue.clear();
                    continue block26;
                }
                case 71: {
                    LOGGER.log(Level.FINEST, " <=BE CopyInResponse");
                    LOGGER.log(Level.FINEST, " FE=> CopyFail");
                    byte[] buf = "COPY commands are only supported using the CopyManager API.".getBytes(StandardCharsets.US_ASCII);
                    this.pgStream.sendChar(102);
                    this.pgStream.sendInteger4(buf.length + 4 + 1);
                    this.pgStream.send(buf);
                    this.pgStream.sendChar(0);
                    this.pgStream.flush();
                    this.sendSync();
                    this.skipMessage();
                    continue block26;
                }
                case 72: {
                    LOGGER.log(Level.FINEST, " <=BE CopyOutResponse");
                    this.skipMessage();
                    handler.handleError(new PSQLException(GT.tr("COPY commands are only supported using the CopyManager API.", new Object[0]), PSQLState.NOT_IMPLEMENTED));
                    continue block26;
                }
                case 99: {
                    this.skipMessage();
                    LOGGER.log(Level.FINEST, " <=BE CopyDone");
                    continue block26;
                }
                case 100: {
                    this.skipMessage();
                    LOGGER.log(Level.FINEST, " <=BE CopyData");
                    continue block26;
                }
            }
            throw new IOException("Unexpected packet type: " + c);
        }
    }

    private void skipMessage() throws IOException {
        int len = this.pgStream.receiveInteger4();
        assert (len >= 4) : "Length from skip message must be at least 4 ";
        this.pgStream.skip(len - 4);
    }

    @Override
    public synchronized void fetch(ResultCursor cursor, ResultHandler handler, int fetchSize, boolean adaptiveFetch) throws SQLException {
        this.waitOnLock();
        Portal portal = (Portal)cursor;
        ResultHandler delegateHandler = handler;
        final SimpleQuery query = Nullness.castNonNull(portal.getQuery());
        handler = new ResultHandlerDelegate(delegateHandler){

            @Override
            public void handleCommandStatus(String status, long updateCount, long insertOID) {
                this.handleResultRows(query, NO_FIELDS, new ArrayList<Tuple>(), null);
            }
        };
        try {
            this.processDeadParsedQueries();
            this.processDeadPortals();
            this.sendExecute(query, portal, fetchSize);
            this.sendSync();
            this.processResults(handler, 0, adaptiveFetch);
            this.estimatedReceiveBufferBytes = 0;
        }
        catch (IOException e) {
            this.abort();
            handler.handleError(new PSQLException(GT.tr("An I/O error occurred while sending to the backend.", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)e));
        }
        handler.handleCompletion();
    }

    @Override
    public int getAdaptiveFetchSize(boolean adaptiveFetch, ResultCursor cursor) {
        SimpleQuery query;
        if (cursor instanceof Portal && Objects.nonNull(query = ((Portal)cursor).getQuery())) {
            return this.adaptiveFetchCache.getFetchSizeForQuery(adaptiveFetch, query);
        }
        return -1;
    }

    @Override
    public void setAdaptiveFetch(boolean adaptiveFetch) {
        this.adaptiveFetchCache.setAdaptiveFetch(adaptiveFetch);
    }

    @Override
    public boolean getAdaptiveFetch() {
        return this.adaptiveFetchCache.getAdaptiveFetch();
    }

    @Override
    public void addQueryToAdaptiveFetchCache(boolean adaptiveFetch, @NonNull ResultCursor cursor) {
        SimpleQuery query;
        if (cursor instanceof Portal && Objects.nonNull(query = ((Portal)cursor).getQuery())) {
            this.adaptiveFetchCache.addNewQuery(adaptiveFetch, query);
        }
    }

    @Override
    public void removeQueryFromAdaptiveFetchCache(boolean adaptiveFetch, @NonNull ResultCursor cursor) {
        SimpleQuery query;
        if (cursor instanceof Portal && Objects.nonNull(query = ((Portal)cursor).getQuery())) {
            this.adaptiveFetchCache.removeQuery(adaptiveFetch, query);
        }
    }

    private Field[] receiveFields() throws IOException {
        this.pgStream.receiveInteger4();
        int size = this.pgStream.receiveInteger2();
        Field[] fields = new Field[size];
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, " <=BE RowDescription({0})", size);
        }
        for (int i = 0; i < fields.length; ++i) {
            String columnLabel = this.pgStream.receiveCanonicalString();
            int tableOid = this.pgStream.receiveInteger4();
            short positionInTable = (short)this.pgStream.receiveInteger2();
            int typeOid = this.pgStream.receiveInteger4();
            int typeLength = this.pgStream.receiveInteger2();
            int typeModifier = this.pgStream.receiveInteger4();
            int formatType = this.pgStream.receiveInteger2();
            fields[i] = new Field(columnLabel, typeOid, typeLength, typeModifier, tableOid, positionInTable);
            fields[i].setFormat(formatType);
            LOGGER.log(Level.FINEST, "        {0}", fields[i]);
        }
        return fields;
    }

    private void receiveAsyncNotify() throws IOException {
        int len = this.pgStream.receiveInteger4();
        assert (len > 4) : "Length for AsyncNotify must be at least 4";
        int pid = this.pgStream.receiveInteger4();
        String msg = this.pgStream.receiveCanonicalString();
        String param = this.pgStream.receiveString();
        this.addNotification(new Notification(msg, pid, param));
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, " <=BE AsyncNotify({0},{1},{2})", new Object[]{pid, msg, param});
        }
    }

    private SQLException receiveErrorResponse() throws IOException {
        int elen = this.pgStream.receiveInteger4();
        assert (elen > 4) : "Error response length must be greater than 4";
        EncodingPredictor.DecodeResult totalMessage = this.pgStream.receiveErrorString(elen - 4);
        ServerErrorMessage errorMsg = new ServerErrorMessage(totalMessage);
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, " <=BE ErrorMessage({0})", errorMsg.toString());
        }
        PSQLException error = new PSQLException(errorMsg, this.logServerErrorDetail);
        if (this.transactionFailCause == null) {
            this.transactionFailCause = error;
        } else {
            error.initCause(this.transactionFailCause);
        }
        return error;
    }

    private SQLWarning receiveNoticeResponse() throws IOException {
        int nlen = this.pgStream.receiveInteger4();
        assert (nlen > 4) : "Notice Response length must be greater than 4";
        ServerErrorMessage warnMsg = new ServerErrorMessage(this.pgStream.receiveString(nlen - 4));
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, " <=BE NoticeResponse({0})", warnMsg.toString());
        }
        return new PSQLWarning(warnMsg);
    }

    private String receiveCommandStatus() throws IOException {
        int len = this.pgStream.receiveInteger4();
        String status = this.pgStream.receiveString(len - 5);
        this.pgStream.receiveChar();
        LOGGER.log(Level.FINEST, " <=BE CommandStatus({0})", status);
        return status;
    }

    private void interpretCommandStatus(String status, ResultHandler handler) {
        try {
            this.commandCompleteParser.parse(status);
        }
        catch (SQLException e) {
            handler.handleError(e);
            return;
        }
        long oid = this.commandCompleteParser.getOid();
        long count = this.commandCompleteParser.getRows();
        handler.handleCommandStatus(status, count, oid);
    }

    private void receiveRFQ() throws IOException {
        if (this.pgStream.receiveInteger4() != 5) {
            throw new IOException("unexpected length of ReadyForQuery message");
        }
        char tStatus = (char)this.pgStream.receiveChar();
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, " <=BE ReadyForQuery({0})", Character.valueOf(tStatus));
        }
        switch (tStatus) {
            case 'I': {
                this.transactionFailCause = null;
                this.setTransactionState(TransactionState.IDLE);
                break;
            }
            case 'T': {
                this.transactionFailCause = null;
                this.setTransactionState(TransactionState.OPEN);
                break;
            }
            case 'E': {
                this.setTransactionState(TransactionState.FAILED);
                break;
            }
            default: {
                throw new IOException("unexpected transaction state in ReadyForQuery message: " + tStatus);
            }
        }
    }

    @Override
    protected void sendCloseMessage() throws IOException {
        this.pgStream.sendChar(88);
        this.pgStream.sendInteger4(4);
    }

    public void readStartupMessages() throws IOException, SQLException {
        block7: for (int i = 0; i < 1000; ++i) {
            int beresp = this.pgStream.receiveChar();
            switch (beresp) {
                case 90: {
                    this.receiveRFQ();
                    return;
                }
                case 75: {
                    int msgLen = this.pgStream.receiveInteger4();
                    if (msgLen != 12) {
                        throw new PSQLException(GT.tr("Protocol error.  Session setup failed.", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
                    }
                    int pid = this.pgStream.receiveInteger4();
                    int ckey = this.pgStream.receiveInteger4();
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, " <=BE BackendKeyData(pid={0},ckey={1})", new Object[]{pid, ckey});
                    }
                    this.setBackendKeyData(pid, ckey);
                    continue block7;
                }
                case 69: {
                    throw this.receiveErrorResponse();
                }
                case 78: {
                    this.addWarning(this.receiveNoticeResponse());
                    continue block7;
                }
                case 83: {
                    this.receiveParameterStatus();
                    continue block7;
                }
                default: {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "  invalid message type={0}", Character.valueOf((char)beresp));
                    }
                    throw new PSQLException(GT.tr("Protocol error.  Session setup failed.", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
                }
            }
        }
        throw new PSQLException(GT.tr("Protocol error.  Session setup failed.", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
    }

    public void receiveParameterStatus() throws IOException, SQLException {
        this.pgStream.receiveInteger4();
        String name = this.pgStream.receiveCanonicalStringIfPresent();
        String value = this.pgStream.receiveCanonicalStringIfPresent();
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, " <=BE ParameterStatus({0} = {1})", new Object[]{name, value});
        }
        if (name.isEmpty()) {
            return;
        }
        this.onParameterStatus(name, value);
        if (name.equals("client_encoding")) {
            if (this.allowEncodingChanges) {
                if (!value.equalsIgnoreCase("UTF8") && !value.equalsIgnoreCase("UTF-8")) {
                    LOGGER.log(Level.FINE, "pgjdbc expects client_encoding to be UTF8 for proper operation. Actual encoding is {0}", value);
                }
                this.pgStream.setEncoding(Encoding.getDatabaseEncoding(value));
            } else if (!value.equalsIgnoreCase("UTF8") && !value.equalsIgnoreCase("UTF-8")) {
                this.close();
                throw new PSQLException(GT.tr("The server''s client_encoding parameter was changed to {0}. The JDBC driver requires client_encoding to be UTF8 for correct operation.", value), PSQLState.CONNECTION_FAILURE);
            }
        }
        if (name.equals("DateStyle") && !value.startsWith("ISO") && !value.toUpperCase(Locale.ROOT).startsWith("ISO")) {
            this.close();
            throw new PSQLException(GT.tr("The server''s DateStyle parameter was changed to {0}. The JDBC driver requires DateStyle to begin with ISO for correct operation.", value), PSQLState.CONNECTION_FAILURE);
        }
        if (name.equals("standard_conforming_strings")) {
            if (value.equals("on")) {
                this.setStandardConformingStrings(true);
            } else if (value.equals("off")) {
                this.setStandardConformingStrings(false);
            } else {
                this.close();
                throw new PSQLException(GT.tr("The server''s standard_conforming_strings parameter was reported as {0}. The JDBC driver expected on or off.", value), PSQLState.CONNECTION_FAILURE);
            }
            return;
        }
        if ("TimeZone".equals(name)) {
            this.setTimeZone(TimestampUtils.parseBackendTimeZone(value));
        } else if ("application_name".equals(name)) {
            this.setApplicationName(value);
        } else if ("server_version_num".equals(name)) {
            this.setServerVersionNum(Integer.parseInt(value));
        } else if ("server_version".equals(name)) {
            this.setServerVersion(value);
        } else if ("integer_datetimes".equals(name)) {
            if ("on".equals(value)) {
                this.setIntegerDateTimes(true);
            } else if ("off".equals(value)) {
                this.setIntegerDateTimes(false);
            } else {
                throw new PSQLException(GT.tr("Protocol error.  Session setup failed.", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
            }
        }
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public @Nullable TimeZone getTimeZone() {
        return this.timeZone;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public String getApplicationName() {
        if (this.applicationName == null) {
            return "";
        }
        return this.applicationName;
    }

    @Override
    public ReplicationProtocol getReplicationProtocol() {
        return this.replicationProtocol;
    }

    @Override
    public boolean useBinaryForReceive(int oid) {
        return this.useBinaryReceiveForOids.contains(oid);
    }

    @Override
    public void setBinaryReceiveOids(Set<Integer> oids) {
        this.useBinaryReceiveForOids.clear();
        this.useBinaryReceiveForOids.addAll(oids);
    }

    @Override
    public boolean useBinaryForSend(int oid) {
        return this.useBinarySendForOids.contains(oid);
    }

    @Override
    public void setBinarySendOids(Set<Integer> oids) {
        this.useBinarySendForOids.clear();
        this.useBinarySendForOids.addAll(oids);
    }

    private void setIntegerDateTimes(boolean state) {
        this.integerDateTimes = state;
    }

    @Override
    public boolean getIntegerDateTimes() {
        return this.integerDateTimes;
    }

    static {
        Encoding.canonicalize("application_name");
        Encoding.canonicalize("client_encoding");
        Encoding.canonicalize("DateStyle");
        Encoding.canonicalize("integer_datetimes");
        Encoding.canonicalize("off");
        Encoding.canonicalize("on");
        Encoding.canonicalize("server_encoding");
        Encoding.canonicalize("server_version");
        Encoding.canonicalize("server_version_num");
        Encoding.canonicalize("standard_conforming_strings");
        Encoding.canonicalize("TimeZone");
        Encoding.canonicalize("UTF8");
        Encoding.canonicalize("UTF-8");
        Encoding.canonicalize("in_hot_standby");
        UNNAMED_PORTAL = new Portal(null, "unnamed");
    }
}

