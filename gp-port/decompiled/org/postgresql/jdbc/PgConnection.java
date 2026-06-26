/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.security.Permission;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.ClientInfoStatus;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLPermission;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.dataflow.qual.Pure;
import org.postgresql.Driver;
import org.postgresql.PGNotification;
import org.postgresql.PGProperty;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.BaseStatement;
import org.postgresql.core.CachedQuery;
import org.postgresql.core.ConnectionFactory;
import org.postgresql.core.Encoding;
import org.postgresql.core.Oid;
import org.postgresql.core.Query;
import org.postgresql.core.QueryExecutor;
import org.postgresql.core.ReplicationProtocol;
import org.postgresql.core.ResultHandlerBase;
import org.postgresql.core.ServerVersion;
import org.postgresql.core.SqlCommand;
import org.postgresql.core.TransactionState;
import org.postgresql.core.TypeInfo;
import org.postgresql.core.Utils;
import org.postgresql.core.Version;
import org.postgresql.fastpath.Fastpath;
import org.postgresql.geometric.PGbox;
import org.postgresql.geometric.PGcircle;
import org.postgresql.geometric.PGline;
import org.postgresql.geometric.PGlseg;
import org.postgresql.geometric.PGpath;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpolygon;
import org.postgresql.jdbc.ArrayEncoding;
import org.postgresql.jdbc.AutoSave;
import org.postgresql.jdbc.FieldMetadata;
import org.postgresql.jdbc.PSQLSavepoint;
import org.postgresql.jdbc.PgArray;
import org.postgresql.jdbc.PgBlob;
import org.postgresql.jdbc.PgCallableStatement;
import org.postgresql.jdbc.PgClob;
import org.postgresql.jdbc.PgDatabaseMetaData;
import org.postgresql.jdbc.PgPreparedStatement;
import org.postgresql.jdbc.PgSQLXML;
import org.postgresql.jdbc.PgStatement;
import org.postgresql.jdbc.PreferQueryMode;
import org.postgresql.jdbc.QueryExecutorTimeZoneProvider;
import org.postgresql.jdbc.TimestampUtils;
import org.postgresql.jdbc.TypeInfoCache;
import org.postgresql.largeobject.LargeObjectManager;
import org.postgresql.replication.PGReplicationConnection;
import org.postgresql.replication.PGReplicationConnectionImpl;
import org.postgresql.util.GT;
import org.postgresql.util.HostSpec;
import org.postgresql.util.LruCache;
import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGInterval;
import org.postgresql.util.PGmoney;
import org.postgresql.util.PGobject;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;
import org.postgresql.xml.DefaultPGXmlFactoryFactory;
import org.postgresql.xml.LegacyInsecurePGXmlFactoryFactory;
import org.postgresql.xml.PGXmlFactoryFactory;

public class PgConnection
implements BaseConnection {
    private static final Logger LOGGER = Logger.getLogger(PgConnection.class.getName());
    private static final Set<Integer> SUPPORTED_BINARY_OIDS = PgConnection.getSupportedBinaryOids();
    private static final SQLPermission SQL_PERMISSION_ABORT = new SQLPermission("callAbort");
    private static final SQLPermission SQL_PERMISSION_NETWORK_TIMEOUT = new SQLPermission("setNetworkTimeout");
    private static final @Nullable MethodHandle SYSTEM_GET_SECURITY_MANAGER;
    private static final @Nullable MethodHandle SECURITY_MANAGER_CHECK_PERMISSION;
    private final Properties clientInfo;
    private final String creatingURL;
    private final ReadOnlyBehavior readOnlyBehavior;
    private @Nullable Throwable openStackTrace;
    private final QueryExecutor queryExecutor;
    private final Query commitQuery;
    private final Query rollbackQuery;
    private final CachedQuery setSessionReadOnly;
    private final CachedQuery setSessionNotReadOnly;
    private final TypeInfo typeCache;
    private boolean disableColumnSanitiser = false;
    protected int prepareThreshold;
    protected int defaultFetchSize;
    protected boolean forcebinary = false;
    private int rsHoldability = 2;
    private int savepointId = 0;
    private boolean autoCommit = true;
    private boolean readOnly = false;
    private boolean hideUnprivilegedObjects;
    private final boolean logServerErrorDetail;
    private final boolean bindStringAsVarchar;
    private @Nullable SQLWarning firstWarning;
    private volatile @Nullable Timer cancelTimer;
    private @Nullable PreparedStatement checkConnectionQuery;
    private final boolean replicationConnection;
    private final LruCache<FieldMetadata.Key, FieldMetadata> fieldMetadataCache;
    private final @Nullable String xmlFactoryFactoryClass;
    private @Nullable PGXmlFactoryFactory xmlFactoryFactory;
    private final TimestampUtils timestampUtils;
    protected Map<String, Class<?>> typemap = new HashMap();
    private @Nullable Fastpath fastpath;
    private @Nullable LargeObjectManager largeobject;
    protected @Nullable DatabaseMetaData metadata;
    private @Nullable CopyManager copyManager;

    final CachedQuery borrowQuery(String sql) throws SQLException {
        return this.queryExecutor.borrowQuery(sql);
    }

    final CachedQuery borrowCallableQuery(String sql) throws SQLException {
        return this.queryExecutor.borrowCallableQuery(sql);
    }

    private CachedQuery borrowReturningQuery(String sql, String @Nullable [] columnNames) throws SQLException {
        return this.queryExecutor.borrowReturningQuery(sql, columnNames);
    }

    @Override
    public CachedQuery createQuery(String sql, boolean escapeProcessing, boolean isParameterized, String ... columnNames) throws SQLException {
        return this.queryExecutor.createQuery(sql, escapeProcessing, isParameterized, columnNames);
    }

    void releaseQuery(CachedQuery cachedQuery) {
        this.queryExecutor.releaseQuery(cachedQuery);
    }

    @Override
    public void setFlushCacheOnDeallocate(boolean flushCacheOnDeallocate) {
        this.queryExecutor.setFlushCacheOnDeallocate(flushCacheOnDeallocate);
        LOGGER.log(Level.FINE, "  setFlushCacheOnDeallocate = {0}", flushCacheOnDeallocate);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public PgConnection(HostSpec[] hostSpecs, Properties info, String url) throws SQLException {
        String stringType;
        LOGGER.log(Level.FINE, "PostgreSQL JDBC Driver 42.4.3");
        this.creatingURL = url;
        this.readOnlyBehavior = PgConnection.getReadOnlyBehavior(PGProperty.READ_ONLY_MODE.get(info));
        this.setDefaultFetchSize(PGProperty.DEFAULT_ROW_FETCH_SIZE.getInt(info));
        this.setPrepareThreshold(PGProperty.PREPARE_THRESHOLD.getInt(info));
        if (this.prepareThreshold == -1) {
            this.setForceBinary(true);
        }
        this.queryExecutor = ConnectionFactory.openConnection(hostSpecs, info);
        if (LOGGER.isLoggable(Level.WARNING) && !this.haveMinimumServerVersion(ServerVersion.v8_2)) {
            LOGGER.log(Level.WARNING, "Unsupported Server Version: {0}", this.queryExecutor.getServerVersion());
        }
        this.setSessionReadOnly = this.createQuery("SET SESSION CHARACTERISTICS AS TRANSACTION READ ONLY", false, true, new String[0]);
        this.setSessionNotReadOnly = this.createQuery("SET SESSION CHARACTERISTICS AS TRANSACTION READ WRITE", false, true, new String[0]);
        if (PGProperty.READ_ONLY.getBoolean(info)) {
            this.setReadOnly(true);
        }
        this.hideUnprivilegedObjects = PGProperty.HIDE_UNPRIVILEGED_OBJECTS.getBoolean(info);
        Set<Integer> binaryOids = PgConnection.getBinaryOids(info);
        HashSet<Integer> useBinarySendForOids = new HashSet<Integer>(binaryOids);
        HashSet<Integer> useBinaryReceiveForOids = new HashSet<Integer>(binaryOids);
        useBinarySendForOids.remove(1082);
        this.queryExecutor.setBinaryReceiveOids(useBinaryReceiveForOids);
        this.queryExecutor.setBinarySendOids(useBinarySendForOids);
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "    types using binary send = {0}", this.oidsToString(useBinarySendForOids));
            LOGGER.log(Level.FINEST, "    types using binary receive = {0}", this.oidsToString(useBinaryReceiveForOids));
            LOGGER.log(Level.FINEST, "    integer date/time = {0}", this.queryExecutor.getIntegerDateTimes());
        }
        if ((stringType = PGProperty.STRING_TYPE.get(info)) != null) {
            if (stringType.equalsIgnoreCase("unspecified")) {
                this.bindStringAsVarchar = false;
            } else {
                if (!stringType.equalsIgnoreCase("varchar")) throw new PSQLException(GT.tr("Unsupported value for stringtype parameter: {0}", stringType), PSQLState.INVALID_PARAMETER_VALUE);
                this.bindStringAsVarchar = true;
            }
        } else {
            this.bindStringAsVarchar = true;
        }
        this.timestampUtils = new TimestampUtils(!this.queryExecutor.getIntegerDateTimes(), new QueryExecutorTimeZoneProvider(this.queryExecutor));
        this.commitQuery = this.createQuery((String)"COMMIT", (boolean)false, (boolean)true, (String[])new String[0]).query;
        this.rollbackQuery = this.createQuery((String)"ROLLBACK", (boolean)false, (boolean)true, (String[])new String[0]).query;
        int unknownLength = PGProperty.UNKNOWN_LENGTH.getInt(info);
        this.typeCache = this.createTypeInfo(this, unknownLength);
        this.initObjectTypes(info);
        if (PGProperty.LOG_UNCLOSED_CONNECTIONS.getBoolean(info)) {
            this.openStackTrace = new Throwable("Connection was created at this point:");
        }
        this.logServerErrorDetail = PGProperty.LOG_SERVER_ERROR_DETAIL.getBoolean(info);
        this.disableColumnSanitiser = PGProperty.DISABLE_COLUMN_SANITISER.getBoolean(info);
        if (this.haveMinimumServerVersion(ServerVersion.v8_3)) {
            this.typeCache.addCoreType("uuid", 2950, 1111, "java.util.UUID", 2951);
            this.typeCache.addCoreType("xml", 142, 2009, "java.sql.SQLXML", 143);
        }
        this.clientInfo = new Properties();
        if (this.haveMinimumServerVersion(ServerVersion.v9_0)) {
            String appName = PGProperty.APPLICATION_NAME.get(info);
            if (appName == null) {
                appName = "";
            }
            this.clientInfo.put("ApplicationName", appName);
        }
        this.fieldMetadataCache = new LruCache(Math.max(0, PGProperty.DATABASE_METADATA_CACHE_FIELDS.getInt(info)), Math.max(0L, (long)PGProperty.DATABASE_METADATA_CACHE_FIELDS_MIB.getInt(info) * 1024L * 1024L), false);
        this.replicationConnection = PGProperty.REPLICATION.get(info) != null;
        this.xmlFactoryFactoryClass = PGProperty.XML_FACTORY_FACTORY.get(info);
    }

    private static ReadOnlyBehavior getReadOnlyBehavior(String property) {
        try {
            return ReadOnlyBehavior.valueOf(property);
        }
        catch (IllegalArgumentException e) {
            try {
                return ReadOnlyBehavior.valueOf(property.toLowerCase(Locale.US));
            }
            catch (IllegalArgumentException e2) {
                return ReadOnlyBehavior.transaction;
            }
        }
    }

    private static Set<Integer> getSupportedBinaryOids() {
        return new HashSet<Integer>(Arrays.asList(17, 21, 23, 20, 700, 701, 1700, 1083, 1082, 1266, 1114, 1184, 1001, 1005, 1007, 1016, 1028, 1021, 1022, 1015, 1009, 600, 603, 2950));
    }

    private static Set<Integer> getBinaryOids(Properties info) throws PSQLException {
        String oids;
        boolean binaryTransfer = PGProperty.BINARY_TRANSFER.getBoolean(info);
        HashSet<Integer> binaryOids = new HashSet<Integer>(32);
        if (binaryTransfer) {
            binaryOids.addAll(SUPPORTED_BINARY_OIDS);
        }
        if ((oids = PGProperty.BINARY_TRANSFER_ENABLE.get(info)) != null) {
            binaryOids.addAll(PgConnection.getOidSet(oids));
        }
        if ((oids = PGProperty.BINARY_TRANSFER_DISABLE.get(info)) != null) {
            binaryOids.removeAll(PgConnection.getOidSet(oids));
        }
        return binaryOids;
    }

    private static Set<Integer> getOidSet(String oidList) throws PSQLException {
        HashSet<Integer> oids = new HashSet<Integer>();
        StringTokenizer tokenizer = new StringTokenizer(oidList, ",");
        while (tokenizer.hasMoreTokens()) {
            String oid = tokenizer.nextToken();
            oids.add(Oid.valueOf(oid));
        }
        return oids;
    }

    private String oidsToString(Set<Integer> oids) {
        StringBuilder sb = new StringBuilder();
        for (Integer oid : oids) {
            sb.append(Oid.toString(oid));
            sb.append(',');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        } else {
            sb.append(" <none>");
        }
        return sb.toString();
    }

    @Override
    public TimestampUtils getTimestampUtils() {
        return this.timestampUtils;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return this.createStatement(1003, 1007);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return this.prepareStatement(sql, 1003, 1007);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return this.prepareCall(sql, 1003, 1007);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        this.checkClosed();
        return this.typemap;
    }

    @Override
    public QueryExecutor getQueryExecutor() {
        return this.queryExecutor;
    }

    @Override
    public ReplicationProtocol getReplicationProtocol() {
        return this.queryExecutor.getReplicationProtocol();
    }

    public void addWarning(SQLWarning warn) {
        if (this.firstWarning != null) {
            this.firstWarning.setNextWarning(warn);
        } else {
            this.firstWarning = warn;
        }
    }

    @Override
    public ResultSet execSQLQuery(String s2) throws SQLException {
        return this.execSQLQuery(s2, 1003, 1007);
    }

    @Override
    public ResultSet execSQLQuery(String s2, int resultSetType, int resultSetConcurrency) throws SQLException {
        BaseStatement stat = (BaseStatement)this.createStatement(resultSetType, resultSetConcurrency);
        boolean hasResultSet = stat.executeWithFlags(s2, 16);
        while (!hasResultSet && stat.getUpdateCount() != -1) {
            hasResultSet = stat.getMoreResults();
        }
        if (!hasResultSet) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        SQLWarning warnings = stat.getWarnings();
        if (warnings != null) {
            this.addWarning(warnings);
        }
        return Nullness.castNonNull(stat.getResultSet(), "hasResultSet==true, yet getResultSet()==null");
    }

    @Override
    public void execSQLUpdate(String s2) throws SQLException {
        BaseStatement stmt = (BaseStatement)this.createStatement();
        if (stmt.executeWithFlags(s2, 22)) {
            throw new PSQLException(GT.tr("A result was returned when none was expected.", new Object[0]), PSQLState.TOO_MANY_RESULTS);
        }
        SQLWarning warnings = stmt.getWarnings();
        if (warnings != null) {
            this.addWarning(warnings);
        }
        stmt.close();
    }

    void execSQLUpdate(CachedQuery query) throws SQLException {
        BaseStatement stmt = (BaseStatement)this.createStatement();
        if (stmt.executeWithFlags(query, 22)) {
            throw new PSQLException(GT.tr("A result was returned when none was expected.", new Object[0]), PSQLState.TOO_MANY_RESULTS);
        }
        SQLWarning warnings = stmt.getWarnings();
        if (warnings != null) {
            this.addWarning(warnings);
        }
        stmt.close();
    }

    public void setCursorName(String cursor) throws SQLException {
        this.checkClosed();
    }

    public @Nullable String getCursorName() throws SQLException {
        this.checkClosed();
        return null;
    }

    public String getURL() throws SQLException {
        return this.creatingURL;
    }

    public String getUserName() throws SQLException {
        return this.queryExecutor.getUser();
    }

    @Override
    public Fastpath getFastpathAPI() throws SQLException {
        this.checkClosed();
        if (this.fastpath == null) {
            this.fastpath = new Fastpath(this);
        }
        return this.fastpath;
    }

    @Override
    public LargeObjectManager getLargeObjectAPI() throws SQLException {
        this.checkClosed();
        if (this.largeobject == null) {
            this.largeobject = new LargeObjectManager(this);
        }
        return this.largeobject;
    }

    @Override
    public Object getObject(String type, @Nullable String value, byte @Nullable [] byteValue) throws SQLException {
        Class<?> c;
        if (this.typemap != null && (c = this.typemap.get(type)) != null) {
            throw new PSQLException(GT.tr("Custom type maps are not supported.", new Object[0]), PSQLState.NOT_IMPLEMENTED);
        }
        PGobject obj = null;
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Constructing object from type={0} value=<{1}>", new Object[]{type, value});
        }
        try {
            Class<? extends PGobject> klass = this.typeCache.getPGobject(type);
            if (klass != null) {
                obj = klass.newInstance();
                obj.setType(type);
                if (byteValue != null && obj instanceof PGBinaryObject) {
                    PGBinaryObject binObj = (PGBinaryObject)((Object)obj);
                    binObj.setByteValue(byteValue, 0);
                } else {
                    obj.setValue(value);
                }
            } else {
                obj = new PGobject();
                obj.setType(type);
                obj.setValue(value);
            }
            return obj;
        }
        catch (SQLException sx) {
            throw sx;
        }
        catch (Exception ex) {
            throw new PSQLException(GT.tr("Failed to create object for: {0}.", type), PSQLState.CONNECTION_FAILURE, (Throwable)ex);
        }
    }

    protected TypeInfo createTypeInfo(BaseConnection conn, int unknownLength) {
        return new TypeInfoCache(conn, unknownLength);
    }

    @Override
    public TypeInfo getTypeInfo() {
        return this.typeCache;
    }

    @Override
    public void addDataType(String type, String name) {
        try {
            this.addDataType(type, Class.forName(name).asSubclass(PGobject.class));
        }
        catch (Exception e) {
            throw new RuntimeException("Cannot register new type: " + e);
        }
    }

    @Override
    public void addDataType(String type, Class<? extends PGobject> klass) throws SQLException {
        this.checkClosed();
        this.typeCache.addDataType(type, klass);
    }

    private void initObjectTypes(Properties info) throws SQLException {
        this.addDataType("box", PGbox.class);
        this.addDataType("circle", PGcircle.class);
        this.addDataType("line", PGline.class);
        this.addDataType("lseg", PGlseg.class);
        this.addDataType("path", PGpath.class);
        this.addDataType("point", PGpoint.class);
        this.addDataType("polygon", PGpolygon.class);
        this.addDataType("money", PGmoney.class);
        this.addDataType("interval", PGInterval.class);
        Enumeration<?> e = info.propertyNames();
        while (e.hasMoreElements()) {
            Class<?> klass;
            String propertyName = (String)e.nextElement();
            if (propertyName == null || !propertyName.startsWith("datatype.")) continue;
            String typeName = propertyName.substring(9);
            String className = Nullness.castNonNull(info.getProperty(propertyName));
            try {
                klass = Class.forName(className);
            }
            catch (ClassNotFoundException cnfe) {
                throw new PSQLException(GT.tr("Unable to load the class {0} responsible for the datatype {1}", className, typeName), PSQLState.SYSTEM_ERROR, (Throwable)cnfe);
            }
            this.addDataType(typeName, klass.asSubclass(PGobject.class));
        }
    }

    @Override
    public void close() throws SQLException {
        if (this.queryExecutor == null) {
            return;
        }
        if (this.queryExecutor.isClosed()) {
            return;
        }
        this.releaseTimer();
        this.queryExecutor.close();
        this.openStackTrace = null;
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        this.checkClosed();
        CachedQuery cachedQuery = this.queryExecutor.createQuery(sql, false, true, new String[0]);
        return cachedQuery.query.getNativeSql();
    }

    @Override
    public synchronized @Nullable SQLWarning getWarnings() throws SQLException {
        this.checkClosed();
        SQLWarning newWarnings = this.queryExecutor.getWarnings();
        if (this.firstWarning == null) {
            this.firstWarning = newWarnings;
        } else if (newWarnings != null) {
            this.firstWarning.setNextWarning(newWarnings);
        }
        return this.firstWarning;
    }

    @Override
    public synchronized void clearWarnings() throws SQLException {
        this.checkClosed();
        this.queryExecutor.getWarnings();
        this.firstWarning = null;
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        this.checkClosed();
        if (this.queryExecutor.getTransactionState() != TransactionState.IDLE) {
            throw new PSQLException(GT.tr("Cannot change transaction read-only property in the middle of a transaction.", new Object[0]), PSQLState.ACTIVE_SQL_TRANSACTION);
        }
        if (readOnly != this.readOnly && this.autoCommit && this.readOnlyBehavior == ReadOnlyBehavior.always) {
            this.execSQLUpdate(readOnly ? this.setSessionReadOnly : this.setSessionNotReadOnly);
        }
        this.readOnly = readOnly;
        LOGGER.log(Level.FINE, "  setReadOnly = {0}", readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        this.checkClosed();
        return this.readOnly;
    }

    @Override
    public boolean hintReadOnly() {
        return this.readOnly && this.readOnlyBehavior != ReadOnlyBehavior.ignore;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.checkClosed();
        if (this.autoCommit == autoCommit) {
            return;
        }
        if (!this.autoCommit) {
            this.commit();
        }
        if (this.readOnly && this.readOnlyBehavior == ReadOnlyBehavior.always) {
            if (autoCommit) {
                this.autoCommit = true;
                this.execSQLUpdate(this.setSessionReadOnly);
            } else {
                this.execSQLUpdate(this.setSessionNotReadOnly);
            }
        }
        this.autoCommit = autoCommit;
        LOGGER.log(Level.FINE, "  setAutoCommit = {0}", autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        this.checkClosed();
        return this.autoCommit;
    }

    private void executeTransactionCommand(Query query) throws SQLException {
        int flags = 22;
        if (this.prepareThreshold == 0) {
            flags |= 1;
        }
        try {
            this.getQueryExecutor().execute(query, null, new TransactionCommandHandler(), 0, 0, flags);
        }
        catch (SQLException e) {
            if (query.org_postgresql_core_Query_arr_getSubqueries() != null || !this.queryExecutor.willHealOnRetry(e)) {
                throw e;
            }
            query.close();
            this.getQueryExecutor().execute(query, null, new TransactionCommandHandler(), 0, 0, flags);
        }
    }

    @Override
    public void commit() throws SQLException {
        this.checkClosed();
        if (this.autoCommit) {
            throw new PSQLException(GT.tr("Cannot commit when autoCommit is enabled.", new Object[0]), PSQLState.NO_ACTIVE_SQL_TRANSACTION);
        }
        if (this.queryExecutor.getTransactionState() != TransactionState.IDLE) {
            this.executeTransactionCommand(this.commitQuery);
        }
    }

    protected void checkClosed() throws SQLException {
        if (this.isClosed()) {
            throw new PSQLException(GT.tr("This connection has been closed.", new Object[0]), PSQLState.CONNECTION_DOES_NOT_EXIST);
        }
    }

    @Override
    public void rollback() throws SQLException {
        this.checkClosed();
        if (this.autoCommit) {
            throw new PSQLException(GT.tr("Cannot rollback when autoCommit is enabled.", new Object[0]), PSQLState.NO_ACTIVE_SQL_TRANSACTION);
        }
        if (this.queryExecutor.getTransactionState() != TransactionState.IDLE) {
            this.executeTransactionCommand(this.rollbackQuery);
        } else {
            LOGGER.log(Level.FINE, "Rollback requested but no transaction in progress");
        }
    }

    @Override
    public TransactionState getTransactionState() {
        return this.queryExecutor.getTransactionState();
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        this.checkClosed();
        String level = null;
        ResultSet rs = this.execSQLQuery("SHOW TRANSACTION ISOLATION LEVEL");
        if (rs.next()) {
            level = rs.getString(1);
        }
        rs.close();
        if (level == null) {
            return 2;
        }
        if ((level = level.toUpperCase(Locale.US)).equals("READ COMMITTED")) {
            return 2;
        }
        if (level.equals("READ UNCOMMITTED")) {
            return 1;
        }
        if (level.equals("REPEATABLE READ")) {
            return 4;
        }
        if (level.equals("SERIALIZABLE")) {
            return 8;
        }
        return 2;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        this.checkClosed();
        if (this.queryExecutor.getTransactionState() != TransactionState.IDLE) {
            throw new PSQLException(GT.tr("Cannot change transaction isolation level in the middle of a transaction.", new Object[0]), PSQLState.ACTIVE_SQL_TRANSACTION);
        }
        String isolationLevelName = this.getIsolationLevelName(level);
        if (isolationLevelName == null) {
            throw new PSQLException(GT.tr("Transaction isolation level {0} not supported.", level), PSQLState.NOT_IMPLEMENTED);
        }
        String isolationLevelSQL = "SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL " + isolationLevelName;
        this.execSQLUpdate(isolationLevelSQL);
        LOGGER.log(Level.FINE, "  setTransactionIsolation = {0}", isolationLevelName);
    }

    protected @Nullable String getIsolationLevelName(int level) {
        switch (level) {
            case 2: {
                return "READ COMMITTED";
            }
            case 8: {
                return "SERIALIZABLE";
            }
            case 1: {
                return "READ UNCOMMITTED";
            }
            case 4: {
                return "REPEATABLE READ";
            }
        }
        return null;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        this.checkClosed();
    }

    @Override
    public String getCatalog() throws SQLException {
        this.checkClosed();
        return this.queryExecutor.getDatabase();
    }

    public boolean getHideUnprivilegedObjects() {
        return this.hideUnprivilegedObjects;
    }

    protected void finalize() throws Throwable {
        try {
            if (this.openStackTrace != null) {
                LOGGER.log(Level.WARNING, GT.tr("Finalizing a Connection that was never closed:", new Object[0]), this.openStackTrace);
            }
            this.close();
        }
        finally {
            super.finalize();
        }
    }

    public String getDBVersionNumber() {
        return this.queryExecutor.getServerVersion();
    }

    public int getServerMajorVersion() {
        try {
            StringTokenizer versionTokens = new StringTokenizer(this.queryExecutor.getServerVersion(), ".");
            return PgConnection.integerPart(versionTokens.nextToken());
        }
        catch (NoSuchElementException e) {
            return 0;
        }
    }

    public int getServerMinorVersion() {
        try {
            StringTokenizer versionTokens = new StringTokenizer(this.queryExecutor.getServerVersion(), ".");
            versionTokens.nextToken();
            return PgConnection.integerPart(versionTokens.nextToken());
        }
        catch (NoSuchElementException e) {
            return 0;
        }
    }

    @Override
    public boolean haveMinimumServerVersion(int ver) {
        return this.queryExecutor.getServerVersionNum() >= ver;
    }

    @Override
    public boolean haveMinimumServerVersion(Version ver) {
        return this.haveMinimumServerVersion(ver.getVersionNum());
    }

    @Override
    @Pure
    public Encoding getEncoding() {
        return this.queryExecutor.getEncoding();
    }

    @Override
    public byte @PolyNull [] encodeString(@PolyNull String str) throws SQLException {
        try {
            return this.getEncoding().encode(str);
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("Unable to translate data into the desired encoding.", new Object[0]), PSQLState.DATA_ERROR, (Throwable)ioe);
        }
    }

    @Override
    public String escapeString(String str) throws SQLException {
        return Utils.escapeLiteral(null, str, this.queryExecutor.getStandardConformingStrings()).toString();
    }

    @Override
    public boolean getStandardConformingStrings() {
        return this.queryExecutor.getStandardConformingStrings();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.queryExecutor.isClosed();
    }

    @Override
    public void cancelQuery() throws SQLException {
        this.checkClosed();
        this.queryExecutor.sendQueryCancel();
    }

    @Override
    public PGNotification[] getNotifications() throws SQLException {
        return this.getNotifications(-1);
    }

    @Override
    public PGNotification[] getNotifications(int timeoutMillis) throws SQLException {
        this.checkClosed();
        this.getQueryExecutor().processNotifies(timeoutMillis);
        PGNotification[] notifications = this.queryExecutor.getNotifications();
        return notifications;
    }

    @Override
    public int getPrepareThreshold() {
        return this.prepareThreshold;
    }

    @Override
    public void setDefaultFetchSize(int fetchSize) throws SQLException {
        if (fetchSize < 0) {
            throw new PSQLException(GT.tr("Fetch size must be a value greater to or equal to 0.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.defaultFetchSize = fetchSize;
        LOGGER.log(Level.FINE, "  setDefaultFetchSize = {0}", fetchSize);
    }

    @Override
    public int getDefaultFetchSize() {
        return this.defaultFetchSize;
    }

    @Override
    public void setPrepareThreshold(int newThreshold) {
        this.prepareThreshold = newThreshold;
        LOGGER.log(Level.FINE, "  setPrepareThreshold = {0}", newThreshold);
    }

    public boolean getForceBinary() {
        return this.forcebinary;
    }

    public void setForceBinary(boolean newValue) {
        this.forcebinary = newValue;
        LOGGER.log(Level.FINE, "  setForceBinary = {0}", newValue);
    }

    public void setTypeMapImpl(Map<String, Class<?>> map) throws SQLException {
        this.typemap = map;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    public int getProtocolVersion() {
        return this.queryExecutor.getProtocolVersion();
    }

    @Override
    public boolean getStringVarcharFlag() {
        return this.bindStringAsVarchar;
    }

    @Override
    public CopyManager getCopyAPI() throws SQLException {
        this.checkClosed();
        if (this.copyManager == null) {
            this.copyManager = new CopyManager(this);
        }
        return this.copyManager;
    }

    @Override
    public boolean binaryTransferSend(int oid) {
        return this.queryExecutor.useBinaryForSend(oid);
    }

    @Override
    public int getBackendPID() {
        return this.queryExecutor.getBackendPID();
    }

    @Override
    public boolean isColumnSanitiserDisabled() {
        return this.disableColumnSanitiser;
    }

    public void setDisableColumnSanitiser(boolean disableColumnSanitiser) {
        this.disableColumnSanitiser = disableColumnSanitiser;
        LOGGER.log(Level.FINE, "  setDisableColumnSanitiser = {0}", disableColumnSanitiser);
    }

    @Override
    public PreferQueryMode getPreferQueryMode() {
        return this.queryExecutor.getPreferQueryMode();
    }

    @Override
    public AutoSave getAutosave() {
        return this.queryExecutor.getAutoSave();
    }

    @Override
    public void setAutosave(AutoSave autoSave) {
        this.queryExecutor.setAutoSave(autoSave);
        LOGGER.log(Level.FINE, "  setAutosave = {0}", autoSave.value());
    }

    protected void abort() {
        this.queryExecutor.abort();
    }

    private synchronized Timer getTimer() {
        if (this.cancelTimer == null) {
            this.cancelTimer = Driver.getSharedTimer().getTimer();
        }
        return this.cancelTimer;
    }

    private synchronized void releaseTimer() {
        if (this.cancelTimer != null) {
            this.cancelTimer = null;
            Driver.getSharedTimer().releaseTimer();
        }
    }

    @Override
    public void addTimerTask(TimerTask timerTask, long milliSeconds) {
        Timer timer = this.getTimer();
        timer.schedule(timerTask, milliSeconds);
    }

    @Override
    public void purgeTimerTasks() {
        Timer timer = this.cancelTimer;
        if (timer != null) {
            timer.purge();
        }
    }

    @Override
    public String escapeIdentifier(String identifier) throws SQLException {
        return Utils.escapeIdentifier(null, identifier).toString();
    }

    @Override
    public String escapeLiteral(String literal) throws SQLException {
        return Utils.escapeLiteral(null, literal, this.queryExecutor.getStandardConformingStrings()).toString();
    }

    @Override
    public LruCache<FieldMetadata.Key, FieldMetadata> getFieldMetadataCache() {
        return this.fieldMetadataCache;
    }

    @Override
    public PGReplicationConnection getReplicationAPI() {
        return new PGReplicationConnectionImpl(this);
    }

    private static int integerPart(String dirtyString) {
        int end;
        int start;
        for (start = 0; start < dirtyString.length() && !Character.isDigit(dirtyString.charAt(start)); ++start) {
        }
        for (end = start; end < dirtyString.length() && Character.isDigit(dirtyString.charAt(end)); ++end) {
        }
        if (start == end) {
            return 0;
        }
        return Integer.parseInt(dirtyString.substring(start, end));
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        this.checkClosed();
        return new PgStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        this.checkClosed();
        return new PgPreparedStatement(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        this.checkClosed();
        return new PgCallableStatement(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        this.checkClosed();
        if (this.metadata == null) {
            this.metadata = new PgDatabaseMetaData(this);
        }
        return this.metadata;
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        this.setTypeMapImpl(map);
        LOGGER.log(Level.FINE, "  setTypeMap = {0}", map);
    }

    protected Array makeArray(int oid, @Nullable String fieldString) throws SQLException {
        return new PgArray((BaseConnection)this, oid, fieldString);
    }

    protected Blob makeBlob(long oid) throws SQLException {
        return new PgBlob(this, oid);
    }

    protected Clob makeClob(long oid) throws SQLException {
        return new PgClob(this, oid);
    }

    protected SQLXML makeSQLXML() throws SQLException {
        return new PgSQLXML(this);
    }

    @Override
    public Clob createClob() throws SQLException {
        this.checkClosed();
        throw Driver.notImplemented(this.getClass(), "createClob()");
    }

    @Override
    public Blob createBlob() throws SQLException {
        this.checkClosed();
        throw Driver.notImplemented(this.getClass(), "createBlob()");
    }

    @Override
    public NClob createNClob() throws SQLException {
        this.checkClosed();
        throw Driver.notImplemented(this.getClass(), "createNClob()");
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        this.checkClosed();
        return this.makeSQLXML();
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        this.checkClosed();
        throw Driver.notImplemented(this.getClass(), "createStruct(String, Object[])");
    }

    @Override
    public Array createArrayOf(String typeName, @Nullable Object elements) throws SQLException {
        this.checkClosed();
        TypeInfo typeInfo = this.getTypeInfo();
        int oid = typeInfo.getPGArrayType(typeName);
        char delim = typeInfo.getArrayDelimiter(oid);
        if (oid == 0) {
            throw new PSQLException(GT.tr("Unable to find server array type for provided name {0}.", typeName), PSQLState.INVALID_NAME);
        }
        if (elements == null) {
            return this.makeArray(oid, null);
        }
        ArrayEncoding.ArrayEncoder<Object> arraySupport = ArrayEncoding.getArrayEncoder(elements);
        if (arraySupport.supportBinaryRepresentation(oid) && this.getPreferQueryMode() != PreferQueryMode.SIMPLE) {
            return new PgArray((BaseConnection)this, oid, arraySupport.toBinaryRepresentation(this, elements, oid));
        }
        String arrayString = arraySupport.toArrayString(delim, elements);
        return this.makeArray(oid, arrayString);
    }

    @Override
    public Array createArrayOf(String typeName, @Nullable Object @Nullable [] elements) throws SQLException {
        return this.createArrayOf(typeName, (Object)elements);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isValid(int timeout) throws SQLException {
        boolean bl;
        block16: {
            if (timeout < 0) {
                throw new PSQLException(GT.tr("Invalid timeout ({0}<0).", timeout), PSQLState.INVALID_PARAMETER_VALUE);
            }
            if (this.isClosed()) {
                return false;
            }
            boolean changedNetworkTimeout = false;
            int oldNetworkTimeout = this.getNetworkTimeout();
            int newNetworkTimeout = (int)Math.min((long)timeout * 1000L, Integer.MAX_VALUE);
            try {
                if (newNetworkTimeout != 0 && (oldNetworkTimeout == 0 || newNetworkTimeout < oldNetworkTimeout)) {
                    changedNetworkTimeout = true;
                    this.setNetworkTimeout(null, newNetworkTimeout);
                }
                if (this.replicationConnection) {
                    Statement statement = this.createStatement();
                    statement.execute("IDENTIFY_SYSTEM");
                    statement.close();
                } else {
                    PreparedStatement checkConnectionQuery;
                    PgConnection pgConnection = this;
                    synchronized (pgConnection) {
                        checkConnectionQuery = this.checkConnectionQuery;
                        if (checkConnectionQuery == null) {
                            this.checkConnectionQuery = checkConnectionQuery = this.prepareStatement("");
                        }
                    }
                    checkConnectionQuery.executeUpdate();
                }
                bl = true;
                if (!changedNetworkTimeout) break block16;
            }
            catch (Throwable throwable) {
                try {
                    if (changedNetworkTimeout) {
                        this.setNetworkTimeout(null, oldNetworkTimeout);
                    }
                    throw throwable;
                }
                catch (SQLException e) {
                    if (PSQLState.IN_FAILED_SQL_TRANSACTION.getState().equals(e.getSQLState())) {
                        return true;
                    }
                    LOGGER.log(Level.FINE, GT.tr("Validating connection.", new Object[0]), e);
                    return false;
                }
            }
            this.setNetworkTimeout(null, oldNetworkTimeout);
        }
        return bl;
    }

    @Override
    public void setClientInfo(String name, @Nullable String value) throws SQLClientInfoException {
        try {
            this.checkClosed();
        }
        catch (SQLException cause) {
            HashMap<String, ClientInfoStatus> failures = new HashMap<String, ClientInfoStatus>();
            failures.put(name, ClientInfoStatus.REASON_UNKNOWN);
            throw new SQLClientInfoException(GT.tr("This connection has been closed.", new Object[0]), failures, (Throwable)cause);
        }
        if (this.haveMinimumServerVersion(ServerVersion.v9_0) && "ApplicationName".equals(name)) {
            String oldValue;
            if (value == null) {
                value = "";
            }
            if (value.equals(oldValue = this.queryExecutor.getApplicationName())) {
                return;
            }
            try {
                StringBuilder sql = new StringBuilder("SET application_name = '");
                Utils.escapeLiteral(sql, value, this.getStandardConformingStrings());
                sql.append("'");
                this.execSQLUpdate(sql.toString());
            }
            catch (SQLException sqle) {
                HashMap<String, ClientInfoStatus> failures = new HashMap<String, ClientInfoStatus>();
                failures.put(name, ClientInfoStatus.REASON_UNKNOWN);
                throw new SQLClientInfoException(GT.tr("Failed to set ClientInfo property: {0}", "ApplicationName"), sqle.getSQLState(), failures, (Throwable)sqle);
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "  setClientInfo = {0} {1}", new Object[]{name, value});
            }
            this.clientInfo.put(name, value);
            return;
        }
        this.addWarning(new SQLWarning(GT.tr("ClientInfo property not supported.", new Object[0]), PSQLState.NOT_IMPLEMENTED.getState()));
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        try {
            this.checkClosed();
        }
        catch (SQLException cause) {
            HashMap<String, ClientInfoStatus> failures = new HashMap<String, ClientInfoStatus>();
            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                failures.put((String)e.getKey(), ClientInfoStatus.REASON_UNKNOWN);
            }
            throw new SQLClientInfoException(GT.tr("This connection has been closed.", new Object[0]), failures, (Throwable)cause);
        }
        HashMap<String, ClientInfoStatus> failures = new HashMap<String, ClientInfoStatus>();
        for (String name : new String[]{"ApplicationName"}) {
            try {
                this.setClientInfo(name, properties.getProperty(name, null));
            }
            catch (SQLClientInfoException e) {
                failures.putAll(e.getFailedProperties());
            }
        }
        if (!failures.isEmpty()) {
            throw new SQLClientInfoException(GT.tr("One or more ClientInfo failed.", new Object[0]), PSQLState.NOT_IMPLEMENTED.getState(), failures);
        }
    }

    @Override
    public @Nullable String getClientInfo(String name) throws SQLException {
        this.checkClosed();
        this.clientInfo.put("ApplicationName", this.queryExecutor.getApplicationName());
        return this.clientInfo.getProperty(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        this.checkClosed();
        this.clientInfo.put("ApplicationName", this.queryExecutor.getApplicationName());
        return this.clientInfo;
    }

    public <T> T createQueryObject(Class<T> ifc) throws SQLException {
        this.checkClosed();
        throw Driver.notImplemented(this.getClass(), "createQueryObject(Class<T>)");
    }

    @Override
    public boolean getLogServerErrorDetail() {
        return this.logServerErrorDetail;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        this.checkClosed();
        return iface.isAssignableFrom(this.getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        this.checkClosed();
        if (iface.isAssignableFrom(this.getClass())) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public @Nullable String getSchema() throws SQLException {
        this.checkClosed();
        try (Statement stmt = this.createStatement();){
            ResultSet rs;
            block8: {
                String string;
                rs = stmt.executeQuery("select current_schema()");
                try {
                    if (rs.next()) break block8;
                    string = null;
                }
                catch (Throwable throwable) {
                    rs.close();
                    throw throwable;
                }
                rs.close();
                return string;
            }
            String string = rs.getString(1);
            rs.close();
            return string;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setSchema(@Nullable String schema) throws SQLException {
        this.checkClosed();
        try (Statement stmt = this.createStatement();){
            if (schema == null) {
                stmt.executeUpdate("SET SESSION search_path TO DEFAULT");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("SET SESSION search_path TO '");
                Utils.escapeLiteral(sb, schema, this.getStandardConformingStrings());
                sb.append("'");
                stmt.executeUpdate(sb.toString());
                LOGGER.log(Level.FINE, "  setSchema = {0}", schema);
            }
        }
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        if (executor == null) {
            throw new SQLException("executor is null");
        }
        if (this.isClosed()) {
            return;
        }
        SQL_PERMISSION_ABORT.checkGuard(this);
        AbortCommand command = new AbortCommand();
        executor.execute(command);
    }

    @Override
    public void setNetworkTimeout(@Nullable Executor executor, int milliseconds) throws SQLException {
        this.checkClosed();
        if (milliseconds < 0) {
            throw new PSQLException(GT.tr("Network timeout must be a value greater than or equal to 0.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.checkPermission(SQL_PERMISSION_NETWORK_TIMEOUT);
        try {
            this.queryExecutor.setNetworkTimeout(milliseconds);
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("Unable to set network timeout.", new Object[0]), PSQLState.COMMUNICATION_ERROR, (Throwable)ioe);
        }
    }

    private void checkPermission(SQLPermission sqlPermissionNetworkTimeout) {
        if (SYSTEM_GET_SECURITY_MANAGER != null && SECURITY_MANAGER_CHECK_PERMISSION != null) {
            try {
                Object securityManager = SYSTEM_GET_SECURITY_MANAGER.invoke();
                if (securityManager != null) {
                    SECURITY_MANAGER_CHECK_PERMISSION.invoke(securityManager, sqlPermissionNetworkTimeout);
                }
            }
            catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        this.checkClosed();
        try {
            return this.queryExecutor.getNetworkTimeout();
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("Unable to get network timeout.", new Object[0]), PSQLState.COMMUNICATION_ERROR, (Throwable)ioe);
        }
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        this.checkClosed();
        switch (holdability) {
            case 2: {
                this.rsHoldability = holdability;
                break;
            }
            case 1: {
                this.rsHoldability = holdability;
                break;
            }
            default: {
                throw new PSQLException(GT.tr("Unknown ResultSet holdability setting: {0}.", holdability), PSQLState.INVALID_PARAMETER_VALUE);
            }
        }
        LOGGER.log(Level.FINE, "  setHoldability = {0}", holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        this.checkClosed();
        return this.rsHoldability;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        this.checkClosed();
        if (this.getAutoCommit()) {
            throw new PSQLException(GT.tr("Cannot establish a savepoint in auto-commit mode.", new Object[0]), PSQLState.NO_ACTIVE_SQL_TRANSACTION);
        }
        PSQLSavepoint savepoint = new PSQLSavepoint(this.savepointId++);
        String pgName = savepoint.getPGName();
        Statement stmt = this.createStatement();
        stmt.executeUpdate("SAVEPOINT " + pgName);
        stmt.close();
        return savepoint;
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        this.checkClosed();
        if (this.getAutoCommit()) {
            throw new PSQLException(GT.tr("Cannot establish a savepoint in auto-commit mode.", new Object[0]), PSQLState.NO_ACTIVE_SQL_TRANSACTION);
        }
        PSQLSavepoint savepoint = new PSQLSavepoint(name);
        Statement stmt = this.createStatement();
        stmt.executeUpdate("SAVEPOINT " + savepoint.getPGName());
        stmt.close();
        return savepoint;
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        this.checkClosed();
        PSQLSavepoint pgSavepoint = (PSQLSavepoint)savepoint;
        this.execSQLUpdate("ROLLBACK TO SAVEPOINT " + pgSavepoint.getPGName());
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        this.checkClosed();
        PSQLSavepoint pgSavepoint = (PSQLSavepoint)savepoint;
        this.execSQLUpdate("RELEASE SAVEPOINT " + pgSavepoint.getPGName());
        pgSavepoint.invalidate();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        this.checkClosed();
        return this.createStatement(resultSetType, resultSetConcurrency, this.getHoldability());
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        this.checkClosed();
        return this.prepareStatement(sql, resultSetType, resultSetConcurrency, this.getHoldability());
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        this.checkClosed();
        return this.prepareCall(sql, resultSetType, resultSetConcurrency, this.getHoldability());
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        if (autoGeneratedKeys != 1) {
            return this.prepareStatement(sql);
        }
        return this.prepareStatement(sql, (String[])null);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int @Nullable [] columnIndexes) throws SQLException {
        if (columnIndexes != null && columnIndexes.length == 0) {
            return this.prepareStatement(sql);
        }
        this.checkClosed();
        throw new PSQLException(GT.tr("Returning autogenerated keys is not supported.", new Object[0]), PSQLState.NOT_IMPLEMENTED);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String @Nullable [] columnNames) throws SQLException {
        if (columnNames != null && columnNames.length == 0) {
            return this.prepareStatement(sql);
        }
        CachedQuery cachedQuery = this.borrowReturningQuery(sql, columnNames);
        PgPreparedStatement ps = new PgPreparedStatement(this, cachedQuery, 1003, 1007, this.getHoldability());
        Query query = cachedQuery.query;
        SqlCommand sqlCommand = query.getSqlCommand();
        if (sqlCommand != null) {
            ps.wantsGeneratedKeysAlways = sqlCommand.isReturningKeywordPresent();
        }
        return ps;
    }

    @Override
    public final Map<String, String> getParameterStatuses() {
        return this.queryExecutor.getParameterStatuses();
    }

    @Override
    public final @Nullable String getParameterStatus(String parameterName) {
        return this.queryExecutor.getParameterStatus(parameterName);
    }

    @Override
    public boolean getAdaptiveFetch() {
        return this.queryExecutor.getAdaptiveFetch();
    }

    @Override
    public void setAdaptiveFetch(boolean adaptiveFetch) {
        this.queryExecutor.setAdaptiveFetch(adaptiveFetch);
    }

    @Override
    public PGXmlFactoryFactory getXmlFactoryFactory() throws SQLException {
        PGXmlFactoryFactory xmlFactoryFactory = this.xmlFactoryFactory;
        if (xmlFactoryFactory != null) {
            return xmlFactoryFactory;
        }
        if (this.xmlFactoryFactoryClass == null || this.xmlFactoryFactoryClass.equals("")) {
            xmlFactoryFactory = DefaultPGXmlFactoryFactory.INSTANCE;
        } else if (this.xmlFactoryFactoryClass.equals("LEGACY_INSECURE")) {
            xmlFactoryFactory = LegacyInsecurePGXmlFactoryFactory.INSTANCE;
        } else {
            Class<PGXmlFactoryFactory> clazz;
            try {
                clazz = Class.forName(this.xmlFactoryFactoryClass);
            }
            catch (ClassNotFoundException ex) {
                throw new PSQLException(GT.tr("Could not instantiate xmlFactoryFactory: {0}", this.xmlFactoryFactoryClass), PSQLState.INVALID_PARAMETER_VALUE, (Throwable)ex);
            }
            if (!clazz.isAssignableFrom(PGXmlFactoryFactory.class)) {
                throw new PSQLException(GT.tr("Connection property xmlFactoryFactory must implement PGXmlFactoryFactory: {0}", this.xmlFactoryFactoryClass), PSQLState.INVALID_PARAMETER_VALUE);
            }
            try {
                xmlFactoryFactory = (PGXmlFactoryFactory)clazz.newInstance();
            }
            catch (Exception ex) {
                throw new PSQLException(GT.tr("Could not instantiate xmlFactoryFactory: {0}", this.xmlFactoryFactoryClass), PSQLState.INVALID_PARAMETER_VALUE, (Throwable)ex);
            }
        }
        this.xmlFactoryFactory = xmlFactoryFactory;
        return xmlFactoryFactory;
    }

    static {
        MethodHandle systemGetSecurityManagerHandle = null;
        MethodHandle securityManagerCheckPermission = null;
        try {
            Class<?> securityManagerClass = Class.forName("java.lang.SecurityManager");
            systemGetSecurityManagerHandle = MethodHandles.lookup().findStatic(System.class, "getSecurityManager", MethodType.methodType(securityManagerClass));
            securityManagerCheckPermission = MethodHandles.lookup().findVirtual(securityManagerClass, "checkPermission", MethodType.methodType(Void.TYPE, Permission.class));
        }
        catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException reflectiveOperationException) {
            // empty catch block
        }
        SYSTEM_GET_SECURITY_MANAGER = systemGetSecurityManagerHandle;
        SECURITY_MANAGER_CHECK_PERMISSION = securityManagerCheckPermission;
    }

    public class AbortCommand
    implements Runnable {
        @Override
        public void run() {
            PgConnection.this.abort();
        }
    }

    private class TransactionCommandHandler
    extends ResultHandlerBase {
        private TransactionCommandHandler() {
        }

        @Override
        public void handleCompletion() throws SQLException {
            SQLWarning warning = this.getWarning();
            if (warning != null) {
                PgConnection.this.addWarning(warning);
            }
            super.handleCompletion();
        }
    }

    private static enum ReadOnlyBehavior {
        ignore,
        transaction,
        always;

    }
}

