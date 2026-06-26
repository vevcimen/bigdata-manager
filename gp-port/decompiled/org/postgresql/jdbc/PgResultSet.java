/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.postgresql.Driver;
import org.postgresql.PGRefCursorResultSet;
import org.postgresql.PGResultSetMetaData;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.BaseStatement;
import org.postgresql.core.Encoding;
import org.postgresql.core.Field;
import org.postgresql.core.Oid;
import org.postgresql.core.Query;
import org.postgresql.core.ResultCursor;
import org.postgresql.core.ResultHandlerBase;
import org.postgresql.core.TransactionState;
import org.postgresql.core.Tuple;
import org.postgresql.core.TypeInfo;
import org.postgresql.core.Utils;
import org.postgresql.jdbc.BooleanTypeUtil;
import org.postgresql.jdbc.PgArray;
import org.postgresql.jdbc.PgBlob;
import org.postgresql.jdbc.PgClob;
import org.postgresql.jdbc.PgDatabaseMetaData;
import org.postgresql.jdbc.PgResultSetMetaData;
import org.postgresql.jdbc.PgSQLXML;
import org.postgresql.jdbc.PgStatement;
import org.postgresql.jdbc.QueryExecutorTimeZoneProvider;
import org.postgresql.jdbc.TimestampUtils;
import org.postgresql.util.ByteConverter;
import org.postgresql.util.GT;
import org.postgresql.util.HStoreConverter;
import org.postgresql.util.JdbcBlackHole;
import org.postgresql.util.PGbytea;
import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class PgResultSet
implements ResultSet,
PGRefCursorResultSet {
    private boolean updateable = false;
    private boolean doingUpdates = false;
    private @Nullable HashMap<String, Object> updateValues = null;
    private boolean usingOID = false;
    private @Nullable List<PrimaryKey> primaryKeys;
    private boolean singleTable = false;
    private String onlyTable = "";
    private @Nullable String tableName = null;
    private @Nullable PreparedStatement deleteStatement = null;
    private final int resultsettype;
    private final int resultsetconcurrency;
    private int fetchdirection = 1002;
    private @Nullable TimeZone defaultTimeZone;
    protected final BaseConnection connection;
    protected final BaseStatement statement;
    protected final Field[] fields;
    protected final @Nullable Query originalQuery;
    private @Nullable TimestampUtils timestampUtils;
    protected final int maxRows;
    protected final int maxFieldSize;
    protected @Nullable List<Tuple> rows;
    protected int currentRow = -1;
    protected int rowOffset;
    protected @Nullable Tuple thisRow;
    protected @Nullable SQLWarning warnings = null;
    protected boolean wasNullFlag = false;
    protected boolean onInsertRow = false;
    private @Nullable Tuple rowBuffer = null;
    protected int fetchSize;
    protected int lastUsedFetchSize;
    protected boolean adaptiveFetch = false;
    protected @Nullable ResultCursor cursor;
    private @Nullable Map<String, Integer> columnNameIndexMap;
    private @Nullable ResultSetMetaData rsMetaData;
    private static final LocalDate LOCAL_DATE_EPOCH = LocalDate.of(1970, 1, 1);
    private @Nullable String refCursorName;
    private static final BigInteger BYTEMAX = new BigInteger(Byte.toString((byte)127));
    private static final BigInteger BYTEMIN = new BigInteger(Byte.toString((byte)-128));
    private static final NumberFormatException FAST_NUMBER_FAILED = new NumberFormatException(){

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    };
    private static final BigInteger SHORTMAX = new BigInteger(Short.toString((short)Short.MAX_VALUE));
    private static final BigInteger SHORTMIN = new BigInteger(Short.toString((short)Short.MIN_VALUE));
    private static final BigInteger INTMAX = new BigInteger(Integer.toString(Integer.MAX_VALUE));
    private static final BigInteger INTMIN = new BigInteger(Integer.toString(Integer.MIN_VALUE));
    private static final BigInteger LONGMAX = new BigInteger(Long.toString(Long.MAX_VALUE));
    private static final BigInteger LONGMIN = new BigInteger(Long.toString(Long.MIN_VALUE));

    protected ResultSetMetaData createMetaData() throws SQLException {
        return new PgResultSetMetaData(this.connection, this.fields);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        this.checkClosed();
        if (this.rsMetaData == null) {
            this.rsMetaData = this.createMetaData();
        }
        return this.rsMetaData;
    }

    PgResultSet(@Nullable Query originalQuery, BaseStatement statement, Field[] fields, List<Tuple> tuples, @Nullable ResultCursor cursor, int maxRows, int maxFieldSize, int rsType, int rsConcurrency, int rsHoldability, boolean adaptiveFetch) throws SQLException {
        if (tuples == null) {
            throw new NullPointerException("tuples must be non-null");
        }
        if (fields == null) {
            throw new NullPointerException("fields must be non-null");
        }
        this.originalQuery = originalQuery;
        this.connection = (BaseConnection)statement.getConnection();
        this.statement = statement;
        this.fields = fields;
        this.rows = tuples;
        this.cursor = cursor;
        this.maxRows = maxRows;
        this.maxFieldSize = maxFieldSize;
        this.resultsettype = rsType;
        this.resultsetconcurrency = rsConcurrency;
        this.adaptiveFetch = adaptiveFetch;
        this.lastUsedFetchSize = tuples.size();
    }

    @Override
    public URL getURL(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getURL columnIndex: {0}", columnIndex);
        this.checkClosed();
        throw Driver.notImplemented(this.getClass(), "getURL(int)");
    }

    @Override
    public URL getURL(String columnName) throws SQLException {
        return this.getURL(this.findColumn(columnName));
    }

    @RequiresNonNull(value={"thisRow"})
    protected @Nullable Object internalGetObject(@Positive int columnIndex, Field field) throws SQLException {
        Nullness.castNonNull(this.thisRow, "thisRow");
        switch (this.getSQLType(columnIndex)) {
            case -7: 
            case 16: {
                byte[] data;
                if (field.getOID() == 16) {
                    return this.getBoolean(columnIndex);
                }
                if (field.getOID() == 1560 && ((data = this.getRawValue(columnIndex)) == null || data.length == 1)) {
                    return this.getBoolean(columnIndex);
                }
                return null;
            }
            case 2009: {
                return this.getSQLXML(columnIndex);
            }
            case -6: 
            case 4: 
            case 5: {
                return this.getInt(columnIndex);
            }
            case -5: {
                return this.getLong(columnIndex);
            }
            case 2: 
            case 3: {
                return this.getNumeric(columnIndex, field.getMod() == -1 ? -1 : field.getMod() - 4 & 0xFFFF, true);
            }
            case 7: {
                return Float.valueOf(this.getFloat(columnIndex));
            }
            case 6: 
            case 8: {
                return this.getDouble(columnIndex);
            }
            case -1: 
            case 1: 
            case 12: {
                return this.getString(columnIndex);
            }
            case 91: {
                return this.getDate(columnIndex);
            }
            case 92: {
                return this.getTime(columnIndex);
            }
            case 93: {
                return this.getTimestamp(columnIndex, null);
            }
            case -4: 
            case -3: 
            case -2: {
                return this.getBytes(columnIndex);
            }
            case 2003: {
                return this.getArray(columnIndex);
            }
            case 2005: {
                return this.getClob(columnIndex);
            }
            case 2004: {
                return this.getBlob(columnIndex);
            }
        }
        String type = this.getPGType(columnIndex);
        if (type.equals("unknown")) {
            return this.getString(columnIndex);
        }
        if (type.equals("uuid")) {
            if (this.isBinary(columnIndex)) {
                return this.getUUID(Nullness.castNonNull(this.thisRow.get(columnIndex - 1)));
            }
            return this.getUUID(Nullness.castNonNull(this.getString(columnIndex)));
        }
        if (type.equals("refcursor")) {
            String cursorName = Nullness.castNonNull(this.getString(columnIndex));
            StringBuilder sb = new StringBuilder("FETCH ALL IN ");
            Utils.escapeIdentifier(sb, cursorName);
            ResultSet rs = this.connection.execSQLQuery(sb.toString(), this.resultsettype, 1007);
            ((PgResultSet)rs).setRefCursor(cursorName);
            ((PgResultSet)rs).closeRefCursor();
            return rs;
        }
        if ("hstore".equals(type)) {
            if (this.isBinary(columnIndex)) {
                return HStoreConverter.fromBytes(Nullness.castNonNull(this.thisRow.get(columnIndex - 1)), this.connection.getEncoding());
            }
            return HStoreConverter.fromString(Nullness.castNonNull(this.getString(columnIndex)));
        }
        return null;
    }

    @Pure
    @EnsuresNonNull(value={"rows"})
    private void checkScrollable() throws SQLException {
        this.checkClosed();
        if (this.resultsettype == 1003) {
            throw new PSQLException(GT.tr("Operation requires a scrollable ResultSet, but this ResultSet is FORWARD_ONLY.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    public boolean absolute(int index) throws SQLException {
        int internalIndex;
        this.checkScrollable();
        if (index == 0) {
            this.beforeFirst();
            return false;
        }
        int rows_size = this.rows.size();
        if (index < 0) {
            if (index < -rows_size) {
                this.beforeFirst();
                return false;
            }
            internalIndex = rows_size + index;
        } else {
            if (index > rows_size) {
                this.afterLast();
                return false;
            }
            internalIndex = index - 1;
        }
        this.currentRow = internalIndex;
        this.initRowBuffer();
        this.onInsertRow = false;
        return true;
    }

    @Override
    public void afterLast() throws SQLException {
        this.checkScrollable();
        int rows_size = this.rows.size();
        if (rows_size > 0) {
            this.currentRow = rows_size;
        }
        this.onInsertRow = false;
        this.thisRow = null;
        this.rowBuffer = null;
    }

    @Override
    public void beforeFirst() throws SQLException {
        this.checkScrollable();
        if (!this.rows.isEmpty()) {
            this.currentRow = -1;
        }
        this.onInsertRow = false;
        this.thisRow = null;
        this.rowBuffer = null;
    }

    @Override
    public boolean first() throws SQLException {
        this.checkScrollable();
        if (this.rows.size() <= 0) {
            return false;
        }
        this.currentRow = 0;
        this.initRowBuffer();
        this.onInsertRow = false;
        return true;
    }

    @Override
    public @Nullable Array getArray(String colName) throws SQLException {
        return this.getArray(this.findColumn(colName));
    }

    protected Array makeArray(int oid, byte[] value) throws SQLException {
        return new PgArray(this.connection, oid, value);
    }

    protected Array makeArray(int oid, String value) throws SQLException {
        return new PgArray(this.connection, oid, value);
    }

    @Override
    @Pure
    public @Nullable Array getArray(int i) throws SQLException {
        byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        int oid = this.fields[i - 1].getOID();
        if (this.isBinary(i)) {
            return this.makeArray(oid, value);
        }
        return this.makeArray(oid, Nullness.castNonNull(this.getFixedString(i)));
    }

    @Override
    public @Nullable BigDecimal getBigDecimal(@Positive int columnIndex) throws SQLException {
        return this.getBigDecimal(columnIndex, -1);
    }

    @Override
    public @Nullable BigDecimal getBigDecimal(String columnName) throws SQLException {
        return this.getBigDecimal(this.findColumn(columnName));
    }

    @Override
    public @Nullable Blob getBlob(String columnName) throws SQLException {
        return this.getBlob(this.findColumn(columnName));
    }

    protected Blob makeBlob(long oid) throws SQLException {
        return new PgBlob(this.connection, oid);
    }

    @Override
    @Pure
    public @Nullable Blob getBlob(int i) throws SQLException {
        byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        return this.makeBlob(this.getLong(i));
    }

    @Override
    public @Nullable Reader getCharacterStream(String columnName) throws SQLException {
        return this.getCharacterStream(this.findColumn(columnName));
    }

    @Override
    public @Nullable Reader getCharacterStream(int i) throws SQLException {
        String value = this.getString(i);
        if (value == null) {
            return null;
        }
        return new CharArrayReader(value.toCharArray());
    }

    @Override
    public @Nullable Clob getClob(String columnName) throws SQLException {
        return this.getClob(this.findColumn(columnName));
    }

    protected Clob makeClob(long oid) throws SQLException {
        return new PgClob(this.connection, oid);
    }

    @Override
    @Pure
    public @Nullable Clob getClob(int i) throws SQLException {
        byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        return this.makeClob(this.getLong(i));
    }

    @Override
    public int getConcurrency() throws SQLException {
        this.checkClosed();
        return this.resultsetconcurrency;
    }

    @Override
    public @Nullable java.sql.Date getDate(int i, @Nullable Calendar cal) throws SQLException {
        byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        if (cal == null) {
            cal = this.getDefaultCalendar();
        }
        if (this.isBinary(i)) {
            int col = i - 1;
            int oid = this.fields[col].getOID();
            TimeZone tz = cal.getTimeZone();
            if (oid == 1082) {
                return this.getTimestampUtils().toDateBin(tz, value);
            }
            if (oid == 1114 || oid == 1184) {
                Timestamp timestamp = Nullness.castNonNull(this.getTimestamp(i, cal));
                return this.getTimestampUtils().convertToDate(timestamp.getTime(), tz);
            }
            throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), "date"), PSQLState.DATA_TYPE_MISMATCH);
        }
        return this.getTimestampUtils().toDate(cal, Nullness.castNonNull(this.getString(i)));
    }

    @Override
    public @Nullable Time getTime(int i, @Nullable Calendar cal) throws SQLException {
        byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        if (cal == null) {
            cal = this.getDefaultCalendar();
        }
        if (this.isBinary(i)) {
            int col = i - 1;
            int oid = this.fields[col].getOID();
            TimeZone tz = cal.getTimeZone();
            if (oid == 1083 || oid == 1266) {
                return this.getTimestampUtils().toTimeBin(tz, value);
            }
            if (oid == 1114 || oid == 1184) {
                Timestamp timestamp = this.getTimestamp(i, cal);
                if (timestamp == null) {
                    return null;
                }
                long timeMillis = timestamp.getTime();
                if (oid == 1184) {
                    return new Time(timeMillis % TimeUnit.DAYS.toMillis(1L));
                }
                return this.getTimestampUtils().convertToTime(timeMillis, tz);
            }
            throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), "time"), PSQLState.DATA_TYPE_MISMATCH);
        }
        String string = this.getString(i);
        return this.getTimestampUtils().toTime(cal, string);
    }

    @Override
    @Pure
    public @Nullable Timestamp getTimestamp(int i, @Nullable Calendar cal) throws SQLException {
        byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        if (cal == null) {
            cal = this.getDefaultCalendar();
        }
        int col = i - 1;
        int oid = this.fields[col].getOID();
        if (this.isBinary(i)) {
            byte[] row = Nullness.castNonNull(this.thisRow).get(col);
            if (oid == 1184 || oid == 1114) {
                boolean hasTimeZone = oid == 1184;
                TimeZone tz = cal.getTimeZone();
                return this.connection.getTimestampUtils().toTimestampBin(tz, Nullness.castNonNull(row), hasTimeZone);
            }
            if (oid == 1083) {
                Timestamp tsWithMicros = this.connection.getTimestampUtils().toTimestampBin(cal.getTimeZone(), Nullness.castNonNull(row), false);
                Timestamp tsUnixEpochDate = new Timestamp(Nullness.castNonNull(this.getTime(i, cal)).getTime());
                tsUnixEpochDate.setNanos(tsWithMicros.getNanos());
                return tsUnixEpochDate;
            }
            if (oid == 1266) {
                TimeZone tz = cal.getTimeZone();
                byte[] timeBytesWithoutTimeZone = Arrays.copyOfRange(Nullness.castNonNull(row), 0, 8);
                Timestamp tsWithMicros = this.connection.getTimestampUtils().toTimestampBin(tz, timeBytesWithoutTimeZone, false);
                Timestamp tsUnixEpochDate = new Timestamp(Nullness.castNonNull(this.getTime(i, cal)).getTime());
                tsUnixEpochDate.setNanos(tsWithMicros.getNanos());
                return tsUnixEpochDate;
            }
            if (oid == 1082) {
                new Timestamp(Nullness.castNonNull(this.getDate(i, cal)).getTime());
            } else {
                throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), "timestamp"), PSQLState.DATA_TYPE_MISMATCH);
            }
        }
        String string = Nullness.castNonNull(this.getString(i));
        if (oid == 1083 || oid == 1266) {
            Timestamp tsWithMicros = this.connection.getTimestampUtils().toTimestamp(cal, string);
            Timestamp tsUnixEpochDate = new Timestamp(this.connection.getTimestampUtils().toTime(cal, string).getTime());
            tsUnixEpochDate.setNanos(tsWithMicros.getNanos());
            return tsUnixEpochDate;
        }
        return this.connection.getTimestampUtils().toTimestamp(cal, string);
    }

    private @Nullable OffsetDateTime getOffsetDateTime(int i) throws SQLException {
        byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        int col = i - 1;
        int oid = this.fields[col].getOID();
        if (this.isBinary(i)) {
            if (oid == 1184 || oid == 1114) {
                return this.getTimestampUtils().toOffsetDateTimeBin(value);
            }
            if (oid == 1266) {
                return this.getTimestampUtils().toOffsetTimeBin(value).atDate(LOCAL_DATE_EPOCH);
            }
        } else {
            if (oid == 1184 || oid == 1114) {
                OffsetDateTime offsetDateTime = this.getTimestampUtils().toOffsetDateTime(Nullness.castNonNull(this.getString(i)));
                if (offsetDateTime != OffsetDateTime.MAX && offsetDateTime != OffsetDateTime.MIN) {
                    return offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC);
                }
                return offsetDateTime;
            }
            if (oid == 1266) {
                return this.getTimestampUtils().toOffsetDateTime(Nullness.castNonNull(this.getString(i)));
            }
        }
        throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), "java.time.OffsetDateTime"), PSQLState.DATA_TYPE_MISMATCH);
    }

    private @Nullable OffsetTime getOffsetTime(int i) throws SQLException {
        byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        int col = i - 1;
        int oid = this.fields[col].getOID();
        if (oid == 1266) {
            if (this.isBinary(i)) {
                return this.getTimestampUtils().toOffsetTimeBin(value);
            }
            return this.getTimestampUtils().toOffsetTime(Nullness.castNonNull(this.getString(i)));
        }
        throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), "java.time.OffsetTime"), PSQLState.DATA_TYPE_MISMATCH);
    }

    private @Nullable LocalDateTime getLocalDateTime(int i) throws SQLException {
        byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        int col = i - 1;
        int oid = this.fields[col].getOID();
        if (oid == 1114) {
            if (this.isBinary(i)) {
                return this.getTimestampUtils().toLocalDateTimeBin(value);
            }
            return this.getTimestampUtils().toLocalDateTime(Nullness.castNonNull(this.getString(i)));
        }
        throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), "java.time.LocalDateTime"), PSQLState.DATA_TYPE_MISMATCH);
    }

    private @Nullable LocalDate getLocalDate(int i) throws SQLException {
        byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        int col = i - 1;
        int oid = this.fields[col].getOID();
        if (this.isBinary(i)) {
            if (oid == 1082) {
                return this.getTimestampUtils().toLocalDateBin(value);
            }
            if (oid == 1114) {
                return this.getTimestampUtils().toLocalDateTimeBin(value).toLocalDate();
            }
        } else if (oid == 1082 || oid == 1114) {
            return this.getTimestampUtils().toLocalDateTime(Nullness.castNonNull(this.getString(i))).toLocalDate();
        }
        throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), "java.time.LocalDate"), PSQLState.DATA_TYPE_MISMATCH);
    }

    private @Nullable LocalTime getLocalTime(int i) throws SQLException {
        byte[] value = this.getRawValue(i);
        if (value == null) {
            return null;
        }
        int col = i - 1;
        int oid = this.fields[col].getOID();
        if (oid == 1083) {
            if (this.isBinary(i)) {
                return this.getTimestampUtils().toLocalTimeBin(value);
            }
            return this.getTimestampUtils().toLocalTime(this.getString(i));
        }
        throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), "java.time.LocalTime"), PSQLState.DATA_TYPE_MISMATCH);
    }

    @Override
    public @Nullable java.sql.Date getDate(String c, @Nullable Calendar cal) throws SQLException {
        return this.getDate(this.findColumn(c), cal);
    }

    @Override
    public @Nullable Time getTime(String c, @Nullable Calendar cal) throws SQLException {
        return this.getTime(this.findColumn(c), cal);
    }

    @Override
    public @Nullable Timestamp getTimestamp(String c, @Nullable Calendar cal) throws SQLException {
        return this.getTimestamp(this.findColumn(c), cal);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        this.checkClosed();
        return this.fetchdirection;
    }

    public @Nullable Object getObjectImpl(String columnName, @Nullable Map<String, Class<?>> map) throws SQLException {
        return this.getObjectImpl(this.findColumn(columnName), map);
    }

    public @Nullable Object getObjectImpl(int i, @Nullable Map<String, Class<?>> map) throws SQLException {
        this.checkClosed();
        if (map == null || map.isEmpty()) {
            return this.getObject(i);
        }
        throw Driver.notImplemented(this.getClass(), "getObjectImpl(int,Map)");
    }

    @Override
    public @Nullable Ref getRef(String columnName) throws SQLException {
        return this.getRef(this.findColumn(columnName));
    }

    @Override
    public @Nullable Ref getRef(int i) throws SQLException {
        this.checkClosed();
        throw Driver.notImplemented(this.getClass(), "getRef(int)");
    }

    @Override
    public int getRow() throws SQLException {
        this.checkClosed();
        if (this.onInsertRow) {
            return 0;
        }
        int rows_size = this.rows.size();
        if (this.currentRow < 0 || this.currentRow >= rows_size) {
            return 0;
        }
        return this.rowOffset + this.currentRow + 1;
    }

    @Override
    public Statement getStatement() throws SQLException {
        this.checkClosed();
        return this.statement;
    }

    @Override
    public int getType() throws SQLException {
        this.checkClosed();
        return this.resultsettype;
    }

    @Override
    @Pure
    public boolean isAfterLast() throws SQLException {
        this.checkClosed();
        if (this.onInsertRow) {
            return false;
        }
        Nullness.castNonNull(this.rows, "rows");
        int rows_size = this.rows.size();
        if (this.rowOffset + rows_size == 0) {
            return false;
        }
        return this.currentRow >= rows_size;
    }

    @Override
    @Pure
    public boolean isBeforeFirst() throws SQLException {
        this.checkClosed();
        if (this.onInsertRow) {
            return false;
        }
        return this.rowOffset + this.currentRow < 0 && !Nullness.castNonNull(this.rows, "rows").isEmpty();
    }

    @Override
    public boolean isFirst() throws SQLException {
        this.checkClosed();
        if (this.onInsertRow) {
            return false;
        }
        int rows_size = this.rows.size();
        if (this.rowOffset + rows_size == 0) {
            return false;
        }
        return this.rowOffset + this.currentRow == 0;
    }

    @Override
    public boolean isLast() throws SQLException {
        this.checkClosed();
        if (this.onInsertRow) {
            return false;
        }
        List<Tuple> rows = Nullness.castNonNull(this.rows, "rows");
        int rows_size = rows.size();
        if (rows_size == 0) {
            return false;
        }
        if (this.currentRow != rows_size - 1) {
            return false;
        }
        ResultCursor cursor = this.cursor;
        if (cursor == null) {
            return true;
        }
        if (this.maxRows > 0 && this.rowOffset + this.currentRow == this.maxRows) {
            return true;
        }
        this.rowOffset += rows_size - 1;
        int fetchRows = this.fetchSize;
        int adaptiveFetchRows = this.connection.getQueryExecutor().getAdaptiveFetchSize(this.adaptiveFetch, cursor);
        if (adaptiveFetchRows != -1) {
            fetchRows = adaptiveFetchRows;
        }
        if (this.maxRows != 0 && (fetchRows == 0 || this.rowOffset + fetchRows > this.maxRows)) {
            fetchRows = this.maxRows - this.rowOffset;
        }
        this.connection.getQueryExecutor().fetch(cursor, new CursorResultHandler(), fetchRows, this.adaptiveFetch);
        this.lastUsedFetchSize = fetchRows;
        rows = Nullness.castNonNull(this.rows, "rows");
        rows.add(0, Nullness.castNonNull(this.thisRow));
        this.currentRow = 0;
        return rows.size() == 1;
    }

    @Override
    public boolean last() throws SQLException {
        this.checkScrollable();
        List<Tuple> rows = Nullness.castNonNull(this.rows, "rows");
        int rows_size = rows.size();
        if (rows_size <= 0) {
            return false;
        }
        this.currentRow = rows_size - 1;
        this.initRowBuffer();
        this.onInsertRow = false;
        return true;
    }

    @Override
    public boolean previous() throws SQLException {
        this.checkScrollable();
        if (this.onInsertRow) {
            throw new PSQLException(GT.tr("Can''t use relative move methods while on the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.currentRow - 1 < 0) {
            this.currentRow = -1;
            this.thisRow = null;
            this.rowBuffer = null;
            return false;
        }
        --this.currentRow;
        this.initRowBuffer();
        return true;
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        this.checkScrollable();
        if (this.onInsertRow) {
            throw new PSQLException(GT.tr("Can''t use relative move methods while on the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        int index = this.currentRow + 1 + rows;
        if (index < 0) {
            this.beforeFirst();
            return false;
        }
        return this.absolute(index);
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        this.checkClosed();
        switch (direction) {
            case 1000: {
                break;
            }
            case 1001: 
            case 1002: {
                this.checkScrollable();
                break;
            }
            default: {
                throw new PSQLException(GT.tr("Invalid fetch direction constant: {0}.", direction), PSQLState.INVALID_PARAMETER_VALUE);
            }
        }
        this.fetchdirection = direction;
    }

    @Override
    public synchronized void cancelRowUpdates() throws SQLException {
        this.checkClosed();
        if (this.onInsertRow) {
            throw new PSQLException(GT.tr("Cannot call cancelRowUpdates() when on the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.doingUpdates) {
            this.doingUpdates = false;
            this.clearRowBuffer(true);
        }
    }

    @Override
    public synchronized void deleteRow() throws SQLException {
        this.checkUpdateable();
        if (this.onInsertRow) {
            throw new PSQLException(GT.tr("Cannot call deleteRow() when on the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.isBeforeFirst()) {
            throw new PSQLException(GT.tr("Currently positioned before the start of the ResultSet.  You cannot call deleteRow() here.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.isAfterLast()) {
            throw new PSQLException(GT.tr("Currently positioned after the end of the ResultSet.  You cannot call deleteRow() here.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        List<Tuple> rows = Nullness.castNonNull(this.rows, "rows");
        if (rows.isEmpty()) {
            throw new PSQLException(GT.tr("There are no rows in this ResultSet.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        List<PrimaryKey> primaryKeys = Nullness.castNonNull(this.primaryKeys, "primaryKeys");
        int numKeys = primaryKeys.size();
        if (this.deleteStatement == null) {
            StringBuilder deleteSQL = new StringBuilder("DELETE FROM ").append(this.onlyTable).append(this.tableName).append(" where ");
            for (int i = 0; i < numKeys; ++i) {
                Utils.escapeIdentifier(deleteSQL, primaryKeys.get((int)i).name);
                deleteSQL.append(" = ?");
                if (i >= numKeys - 1) continue;
                deleteSQL.append(" and ");
            }
            this.deleteStatement = this.connection.prepareStatement(deleteSQL.toString());
        }
        this.deleteStatement.clearParameters();
        for (int i = 0; i < numKeys; ++i) {
            this.deleteStatement.setObject(i + 1, primaryKeys.get(i).getValue());
        }
        this.deleteStatement.executeUpdate();
        rows.remove(this.currentRow);
        --this.currentRow;
        this.moveToCurrentRow();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized void insertRow() throws SQLException {
        this.checkUpdateable();
        Nullness.castNonNull(this.rows, "rows");
        if (!this.onInsertRow) {
            throw new PSQLException(GT.tr("Not on the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        HashMap<String, Object> updateValues = this.updateValues;
        if (updateValues == null || updateValues.isEmpty()) {
            throw new PSQLException(GT.tr("You must specify at least one column value to insert a row.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
        }
        StringBuilder insertSQL = new StringBuilder("INSERT INTO ").append(this.tableName).append(" (");
        StringBuilder paramSQL = new StringBuilder(") values (");
        Iterator<String> columnNames = updateValues.keySet().iterator();
        int numColumns = updateValues.size();
        int i = 0;
        while (columnNames.hasNext()) {
            String columnName = columnNames.next();
            Utils.escapeIdentifier(insertSQL, columnName);
            if (i < numColumns - 1) {
                insertSQL.append(", ");
                paramSQL.append("?,");
            } else {
                paramSQL.append("?)");
            }
            ++i;
        }
        insertSQL.append(paramSQL.toString());
        PreparedStatement insertStatement = null;
        Tuple rowBuffer = Nullness.castNonNull(this.rowBuffer);
        try {
            insertStatement = this.connection.prepareStatement(insertSQL.toString(), 1);
            Iterator<Object> values = updateValues.values().iterator();
            int i2 = 1;
            while (values.hasNext()) {
                insertStatement.setObject(i2, values.next());
                ++i2;
            }
            insertStatement.executeUpdate();
            if (this.usingOID) {
                long insertedOID = ((PgStatement)((Object)insertStatement)).getLastOID();
                updateValues.put("oid", insertedOID);
            }
            this.updateRowBuffer(insertStatement, rowBuffer, Nullness.castNonNull(updateValues));
        }
        catch (Throwable throwable) {
            JdbcBlackHole.close(insertStatement);
            throw throwable;
        }
        JdbcBlackHole.close(insertStatement);
        Nullness.castNonNull(this.rows).add(rowBuffer);
        this.thisRow = rowBuffer;
        this.clearRowBuffer(false);
    }

    @Override
    public synchronized void moveToCurrentRow() throws SQLException {
        this.checkUpdateable();
        Nullness.castNonNull(this.rows, "rows");
        if (this.currentRow < 0 || this.currentRow >= this.rows.size()) {
            this.thisRow = null;
            this.rowBuffer = null;
        } else {
            this.initRowBuffer();
        }
        this.onInsertRow = false;
        this.doingUpdates = false;
    }

    @Override
    public synchronized void moveToInsertRow() throws SQLException {
        this.checkUpdateable();
        this.clearRowBuffer(false);
        this.onInsertRow = true;
        this.doingUpdates = false;
    }

    private synchronized void clearRowBuffer(boolean copyCurrentRow) throws SQLException {
        this.rowBuffer = copyCurrentRow ? Nullness.castNonNull(this.thisRow, "thisRow").updateableCopy() : new Tuple(this.fields.length);
        HashMap<String, Object> updateValues = this.updateValues;
        if (updateValues != null) {
            updateValues.clear();
        }
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        this.checkClosed();
        return false;
    }

    @Override
    public boolean rowInserted() throws SQLException {
        this.checkClosed();
        return false;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        this.checkClosed();
        return false;
    }

    @Override
    public synchronized void updateAsciiStream(@Positive int columnIndex, @Nullable InputStream x, int length) throws SQLException {
        if (x == null) {
            this.updateNull(columnIndex);
            return;
        }
        try {
            int n;
            InputStreamReader reader = new InputStreamReader(x, StandardCharsets.US_ASCII);
            char[] data = new char[length];
            int numRead = 0;
            while ((n = reader.read(data, numRead, length - numRead)) != -1 && (numRead += n) != length) {
            }
            this.updateString(columnIndex, new String(data, 0, numRead));
        }
        catch (IOException ie) {
            throw new PSQLException(GT.tr("Provided InputStream failed.", new Object[0]), null, (Throwable)ie);
        }
    }

    @Override
    public synchronized void updateBigDecimal(@Positive int columnIndex, @Nullable BigDecimal x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    @Override
    public synchronized void updateBinaryStream(@Positive int columnIndex, @Nullable InputStream x, int length) throws SQLException {
        if (x == null) {
            this.updateNull(columnIndex);
            return;
        }
        byte[] data = new byte[length];
        int numRead = 0;
        try {
            int n;
            while ((n = x.read(data, numRead, length - numRead)) != -1 && (numRead += n) != length) {
            }
        }
        catch (IOException ie) {
            throw new PSQLException(GT.tr("Provided InputStream failed.", new Object[0]), null, (Throwable)ie);
        }
        if (numRead == length) {
            this.updateBytes(columnIndex, data);
        } else {
            byte[] data2 = new byte[numRead];
            System.arraycopy(data, 0, data2, 0, numRead);
            this.updateBytes(columnIndex, data2);
        }
    }

    @Override
    public synchronized void updateBoolean(@Positive int columnIndex, boolean x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    @Override
    public synchronized void updateByte(@Positive int columnIndex, byte x) throws SQLException {
        this.updateValue(columnIndex, String.valueOf(x));
    }

    @Override
    public synchronized void updateBytes(@Positive int columnIndex, byte @Nullable [] x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    @Override
    public synchronized void updateCharacterStream(@Positive int columnIndex, @Nullable Reader x, int length) throws SQLException {
        if (x == null) {
            this.updateNull(columnIndex);
            return;
        }
        try {
            int n;
            char[] data = new char[length];
            int numRead = 0;
            while ((n = x.read(data, numRead, length - numRead)) != -1 && (numRead += n) != length) {
            }
            this.updateString(columnIndex, new String(data, 0, numRead));
        }
        catch (IOException ie) {
            throw new PSQLException(GT.tr("Provided Reader failed.", new Object[0]), null, (Throwable)ie);
        }
    }

    @Override
    public synchronized void updateDate(@Positive int columnIndex, @Nullable java.sql.Date x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    @Override
    public synchronized void updateDouble(@Positive int columnIndex, double x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    @Override
    public synchronized void updateFloat(@Positive int columnIndex, float x) throws SQLException {
        this.updateValue(columnIndex, Float.valueOf(x));
    }

    @Override
    public synchronized void updateInt(@Positive int columnIndex, int x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    @Override
    public synchronized void updateLong(@Positive int columnIndex, long x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    @Override
    public synchronized void updateNull(@Positive int columnIndex) throws SQLException {
        this.checkColumnIndex(columnIndex);
        String columnTypeName = this.getPGType(columnIndex);
        this.updateValue(columnIndex, new NullObject(columnTypeName));
    }

    @Override
    public synchronized void updateObject(int columnIndex, @Nullable Object x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    @Override
    public synchronized void updateObject(int columnIndex, @Nullable Object x, int scale) throws SQLException {
        this.updateObject(columnIndex, x);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void refreshRow() throws SQLException {
        this.checkUpdateable();
        if (this.onInsertRow) {
            throw new PSQLException(GT.tr("Can''t refresh the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.isBeforeFirst() || this.isAfterLast() || Nullness.castNonNull(this.rows, "rows").isEmpty()) {
            return;
        }
        StringBuilder selectSQL = new StringBuilder("select ");
        ResultSetMetaData rsmd = this.getMetaData();
        PGResultSetMetaData pgmd = (PGResultSetMetaData)((Object)rsmd);
        for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
            if (i > 1) {
                selectSQL.append(", ");
            }
            Utils.escapeIdentifier(selectSQL, pgmd.getBaseColumnName(i));
        }
        selectSQL.append(" from ").append(this.onlyTable).append(this.tableName).append(" where ");
        List<PrimaryKey> primaryKeys = Nullness.castNonNull(this.primaryKeys, "primaryKeys");
        int numKeys = primaryKeys.size();
        for (int i = 0; i < numKeys; ++i) {
            PrimaryKey primaryKey = primaryKeys.get(i);
            Utils.escapeIdentifier(selectSQL, primaryKey.name);
            selectSQL.append(" = ?");
            if (i >= numKeys - 1) continue;
            selectSQL.append(" and ");
        }
        String sqlText = selectSQL.toString();
        if (this.connection.getLogger().isLoggable(Level.FINE)) {
            this.connection.getLogger().log(Level.FINE, "selecting {0}", sqlText);
        }
        PreparedStatement selectStatement = null;
        try {
            selectStatement = this.connection.prepareStatement(sqlText, 1004, 1008);
            for (int i = 0; i < numKeys; ++i) {
                selectStatement.setObject(i + 1, primaryKeys.get(i).getValue());
            }
            PgResultSet rs = (PgResultSet)selectStatement.executeQuery();
            if (rs.next()) {
                this.rowBuffer = rs.thisRow == null ? null : Nullness.castNonNull(rs.thisRow).updateableCopy();
            }
            Nullness.castNonNull(this.rows).set(this.currentRow, Nullness.castNonNull(this.rowBuffer));
            this.thisRow = this.rowBuffer;
            this.connection.getLogger().log(Level.FINE, "done updates");
            rs.close();
        }
        catch (Throwable throwable) {
            JdbcBlackHole.close(selectStatement);
            throw throwable;
        }
        JdbcBlackHole.close(selectStatement);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized void updateRow() throws SQLException {
        this.checkUpdateable();
        if (this.onInsertRow) {
            throw new PSQLException(GT.tr("Cannot call updateRow() when on the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        List<Tuple> rows = Nullness.castNonNull(this.rows, "rows");
        if (this.isBeforeFirst() || this.isAfterLast() || rows.isEmpty()) {
            throw new PSQLException(GT.tr("Cannot update the ResultSet because it is either before the start or after the end of the results.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (!this.doingUpdates) {
            return;
        }
        StringBuilder updateSQL = new StringBuilder("UPDATE " + this.onlyTable + this.tableName + " SET  ");
        HashMap<String, Object> updateValues = Nullness.castNonNull(this.updateValues);
        int numColumns = updateValues.size();
        Iterator<String> columns = updateValues.keySet().iterator();
        int i = 0;
        while (columns.hasNext()) {
            String column = columns.next();
            Utils.escapeIdentifier(updateSQL, column);
            updateSQL.append(" = ?");
            if (i < numColumns - 1) {
                updateSQL.append(", ");
            }
            ++i;
        }
        updateSQL.append(" WHERE ");
        List<PrimaryKey> primaryKeys = Nullness.castNonNull(this.primaryKeys, "primaryKeys");
        int numKeys = primaryKeys.size();
        for (int i2 = 0; i2 < numKeys; ++i2) {
            PrimaryKey primaryKey = primaryKeys.get(i2);
            Utils.escapeIdentifier(updateSQL, primaryKey.name);
            updateSQL.append(" = ?");
            if (i2 >= numKeys - 1) continue;
            updateSQL.append(" and ");
        }
        String sqlText = updateSQL.toString();
        if (this.connection.getLogger().isLoggable(Level.FINE)) {
            this.connection.getLogger().log(Level.FINE, "updating {0}", sqlText);
        }
        PreparedStatement updateStatement = null;
        try {
            updateStatement = this.connection.prepareStatement(sqlText);
            int i3 = 0;
            for (Object o : updateValues.values()) {
                updateStatement.setObject(i3 + 1, o);
                ++i3;
            }
            int j = 0;
            while (j < numKeys) {
                updateStatement.setObject(i3 + 1, primaryKeys.get(j).getValue());
                ++j;
                ++i3;
            }
            updateStatement.executeUpdate();
        }
        catch (Throwable throwable) {
            JdbcBlackHole.close(updateStatement);
            throw throwable;
        }
        JdbcBlackHole.close(updateStatement);
        Tuple rowBuffer = Nullness.castNonNull(this.rowBuffer, "rowBuffer");
        this.updateRowBuffer(null, rowBuffer, updateValues);
        this.connection.getLogger().log(Level.FINE, "copying data");
        this.thisRow = rowBuffer.readOnlyCopy();
        rows.set(this.currentRow, rowBuffer);
        this.connection.getLogger().log(Level.FINE, "done updates");
        updateValues.clear();
        this.doingUpdates = false;
    }

    @Override
    public synchronized void updateShort(@Positive int columnIndex, short x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    @Override
    public synchronized void updateString(@Positive int columnIndex, @Nullable String x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    @Override
    public synchronized void updateTime(@Positive int columnIndex, @Nullable Time x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    @Override
    public synchronized void updateTimestamp(int columnIndex, @Nullable Timestamp x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    @Override
    public synchronized void updateNull(String columnName) throws SQLException {
        this.updateNull(this.findColumn(columnName));
    }

    @Override
    public synchronized void updateBoolean(String columnName, boolean x) throws SQLException {
        this.updateBoolean(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateByte(String columnName, byte x) throws SQLException {
        this.updateByte(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateShort(String columnName, short x) throws SQLException {
        this.updateShort(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateInt(String columnName, int x) throws SQLException {
        this.updateInt(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateLong(String columnName, long x) throws SQLException {
        this.updateLong(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateFloat(String columnName, float x) throws SQLException {
        this.updateFloat(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateDouble(String columnName, double x) throws SQLException {
        this.updateDouble(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateBigDecimal(String columnName, @Nullable BigDecimal x) throws SQLException {
        this.updateBigDecimal(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateString(String columnName, @Nullable String x) throws SQLException {
        this.updateString(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateBytes(String columnName, byte @Nullable [] x) throws SQLException {
        this.updateBytes(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateDate(String columnName, @Nullable java.sql.Date x) throws SQLException {
        this.updateDate(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateTime(String columnName, @Nullable Time x) throws SQLException {
        this.updateTime(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateTimestamp(String columnName, @Nullable Timestamp x) throws SQLException {
        this.updateTimestamp(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateAsciiStream(String columnName, @Nullable InputStream x, int length) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnName), x, length);
    }

    @Override
    public synchronized void updateBinaryStream(String columnName, @Nullable InputStream x, int length) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnName), x, length);
    }

    @Override
    public synchronized void updateCharacterStream(String columnName, @Nullable Reader reader, int length) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnName), reader, length);
    }

    @Override
    public synchronized void updateObject(String columnName, @Nullable Object x, int scale) throws SQLException {
        this.updateObject(this.findColumn(columnName), x);
    }

    @Override
    public synchronized void updateObject(String columnName, @Nullable Object x) throws SQLException {
        this.updateObject(this.findColumn(columnName), x);
    }

    boolean isUpdateable() throws SQLException {
        int oidIndex;
        this.checkClosed();
        if (this.resultsetconcurrency == 1007) {
            throw new PSQLException(GT.tr("ResultSets with concurrency CONCUR_READ_ONLY cannot be updated.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.updateable) {
            return true;
        }
        this.connection.getLogger().log(Level.FINE, "checking if rs is updateable");
        this.parseQuery();
        if (this.tableName == null) {
            this.connection.getLogger().log(Level.FINE, "tableName is not found");
            return false;
        }
        if (!this.singleTable) {
            this.connection.getLogger().log(Level.FINE, "not a single table");
            return false;
        }
        this.usingOID = false;
        this.connection.getLogger().log(Level.FINE, "getting primary keys");
        ArrayList<PrimaryKey> primaryKeys = new ArrayList<PrimaryKey>();
        this.primaryKeys = primaryKeys;
        int i = 0;
        int numPKcolumns = 0;
        @Nullable String[] s2 = PgResultSet.quotelessTableName(Nullness.castNonNull(this.tableName));
        String quotelessTableName = Nullness.castNonNull(s2[0]);
        String quotelessSchemaName = s2[1];
        ResultSet rs = ((PgDatabaseMetaData)this.connection.getMetaData()).getPrimaryUniqueKeys("", quotelessSchemaName, quotelessTableName);
        String lastConstraintName = null;
        while (rs.next()) {
            String columnName;
            int index;
            String constraintName = Nullness.castNonNull(rs.getString(6));
            if (lastConstraintName == null || !lastConstraintName.equals(constraintName)) {
                if (lastConstraintName != null) {
                    if (i == numPKcolumns && numPKcolumns > 0) break;
                    this.connection.getLogger().log(Level.FINE, "no of keys={0} from constraint {1}", new Object[]{i, lastConstraintName});
                }
                i = 0;
                numPKcolumns = 0;
                primaryKeys.clear();
                lastConstraintName = constraintName;
            }
            ++numPKcolumns;
            boolean isNotNull = rs.getBoolean("IS_NOT_NULL");
            if (!isNotNull || (index = this.findColumnIndex(columnName = Nullness.castNonNull(rs.getString(4)))) <= 0) continue;
            ++i;
            primaryKeys.add(new PrimaryKey(index, columnName));
        }
        rs.close();
        this.connection.getLogger().log(Level.FINE, "no of keys={0} from constraint {1}", new Object[]{i, lastConstraintName});
        this.updateable = i == numPKcolumns && numPKcolumns > 0;
        this.connection.getLogger().log(Level.FINE, "checking primary key {0}", this.updateable);
        if (!this.updateable && (oidIndex = this.findColumnIndex("oid")) > 0) {
            primaryKeys.add(new PrimaryKey(oidIndex, "oid"));
            this.usingOID = true;
            this.updateable = true;
        }
        if (!this.updateable) {
            throw new PSQLException(GT.tr("No eligible primary or unique key found for table {0}.", this.tableName), PSQLState.INVALID_CURSOR_STATE);
        }
        return this.updateable;
    }

    public void setAdaptiveFetch(boolean adaptiveFetch) throws SQLException {
        this.checkClosed();
        this.updateQueryInsideAdaptiveFetchCache(adaptiveFetch);
        this.adaptiveFetch = adaptiveFetch;
    }

    private void updateQueryInsideAdaptiveFetchCache(boolean newAdaptiveFetch) {
        if (Objects.nonNull(this.cursor)) {
            ResultCursor resultCursor = this.cursor;
            if (!this.adaptiveFetch && newAdaptiveFetch) {
                this.connection.getQueryExecutor().addQueryToAdaptiveFetchCache(true, resultCursor);
            }
            if (this.adaptiveFetch && !newAdaptiveFetch && Objects.nonNull(this.cursor)) {
                this.connection.getQueryExecutor().removeQueryFromAdaptiveFetchCache(true, resultCursor);
            }
        }
    }

    public boolean getAdaptiveFetch() throws SQLException {
        this.checkClosed();
        return this.adaptiveFetch;
    }

    public static @Nullable String[] quotelessTableName(String fullname) {
        @Nullable String[] parts = new String[]{null, ""};
        StringBuilder acc = new StringBuilder();
        boolean betweenQuotes = false;
        block4: for (int i = 0; i < fullname.length(); ++i) {
            char c = fullname.charAt(i);
            switch (c) {
                case '\"': {
                    if (i < fullname.length() - 1 && fullname.charAt(i + 1) == '\"') {
                        ++i;
                        acc.append(c);
                        continue block4;
                    }
                    betweenQuotes = !betweenQuotes;
                    continue block4;
                }
                case '.': {
                    if (betweenQuotes) {
                        acc.append(c);
                        continue block4;
                    }
                    parts[1] = acc.toString();
                    acc = new StringBuilder();
                    continue block4;
                }
                default: {
                    acc.append(betweenQuotes ? c : Character.toLowerCase(c));
                }
            }
        }
        parts[0] = acc.toString();
        return parts;
    }

    private void parseQuery() {
        Query originalQuery = this.originalQuery;
        if (originalQuery == null) {
            return;
        }
        String sql = originalQuery.toString(null);
        StringTokenizer st = new StringTokenizer(sql, " \r\t\n");
        boolean tableFound = false;
        boolean tablesChecked = false;
        String name = "";
        this.singleTable = true;
        while (!tableFound && !tablesChecked && st.hasMoreTokens()) {
            name = st.nextToken();
            if (!"from".equalsIgnoreCase(name)) continue;
            this.tableName = st.nextToken();
            if ("only".equalsIgnoreCase(this.tableName)) {
                this.tableName = st.nextToken();
                this.onlyTable = "ONLY ";
            }
            tableFound = true;
        }
    }

    private void setRowBufferColumn(Tuple rowBuffer, int columnIndex, @Nullable Object valueObject) throws SQLException {
        if (valueObject instanceof PGobject) {
            String value = ((PGobject)valueObject).getValue();
            rowBuffer.set(columnIndex, value == null ? null : this.connection.encodeString(value));
        } else {
            if (valueObject == null) {
                rowBuffer.set(columnIndex, null);
                return;
            }
            switch (this.getSQLType(columnIndex + 1)) {
                case -7: 
                case 16: {
                    rowBuffer.set(columnIndex, this.connection.encodeString((Boolean)valueObject != false ? "t" : "f"));
                    break;
                }
                case 91: {
                    rowBuffer.set(columnIndex, this.connection.encodeString(this.getTimestampUtils().toString(this.getDefaultCalendar(), (java.sql.Date)valueObject)));
                    break;
                }
                case 92: {
                    rowBuffer.set(columnIndex, this.connection.encodeString(this.getTimestampUtils().toString(this.getDefaultCalendar(), (Time)valueObject)));
                    break;
                }
                case 93: {
                    rowBuffer.set(columnIndex, this.connection.encodeString(this.getTimestampUtils().toString(this.getDefaultCalendar(), (Timestamp)valueObject)));
                    break;
                }
                case 0: {
                    break;
                }
                case -4: 
                case -3: 
                case -2: {
                    if (this.isBinary(columnIndex + 1)) {
                        rowBuffer.set(columnIndex, (byte[])valueObject);
                        break;
                    }
                    try {
                        rowBuffer.set(columnIndex, PGbytea.toPGString((byte[])valueObject).getBytes(this.connection.getEncoding().name()));
                        break;
                    }
                    catch (UnsupportedEncodingException e) {
                        throw new PSQLException(GT.tr("The JVM claims not to support the encoding: {0}", this.connection.getEncoding().name()), PSQLState.UNEXPECTED_ERROR, (Throwable)e);
                    }
                }
                default: {
                    rowBuffer.set(columnIndex, this.connection.encodeString(String.valueOf(valueObject)));
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateRowBuffer(@Nullable PreparedStatement insertStatement, Tuple rowBuffer, HashMap<String, Object> updateValues) throws SQLException {
        for (Map.Entry<String, Object> entry : updateValues.entrySet()) {
            int columnIndex = this.findColumn(entry.getKey()) - 1;
            Object valueObject = entry.getValue();
            this.setRowBufferColumn(rowBuffer, columnIndex, valueObject);
        }
        if (insertStatement == null) {
            return;
        }
        try (ResultSet generatedKeys = insertStatement.getGeneratedKeys();){
            generatedKeys.next();
            List<PrimaryKey> primaryKeys = Nullness.castNonNull(this.primaryKeys);
            int numKeys = primaryKeys.size();
            for (int i = 0; i < numKeys; ++i) {
                PrimaryKey key = primaryKeys.get(i);
                int columnIndex = key.index - 1;
                Object valueObject = generatedKeys.getObject(key.name);
                this.setRowBufferColumn(rowBuffer, columnIndex, valueObject);
            }
        }
    }

    public BaseStatement getPGStatement() {
        return this.statement;
    }

    @Override
    public @Nullable String getRefCursor() {
        return this.refCursorName;
    }

    private void setRefCursor(String refCursorName) {
        this.refCursorName = refCursorName;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        this.checkClosed();
        if (rows < 0) {
            throw new PSQLException(GT.tr("Fetch size must be a value greater to or equal to 0.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.fetchSize = rows;
    }

    @Override
    public int getFetchSize() throws SQLException {
        this.checkClosed();
        if (this.adaptiveFetch) {
            return this.lastUsedFetchSize;
        }
        return this.fetchSize;
    }

    public int getLastUsedFetchSize() throws SQLException {
        this.checkClosed();
        return this.lastUsedFetchSize;
    }

    @Override
    public boolean next() throws SQLException {
        this.checkClosed();
        Nullness.castNonNull(this.rows, "rows");
        if (this.onInsertRow) {
            throw new PSQLException(GT.tr("Can''t use relative move methods while on the insert row.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.currentRow + 1 >= this.rows.size()) {
            ResultCursor cursor = this.cursor;
            if (cursor == null || this.maxRows > 0 && this.rowOffset + this.rows.size() >= this.maxRows) {
                this.currentRow = this.rows.size();
                this.thisRow = null;
                this.rowBuffer = null;
                return false;
            }
            this.rowOffset += this.rows.size();
            int fetchRows = this.fetchSize;
            int adaptiveFetchRows = this.connection.getQueryExecutor().getAdaptiveFetchSize(this.adaptiveFetch, cursor);
            if (adaptiveFetchRows != -1) {
                fetchRows = adaptiveFetchRows;
            }
            if (this.maxRows != 0 && (fetchRows == 0 || this.rowOffset + fetchRows > this.maxRows)) {
                fetchRows = this.maxRows - this.rowOffset;
            }
            this.connection.getQueryExecutor().fetch(cursor, new CursorResultHandler(), fetchRows, this.adaptiveFetch);
            this.closeRefCursor();
            this.lastUsedFetchSize = fetchRows;
            this.currentRow = 0;
            if (this.rows == null || this.rows.isEmpty()) {
                this.thisRow = null;
                this.rowBuffer = null;
                return false;
            }
        } else {
            ++this.currentRow;
        }
        this.initRowBuffer();
        return true;
    }

    @Override
    public void close() throws SQLException {
        try {
            this.closeInternally();
        }
        finally {
            ((PgStatement)this.statement).checkCompletion();
        }
    }

    protected void closeInternally() throws SQLException {
        this.rows = null;
        JdbcBlackHole.close(this.deleteStatement);
        this.deleteStatement = null;
        if (this.cursor != null) {
            this.cursor.close();
            this.cursor = null;
        }
        this.closeRefCursor();
    }

    private void closeRefCursor() throws SQLException {
        String refCursorName = this.refCursorName;
        if (refCursorName == null || this.cursor != null) {
            return;
        }
        try {
            if (this.connection.getTransactionState() == TransactionState.OPEN) {
                StringBuilder sb = new StringBuilder("CLOSE ");
                Utils.escapeIdentifier(sb, refCursorName);
                this.connection.execSQLUpdate(sb.toString());
            }
        }
        finally {
            this.refCursorName = null;
        }
    }

    @Override
    public boolean wasNull() throws SQLException {
        this.checkClosed();
        return this.wasNullFlag;
    }

    @Override
    @Pure
    public @Nullable String getString(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getString columnIndex: {0}", columnIndex);
        byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return null;
        }
        if (this.isBinary(columnIndex) && this.getSQLType(columnIndex) != 12) {
            Field field = this.fields[columnIndex - 1];
            Object obj = this.internalGetObject(columnIndex, field);
            if (obj == null) {
                obj = this.getObject(columnIndex);
                if (obj == null) {
                    return null;
                }
                return obj.toString();
            }
            if (obj instanceof Date) {
                int oid = field.getOID();
                return this.getTimestampUtils().timeToString((Date)obj, oid == 1184 || oid == 1266);
            }
            if ("hstore".equals(this.getPGType(columnIndex))) {
                return HStoreConverter.toString((Map)obj);
            }
            return this.trimString(columnIndex, obj.toString());
        }
        Encoding encoding = this.connection.getEncoding();
        try {
            return this.trimString(columnIndex, encoding.decode(value));
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("Invalid character data was found.  This is most likely caused by stored data containing characters that are invalid for the character set the database was created in.  The most common example of this is storing 8bit data in a SQL_ASCII database.", new Object[0]), PSQLState.DATA_ERROR, (Throwable)ioe);
        }
    }

    @Override
    @Pure
    public boolean getBoolean(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getBoolean columnIndex: {0}", columnIndex);
        byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return false;
        }
        int col = columnIndex - 1;
        if (16 == this.fields[col].getOID()) {
            byte[] v = value;
            return 1 == v.length && 116 == v[0];
        }
        if (this.isBinary(columnIndex)) {
            return BooleanTypeUtil.castToBoolean(this.readDoubleValue(value, this.fields[col].getOID(), "boolean"));
        }
        String stringValue = Nullness.castNonNull(this.getString(columnIndex));
        return BooleanTypeUtil.castToBoolean(stringValue);
    }

    @Override
    public byte getByte(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getByte columnIndex: {0}", columnIndex);
        byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return 0;
        }
        if (this.isBinary(columnIndex)) {
            int col = columnIndex - 1;
            return (byte)this.readLongValue(value, this.fields[col].getOID(), -128L, 127L, "byte");
        }
        String s2 = this.getString(columnIndex);
        if (s2 != null) {
            if ((s2 = s2.trim()).isEmpty()) {
                return 0;
            }
            try {
                return Byte.parseByte(s2);
            }
            catch (NumberFormatException e) {
                try {
                    BigDecimal n = new BigDecimal(s2);
                    BigInteger i = n.toBigInteger();
                    int gt = i.compareTo(BYTEMAX);
                    int lt = i.compareTo(BYTEMIN);
                    if (gt > 0 || lt < 0) {
                        throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "byte", s2), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                    }
                    return i.byteValue();
                }
                catch (NumberFormatException ex) {
                    throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "byte", s2), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                }
            }
        }
        return 0;
    }

    @Override
    public short getShort(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getShort columnIndex: {0}", columnIndex);
        byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return 0;
        }
        if (this.isBinary(columnIndex)) {
            int col = columnIndex - 1;
            int oid = this.fields[col].getOID();
            if (oid == 21) {
                return ByteConverter.int2(value, 0);
            }
            return (short)this.readLongValue(value, oid, -32768L, 32767L, "short");
        }
        return PgResultSet.toShort(this.getFixedString(columnIndex));
    }

    @Override
    @Pure
    public int getInt(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getInt columnIndex: {0}", columnIndex);
        byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return 0;
        }
        if (this.isBinary(columnIndex)) {
            int col = columnIndex - 1;
            int oid = this.fields[col].getOID();
            if (oid == 23) {
                return ByteConverter.int4(value, 0);
            }
            return (int)this.readLongValue(value, oid, Integer.MIN_VALUE, Integer.MAX_VALUE, "int");
        }
        Encoding encoding = this.connection.getEncoding();
        if (encoding.hasAsciiNumbers()) {
            try {
                return this.getFastInt(value);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return PgResultSet.toInt(this.getFixedString(columnIndex));
    }

    @Override
    @Pure
    public long getLong(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getLong columnIndex: {0}", columnIndex);
        byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return 0L;
        }
        if (this.isBinary(columnIndex)) {
            int col = columnIndex - 1;
            int oid = this.fields[col].getOID();
            if (oid == 20) {
                return ByteConverter.int8(value, 0);
            }
            return this.readLongValue(value, oid, Long.MIN_VALUE, Long.MAX_VALUE, "long");
        }
        Encoding encoding = this.connection.getEncoding();
        if (encoding.hasAsciiNumbers()) {
            try {
                return this.getFastLong(value);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return PgResultSet.toLong(this.getFixedString(columnIndex));
    }

    private long getFastLong(byte[] bytes) throws NumberFormatException {
        int start;
        boolean neg;
        if (bytes.length == 0) {
            throw FAST_NUMBER_FAILED;
        }
        long val = 0L;
        if (bytes[0] == 45) {
            neg = true;
            start = 1;
            if (bytes.length == 1 || bytes.length > 19) {
                throw FAST_NUMBER_FAILED;
            }
        } else {
            start = 0;
            neg = false;
            if (bytes.length > 18) {
                throw FAST_NUMBER_FAILED;
            }
        }
        while (start < bytes.length) {
            byte b;
            if ((b = bytes[start++]) < 48 || b > 57) {
                throw FAST_NUMBER_FAILED;
            }
            val *= 10L;
            val += (long)(b - 48);
        }
        if (neg) {
            val = -val;
        }
        return val;
    }

    private int getFastInt(byte[] bytes) throws NumberFormatException {
        int start;
        boolean neg;
        if (bytes.length == 0) {
            throw FAST_NUMBER_FAILED;
        }
        int val = 0;
        if (bytes[0] == 45) {
            neg = true;
            start = 1;
            if (bytes.length == 1 || bytes.length > 10) {
                throw FAST_NUMBER_FAILED;
            }
        } else {
            start = 0;
            neg = false;
            if (bytes.length > 9) {
                throw FAST_NUMBER_FAILED;
            }
        }
        while (start < bytes.length) {
            byte b;
            if ((b = bytes[start++]) < 48 || b > 57) {
                throw FAST_NUMBER_FAILED;
            }
            val *= 10;
            val += b - 48;
        }
        if (neg) {
            val = -val;
        }
        return val;
    }

    private BigDecimal getFastBigDecimal(byte[] bytes) throws NumberFormatException {
        int numNonSignChars;
        int start;
        boolean neg;
        if (bytes.length == 0) {
            throw FAST_NUMBER_FAILED;
        }
        int scale = 0;
        long val = 0L;
        if (bytes[0] == 45) {
            neg = true;
            start = 1;
            if (bytes.length == 1 || bytes.length > 19) {
                throw FAST_NUMBER_FAILED;
            }
        } else {
            start = 0;
            neg = false;
            if (bytes.length > 18) {
                throw FAST_NUMBER_FAILED;
            }
        }
        int periodsSeen = 0;
        while (start < bytes.length) {
            byte b;
            if ((b = bytes[start++]) < 48 || b > 57) {
                if (b == 46) {
                    scale = bytes.length - start;
                    ++periodsSeen;
                    continue;
                }
                throw FAST_NUMBER_FAILED;
            }
            val *= 10L;
            val += (long)(b - 48);
        }
        int n = numNonSignChars = neg ? bytes.length - 1 : bytes.length;
        if (periodsSeen > 1 || periodsSeen == numNonSignChars) {
            throw FAST_NUMBER_FAILED;
        }
        if (neg) {
            val = -val;
        }
        return BigDecimal.valueOf(val, scale);
    }

    @Override
    @Pure
    public float getFloat(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getFloat columnIndex: {0}", columnIndex);
        byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return 0.0f;
        }
        if (this.isBinary(columnIndex)) {
            int col = columnIndex - 1;
            int oid = this.fields[col].getOID();
            if (oid == 700) {
                return ByteConverter.float4(value, 0);
            }
            return (float)this.readDoubleValue(value, oid, "float");
        }
        return PgResultSet.toFloat(this.getFixedString(columnIndex));
    }

    @Override
    @Pure
    public double getDouble(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getDouble columnIndex: {0}", columnIndex);
        byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return 0.0;
        }
        if (this.isBinary(columnIndex)) {
            int col = columnIndex - 1;
            int oid = this.fields[col].getOID();
            if (oid == 701) {
                return ByteConverter.float8(value, 0);
            }
            return this.readDoubleValue(value, oid, "double");
        }
        return PgResultSet.toDouble(this.getFixedString(columnIndex));
    }

    @Override
    public @Nullable BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getBigDecimal columnIndex: {0}", columnIndex);
        return (BigDecimal)this.getNumeric(columnIndex, scale, false);
    }

    @Pure
    private @Nullable Number getNumeric(int columnIndex, int scale, boolean allowNaN) throws SQLException {
        byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return null;
        }
        if (this.isBinary(columnIndex)) {
            int sqlType = this.getSQLType(columnIndex);
            if (sqlType != 2 && sqlType != 3) {
                Object obj = this.internalGetObject(columnIndex, this.fields[columnIndex - 1]);
                if (obj == null) {
                    return null;
                }
                if (obj instanceof Long || obj instanceof Integer || obj instanceof Byte) {
                    BigDecimal res = BigDecimal.valueOf(((Number)obj).longValue());
                    res = this.scaleBigDecimal(res, scale);
                    return res;
                }
                return this.toBigDecimal(this.trimMoney(String.valueOf(obj)), scale);
            }
            Number num = ByteConverter.numeric(value);
            if (allowNaN && Double.isNaN(num.doubleValue())) {
                return Double.NaN;
            }
            return num;
        }
        Encoding encoding = this.connection.getEncoding();
        if (encoding.hasAsciiNumbers()) {
            try {
                BigDecimal res = this.getFastBigDecimal(value);
                res = this.scaleBigDecimal(res, scale);
                return res;
            }
            catch (NumberFormatException res) {
                // empty catch block
            }
        }
        String stringValue = this.getFixedString(columnIndex);
        if (allowNaN && "NaN".equalsIgnoreCase(stringValue)) {
            return Double.NaN;
        }
        return this.toBigDecimal(stringValue, scale);
    }

    @Override
    @Pure
    public byte @Nullable [] getBytes(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getBytes columnIndex: {0}", columnIndex);
        byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return null;
        }
        if (this.isBinary(columnIndex)) {
            return value;
        }
        if (this.fields[columnIndex - 1].getOID() == 17) {
            return this.trimBytes(columnIndex, PGbytea.toBytes(value));
        }
        return this.trimBytes(columnIndex, value);
    }

    @Override
    @Pure
    public @Nullable java.sql.Date getDate(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getDate columnIndex: {0}", columnIndex);
        return this.getDate(columnIndex, null);
    }

    @Override
    @Pure
    public @Nullable Time getTime(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getTime columnIndex: {0}", columnIndex);
        return this.getTime(columnIndex, null);
    }

    @Override
    @Pure
    public @Nullable Timestamp getTimestamp(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getTimestamp columnIndex: {0}", columnIndex);
        return this.getTimestamp(columnIndex, null);
    }

    @Override
    @Pure
    public @Nullable InputStream getAsciiStream(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getAsciiStream columnIndex: {0}", columnIndex);
        byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return null;
        }
        String stringValue = Nullness.castNonNull(this.getString(columnIndex));
        return new ByteArrayInputStream(stringValue.getBytes(StandardCharsets.US_ASCII));
    }

    @Override
    @Pure
    public @Nullable InputStream getUnicodeStream(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getUnicodeStream columnIndex: {0}", columnIndex);
        byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return null;
        }
        String stringValue = Nullness.castNonNull(this.getString(columnIndex));
        return new ByteArrayInputStream(stringValue.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    @Pure
    public @Nullable InputStream getBinaryStream(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getBinaryStream columnIndex: {0}", columnIndex);
        byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return null;
        }
        byte[] b = this.getBytes(columnIndex);
        if (b != null) {
            return new ByteArrayInputStream(b);
        }
        return null;
    }

    @Override
    @Pure
    public @Nullable String getString(String columnName) throws SQLException {
        return this.getString(this.findColumn(columnName));
    }

    @Override
    @Pure
    public boolean getBoolean(String columnName) throws SQLException {
        return this.getBoolean(this.findColumn(columnName));
    }

    @Override
    @Pure
    public byte getByte(String columnName) throws SQLException {
        return this.getByte(this.findColumn(columnName));
    }

    @Override
    @Pure
    public short getShort(String columnName) throws SQLException {
        return this.getShort(this.findColumn(columnName));
    }

    @Override
    @Pure
    public int getInt(String columnName) throws SQLException {
        return this.getInt(this.findColumn(columnName));
    }

    @Override
    @Pure
    public long getLong(String columnName) throws SQLException {
        return this.getLong(this.findColumn(columnName));
    }

    @Override
    @Pure
    public float getFloat(String columnName) throws SQLException {
        return this.getFloat(this.findColumn(columnName));
    }

    @Override
    @Pure
    public double getDouble(String columnName) throws SQLException {
        return this.getDouble(this.findColumn(columnName));
    }

    @Override
    @Pure
    public @Nullable BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return this.getBigDecimal(this.findColumn(columnName), scale);
    }

    @Override
    @Pure
    public byte @Nullable [] getBytes(String columnName) throws SQLException {
        return this.getBytes(this.findColumn(columnName));
    }

    @Override
    @Pure
    public @Nullable java.sql.Date getDate(String columnName) throws SQLException {
        return this.getDate(this.findColumn(columnName), null);
    }

    @Override
    @Pure
    public @Nullable Time getTime(String columnName) throws SQLException {
        return this.getTime(this.findColumn(columnName), null);
    }

    @Override
    @Pure
    public @Nullable Timestamp getTimestamp(String columnName) throws SQLException {
        return this.getTimestamp(this.findColumn(columnName), null);
    }

    @Override
    @Pure
    public @Nullable InputStream getAsciiStream(String columnName) throws SQLException {
        return this.getAsciiStream(this.findColumn(columnName));
    }

    @Override
    @Pure
    public @Nullable InputStream getUnicodeStream(String columnName) throws SQLException {
        return this.getUnicodeStream(this.findColumn(columnName));
    }

    @Override
    @Pure
    public @Nullable InputStream getBinaryStream(String columnName) throws SQLException {
        return this.getBinaryStream(this.findColumn(columnName));
    }

    @Override
    @Pure
    public @Nullable SQLWarning getWarnings() throws SQLException {
        this.checkClosed();
        return this.warnings;
    }

    @Override
    public void clearWarnings() throws SQLException {
        this.checkClosed();
        this.warnings = null;
    }

    protected void addWarning(SQLWarning warnings) {
        if (this.warnings != null) {
            this.warnings.setNextWarning(warnings);
        } else {
            this.warnings = warnings;
        }
    }

    @Override
    public @Nullable String getCursorName() throws SQLException {
        this.checkClosed();
        return null;
    }

    @Override
    public @Nullable Object getObject(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getObject columnIndex: {0}", columnIndex);
        byte[] value = this.getRawValue(columnIndex);
        if (value == null) {
            return null;
        }
        Field field = this.fields[columnIndex - 1];
        if (field == null) {
            this.wasNullFlag = true;
            return null;
        }
        Object result = this.internalGetObject(columnIndex, field);
        if (result != null) {
            return result;
        }
        if (this.isBinary(columnIndex)) {
            return this.connection.getObject(this.getPGType(columnIndex), null, value);
        }
        String stringValue = Nullness.castNonNull(this.getString(columnIndex));
        return this.connection.getObject(this.getPGType(columnIndex), stringValue, null);
    }

    @Override
    public @Nullable Object getObject(String columnName) throws SQLException {
        return this.getObject(this.findColumn(columnName));
    }

    @Override
    public @NonNegative int findColumn(String columnName) throws SQLException {
        this.checkClosed();
        int col = this.findColumnIndex(columnName);
        if (col == 0) {
            throw new PSQLException(GT.tr("The column name {0} was not found in this ResultSet.", columnName), PSQLState.UNDEFINED_COLUMN);
        }
        return col;
    }

    public static Map<String, Integer> createColumnNameIndexMap(Field[] fields, boolean isSanitiserDisabled) {
        HashMap<String, Integer> columnNameIndexMap = new HashMap<String, Integer>(fields.length * 2);
        for (int i = fields.length - 1; i >= 0; --i) {
            String columnLabel = fields[i].getColumnLabel();
            if (isSanitiserDisabled) {
                columnNameIndexMap.put(columnLabel, i + 1);
                continue;
            }
            columnNameIndexMap.put(columnLabel.toLowerCase(Locale.US), i + 1);
        }
        return columnNameIndexMap;
    }

    private @NonNegative int findColumnIndex(String columnName) {
        Integer index;
        if (this.columnNameIndexMap == null) {
            if (this.originalQuery != null) {
                this.columnNameIndexMap = this.originalQuery.getResultSetColumnNameIndexMap();
            }
            if (this.columnNameIndexMap == null) {
                this.columnNameIndexMap = PgResultSet.createColumnNameIndexMap(this.fields, this.connection.isColumnSanitiserDisabled());
            }
        }
        if ((index = this.columnNameIndexMap.get(columnName)) != null) {
            return index;
        }
        index = this.columnNameIndexMap.get(columnName.toLowerCase(Locale.US));
        if (index != null) {
            this.columnNameIndexMap.put(columnName, index);
            return index;
        }
        index = this.columnNameIndexMap.get(columnName.toUpperCase(Locale.US));
        if (index != null) {
            this.columnNameIndexMap.put(columnName, index);
            return index;
        }
        return 0;
    }

    public int getColumnOID(int field) {
        return this.fields[field - 1].getOID();
    }

    public @Nullable String getFixedString(int col) throws SQLException {
        String stringValue = Nullness.castNonNull(this.getString(col));
        return this.trimMoney(stringValue);
    }

    private @PolyNull String trimMoney(@PolyNull String s2) {
        if (s2 == null) {
            return null;
        }
        if (s2.length() < 2) {
            return s2;
        }
        char ch = s2.charAt(0);
        if (ch > '-') {
            return s2;
        }
        if (ch == '(') {
            s2 = "-" + PGtokenizer.removePara(s2).substring(1);
        } else if (ch == '$') {
            s2 = s2.substring(1);
        } else if (ch == '-' && s2.charAt(1) == '$') {
            s2 = "-" + s2.substring(2);
        }
        return s2;
    }

    @Pure
    protected String getPGType(@Positive int column) throws SQLException {
        Field field = this.fields[column - 1];
        this.initSqlType(field);
        return field.getPGType();
    }

    @Pure
    protected int getSQLType(@Positive int column) throws SQLException {
        Field field = this.fields[column - 1];
        this.initSqlType(field);
        return field.getSQLType();
    }

    @Pure
    private void initSqlType(Field field) throws SQLException {
        if (field.isTypeInitialized()) {
            return;
        }
        TypeInfo typeInfo = this.connection.getTypeInfo();
        int oid = field.getOID();
        String pgType = Nullness.castNonNull(typeInfo.getPGType(oid));
        int sqlType = typeInfo.getSQLType(pgType);
        field.setSQLType(sqlType);
        field.setPGType(pgType);
    }

    @EnsuresNonNull(value={"updateValues", "rows"})
    private void checkUpdateable() throws SQLException {
        this.checkClosed();
        if (!this.isUpdateable()) {
            throw new PSQLException(GT.tr("ResultSet is not updateable.  The query that generated this result set must select only one table, and must select all primary keys from that table. See the JDBC 2.1 API Specification, section 5.6 for more details.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        if (this.updateValues == null) {
            this.updateValues = new HashMap((int)((double)this.fields.length / 0.75), 0.75f);
        }
        Nullness.castNonNull(this.updateValues, "updateValues");
        Nullness.castNonNull(this.rows, "rows");
    }

    @Pure
    @EnsuresNonNull(value={"rows"})
    protected void checkClosed() throws SQLException {
        if (this.rows == null) {
            throw new PSQLException(GT.tr("This ResultSet is closed.", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
    }

    protected boolean isResultSetClosed() {
        return this.rows == null;
    }

    @Pure
    protected void checkColumnIndex(@Positive int column) throws SQLException {
        if (column < 1 || column > this.fields.length) {
            throw new PSQLException(GT.tr("The column index is out of range: {0}, number of columns: {1}.", column, this.fields.length), PSQLState.INVALID_PARAMETER_VALUE);
        }
    }

    @EnsuresNonNull(value={"thisRow"})
    protected byte @Nullable [] getRawValue(@Positive int column) throws SQLException {
        this.checkClosed();
        if (this.thisRow == null) {
            throw new PSQLException(GT.tr("ResultSet not positioned properly, perhaps you need to call next.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        this.checkColumnIndex(column);
        byte[] bytes = this.thisRow.get(column - 1);
        this.wasNullFlag = bytes == null;
        return bytes;
    }

    @Pure
    protected boolean isBinary(@Positive int column) {
        return this.fields[column - 1].getFormat() == 1;
    }

    public static short toShort(@Nullable String s2) throws SQLException {
        if (s2 != null) {
            try {
                s2 = s2.trim();
                return Short.parseShort(s2);
            }
            catch (NumberFormatException e) {
                try {
                    BigDecimal n = new BigDecimal(s2);
                    BigInteger i = n.toBigInteger();
                    int gt = i.compareTo(SHORTMAX);
                    int lt = i.compareTo(SHORTMIN);
                    if (gt > 0 || lt < 0) {
                        throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "short", s2), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                    }
                    return i.shortValue();
                }
                catch (NumberFormatException ne) {
                    throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "short", s2), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                }
            }
        }
        return 0;
    }

    public static int toInt(@Nullable String s2) throws SQLException {
        if (s2 != null) {
            try {
                s2 = s2.trim();
                return Integer.parseInt(s2);
            }
            catch (NumberFormatException e) {
                try {
                    BigDecimal n = new BigDecimal(s2);
                    BigInteger i = n.toBigInteger();
                    int gt = i.compareTo(INTMAX);
                    int lt = i.compareTo(INTMIN);
                    if (gt > 0 || lt < 0) {
                        throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "int", s2), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                    }
                    return i.intValue();
                }
                catch (NumberFormatException ne) {
                    throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "int", s2), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                }
            }
        }
        return 0;
    }

    public static long toLong(@Nullable String s2) throws SQLException {
        if (s2 != null) {
            try {
                s2 = s2.trim();
                return Long.parseLong(s2);
            }
            catch (NumberFormatException e) {
                try {
                    BigDecimal n = new BigDecimal(s2);
                    BigInteger i = n.toBigInteger();
                    int gt = i.compareTo(LONGMAX);
                    int lt = i.compareTo(LONGMIN);
                    if (gt > 0 || lt < 0) {
                        throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "long", s2), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                    }
                    return i.longValue();
                }
                catch (NumberFormatException ne) {
                    throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "long", s2), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
                }
            }
        }
        return 0L;
    }

    public static @PolyNull BigDecimal toBigDecimal(@PolyNull String s2) throws SQLException {
        if (s2 == null) {
            return null;
        }
        try {
            s2 = s2.trim();
            return new BigDecimal(s2);
        }
        catch (NumberFormatException e) {
            throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "BigDecimal", s2), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
        }
    }

    public @PolyNull BigDecimal toBigDecimal(@PolyNull String s2, int scale) throws SQLException {
        if (s2 == null) {
            return null;
        }
        BigDecimal val = PgResultSet.toBigDecimal(s2);
        return this.scaleBigDecimal(val, scale);
    }

    private BigDecimal scaleBigDecimal(BigDecimal val, int scale) throws PSQLException {
        if (scale == -1) {
            return val;
        }
        try {
            return val.setScale(scale);
        }
        catch (ArithmeticException e) {
            throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "BigDecimal", val), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
        }
    }

    public static float toFloat(@Nullable String s2) throws SQLException {
        if (s2 != null) {
            try {
                s2 = s2.trim();
                return Float.parseFloat(s2);
            }
            catch (NumberFormatException e) {
                throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "float", s2), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
            }
        }
        return 0.0f;
    }

    public static double toDouble(@Nullable String s2) throws SQLException {
        if (s2 != null) {
            try {
                s2 = s2.trim();
                return Double.parseDouble(s2);
            }
            catch (NumberFormatException e) {
                throw new PSQLException(GT.tr("Bad value for type {0} : {1}", "double", s2), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
            }
        }
        return 0.0;
    }

    @RequiresNonNull(value={"rows"})
    private void initRowBuffer() {
        this.thisRow = Nullness.castNonNull(this.rows, "rows").get(this.currentRow);
        this.rowBuffer = this.resultsetconcurrency == 1008 ? this.thisRow.updateableCopy() : null;
    }

    private boolean isColumnTrimmable(@Positive int columnIndex) throws SQLException {
        switch (this.getSQLType(columnIndex)) {
            case -4: 
            case -3: 
            case -2: 
            case -1: 
            case 1: 
            case 12: {
                return true;
            }
        }
        return false;
    }

    private byte[] trimBytes(@Positive int columnIndex, byte[] bytes) throws SQLException {
        if (this.maxFieldSize > 0 && bytes.length > this.maxFieldSize && this.isColumnTrimmable(columnIndex)) {
            byte[] newBytes = new byte[this.maxFieldSize];
            System.arraycopy(bytes, 0, newBytes, 0, this.maxFieldSize);
            return newBytes;
        }
        return bytes;
    }

    private String trimString(@Positive int columnIndex, String string) throws SQLException {
        if (this.maxFieldSize > 0 && string.length() > this.maxFieldSize && this.isColumnTrimmable(columnIndex)) {
            return string.substring(0, this.maxFieldSize);
        }
        return string;
    }

    private double readDoubleValue(byte[] bytes, int oid, String targetType) throws PSQLException {
        switch (oid) {
            case 21: {
                return ByteConverter.int2(bytes, 0);
            }
            case 23: {
                return ByteConverter.int4(bytes, 0);
            }
            case 20: {
                return ByteConverter.int8(bytes, 0);
            }
            case 700: {
                return ByteConverter.float4(bytes, 0);
            }
            case 701: {
                return ByteConverter.float8(bytes, 0);
            }
            case 1700: {
                return ByteConverter.numeric(bytes).doubleValue();
            }
        }
        throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), targetType), PSQLState.DATA_TYPE_MISMATCH);
    }

    @Pure
    private long readLongValue(byte[] bytes, int oid, long minVal, long maxVal, String targetType) throws PSQLException {
        long val;
        switch (oid) {
            case 21: {
                val = ByteConverter.int2(bytes, 0);
                break;
            }
            case 23: {
                val = ByteConverter.int4(bytes, 0);
                break;
            }
            case 20: {
                val = ByteConverter.int8(bytes, 0);
                break;
            }
            case 700: {
                val = (long)ByteConverter.float4(bytes, 0);
                break;
            }
            case 701: {
                val = (long)ByteConverter.float8(bytes, 0);
                break;
            }
            case 1700: {
                Number num = ByteConverter.numeric(bytes);
                if (num instanceof BigDecimal) {
                    val = ((BigDecimal)num).setScale(0, RoundingMode.DOWN).longValueExact();
                    break;
                }
                val = num.longValue();
                break;
            }
            default: {
                throw new PSQLException(GT.tr("Cannot convert the column of type {0} to requested type {1}.", Oid.toString(oid), targetType), PSQLState.DATA_TYPE_MISMATCH);
            }
        }
        if (val < minVal || val > maxVal) {
            throw new PSQLException(GT.tr("Bad value for type {0} : {1}", targetType, val), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
        }
        return val;
    }

    protected void updateValue(@Positive int columnIndex, @Nullable Object value) throws SQLException {
        this.checkUpdateable();
        if (!this.onInsertRow && (this.isBeforeFirst() || this.isAfterLast() || Nullness.castNonNull(this.rows, "rows").isEmpty())) {
            throw new PSQLException(GT.tr("Cannot update the ResultSet because it is either before the start or after the end of the results.", new Object[0]), PSQLState.INVALID_CURSOR_STATE);
        }
        this.checkColumnIndex(columnIndex);
        boolean bl = this.doingUpdates = !this.onInsertRow;
        if (value == null) {
            this.updateNull(columnIndex);
        } else {
            PGResultSetMetaData md = (PGResultSetMetaData)((Object)this.getMetaData());
            Nullness.castNonNull(this.updateValues, "updateValues").put(md.getBaseColumnName(columnIndex), value);
        }
    }

    @Pure
    protected Object getUUID(String data) throws SQLException {
        UUID uuid;
        try {
            uuid = UUID.fromString(data);
        }
        catch (IllegalArgumentException iae) {
            throw new PSQLException(GT.tr("Invalid UUID data.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE, (Throwable)iae);
        }
        return uuid;
    }

    @Pure
    protected Object getUUID(byte[] data) throws SQLException {
        return new UUID(ByteConverter.int8(data, 0), ByteConverter.int8(data, 8));
    }

    void addRows(List<Tuple> tuples) {
        Nullness.castNonNull(this.rows, "rows").addAll(tuples);
    }

    @Override
    public void updateRef(@Positive int columnIndex, @Nullable Ref x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateRef(int,Ref)");
    }

    @Override
    public void updateRef(String columnName, @Nullable Ref x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateRef(String,Ref)");
    }

    @Override
    public void updateBlob(@Positive int columnIndex, @Nullable Blob x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateBlob(int,Blob)");
    }

    @Override
    public void updateBlob(String columnName, @Nullable Blob x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateBlob(String,Blob)");
    }

    @Override
    public void updateClob(@Positive int columnIndex, @Nullable Clob x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateClob(int,Clob)");
    }

    @Override
    public void updateClob(String columnName, @Nullable Clob x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateClob(String,Clob)");
    }

    @Override
    public void updateArray(@Positive int columnIndex, @Nullable Array x) throws SQLException {
        this.updateObject(columnIndex, (Object)x);
    }

    @Override
    public void updateArray(String columnName, @Nullable Array x) throws SQLException {
        this.updateArray(this.findColumn(columnName), x);
    }

    @Override
    public <T> @Nullable T getObject(@Positive int columnIndex, Class<T> type) throws SQLException {
        if (type == null) {
            throw new SQLException("type is null");
        }
        int sqlType = this.getSQLType(columnIndex);
        if (type == BigDecimal.class) {
            if (sqlType == 2 || sqlType == 3) {
                return type.cast(this.getBigDecimal(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == String.class) {
            if (sqlType == 1 || sqlType == 12) {
                return type.cast(this.getString(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == Boolean.class) {
            if (sqlType == 16 || sqlType == -7) {
                boolean booleanValue = this.getBoolean(columnIndex);
                if (this.wasNull()) {
                    return null;
                }
                return type.cast(booleanValue);
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == Short.class) {
            if (sqlType == 5) {
                short shortValue = this.getShort(columnIndex);
                if (this.wasNull()) {
                    return null;
                }
                return type.cast(shortValue);
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == Integer.class) {
            if (sqlType == 4 || sqlType == 5) {
                int intValue = this.getInt(columnIndex);
                if (this.wasNull()) {
                    return null;
                }
                return type.cast(intValue);
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == Long.class) {
            if (sqlType == -5) {
                long longValue = this.getLong(columnIndex);
                if (this.wasNull()) {
                    return null;
                }
                return type.cast(longValue);
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == BigInteger.class) {
            if (sqlType == -5) {
                long longValue = this.getLong(columnIndex);
                if (this.wasNull()) {
                    return null;
                }
                return type.cast(BigInteger.valueOf(longValue));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == Float.class) {
            if (sqlType == 7) {
                float floatValue = this.getFloat(columnIndex);
                if (this.wasNull()) {
                    return null;
                }
                return type.cast(Float.valueOf(floatValue));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == Double.class) {
            if (sqlType == 6 || sqlType == 8) {
                double doubleValue = this.getDouble(columnIndex);
                if (this.wasNull()) {
                    return null;
                }
                return type.cast(doubleValue);
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == java.sql.Date.class) {
            if (sqlType == 91) {
                return type.cast(this.getDate(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == Time.class) {
            if (sqlType == 92) {
                return type.cast(this.getTime(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == Timestamp.class) {
            if (sqlType == 93 || sqlType == 2014) {
                return type.cast(this.getTimestamp(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == Calendar.class) {
            if (sqlType == 93 || sqlType == 2014) {
                Timestamp timestampValue = this.getTimestamp(columnIndex);
                if (timestampValue == null) {
                    return null;
                }
                Calendar calendar = Calendar.getInstance(this.getDefaultCalendar().getTimeZone());
                calendar.setTimeInMillis(timestampValue.getTime());
                return type.cast(calendar);
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == Blob.class) {
            if (sqlType == 2004 || sqlType == -2 || sqlType == -5) {
                return type.cast(this.getBlob(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == Clob.class) {
            if (sqlType == 2005 || sqlType == -5) {
                return type.cast(this.getClob(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == Date.class) {
            if (sqlType == 93) {
                Timestamp timestamp = this.getTimestamp(columnIndex);
                if (timestamp == null) {
                    return null;
                }
                return type.cast(new Date(timestamp.getTime()));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == Array.class) {
            if (sqlType == 2003) {
                return type.cast(this.getArray(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == SQLXML.class) {
            if (sqlType == 2009) {
                return type.cast(this.getSQLXML(columnIndex));
            }
            throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
        }
        if (type == UUID.class) {
            return type.cast(this.getObject(columnIndex));
        }
        if (type == InetAddress.class) {
            String inetText = this.getString(columnIndex);
            if (inetText == null) {
                return null;
            }
            int slash = inetText.indexOf("/");
            try {
                return type.cast(InetAddress.getByName(slash < 0 ? inetText : inetText.substring(0, slash)));
            }
            catch (UnknownHostException ex) {
                throw new PSQLException(GT.tr("Invalid Inet data.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE, (Throwable)ex);
            }
        }
        if (type == LocalDate.class) {
            return type.cast(this.getLocalDate(columnIndex));
        }
        if (type == LocalTime.class) {
            return type.cast(this.getLocalTime(columnIndex));
        }
        if (type == LocalDateTime.class) {
            return type.cast(this.getLocalDateTime(columnIndex));
        }
        if (type == OffsetDateTime.class) {
            return type.cast(this.getOffsetDateTime(columnIndex));
        }
        if (type == OffsetTime.class) {
            return type.cast(this.getOffsetTime(columnIndex));
        }
        if (PGobject.class.isAssignableFrom(type)) {
            Object object;
            if (this.isBinary(columnIndex)) {
                byte[] byteValue = Nullness.castNonNull(this.thisRow, "thisRow").get(columnIndex - 1);
                object = this.connection.getObject(this.getPGType(columnIndex), null, byteValue);
            } else {
                object = this.connection.getObject(this.getPGType(columnIndex), this.getString(columnIndex), null);
            }
            return type.cast(object);
        }
        throw new PSQLException(GT.tr("conversion to {0} from {1} not supported", type, this.getPGType(columnIndex)), PSQLState.INVALID_PARAMETER_VALUE);
    }

    @Override
    public <T> @Nullable T getObject(String columnLabel, Class<T> type) throws SQLException {
        return this.getObject(this.findColumn(columnLabel), type);
    }

    @Override
    public @Nullable Object getObject(String s2, @Nullable Map<String, Class<?>> map) throws SQLException {
        return this.getObjectImpl(s2, map);
    }

    @Override
    public @Nullable Object getObject(@Positive int i, @Nullable Map<String, Class<?>> map) throws SQLException {
        return this.getObjectImpl(i, map);
    }

    @Override
    public void updateObject(@Positive int columnIndex, @Nullable Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateObject");
    }

    @Override
    public void updateObject(String columnLabel, @Nullable Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateObject");
    }

    @Override
    public void updateObject(@Positive int columnIndex, @Nullable Object x, SQLType targetSqlType) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateObject");
    }

    @Override
    public void updateObject(String columnLabel, @Nullable Object x, SQLType targetSqlType) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateObject");
    }

    @Override
    public @Nullable RowId getRowId(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getRowId columnIndex: {0}", columnIndex);
        throw Driver.notImplemented(this.getClass(), "getRowId(int)");
    }

    @Override
    public @Nullable RowId getRowId(String columnName) throws SQLException {
        return this.getRowId(this.findColumn(columnName));
    }

    @Override
    public void updateRowId(@Positive int columnIndex, @Nullable RowId x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateRowId(int, RowId)");
    }

    @Override
    public void updateRowId(String columnName, @Nullable RowId x) throws SQLException {
        this.updateRowId(this.findColumn(columnName), x);
    }

    @Override
    public int getHoldability() throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getHoldability()");
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.rows == null;
    }

    @Override
    public void updateNString(@Positive int columnIndex, @Nullable String nString) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateNString(int, String)");
    }

    @Override
    public void updateNString(String columnName, @Nullable String nString) throws SQLException {
        this.updateNString(this.findColumn(columnName), nString);
    }

    @Override
    public void updateNClob(@Positive int columnIndex, @Nullable NClob nClob) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateNClob(int, NClob)");
    }

    @Override
    public void updateNClob(String columnName, @Nullable NClob nClob) throws SQLException {
        this.updateNClob(this.findColumn(columnName), nClob);
    }

    @Override
    public void updateNClob(@Positive int columnIndex, @Nullable Reader reader) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateNClob(int, Reader)");
    }

    @Override
    public void updateNClob(String columnName, @Nullable Reader reader) throws SQLException {
        this.updateNClob(this.findColumn(columnName), reader);
    }

    @Override
    public void updateNClob(@Positive int columnIndex, @Nullable Reader reader, long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateNClob(int, Reader, long)");
    }

    @Override
    public void updateNClob(String columnName, @Nullable Reader reader, long length) throws SQLException {
        this.updateNClob(this.findColumn(columnName), reader, length);
    }

    @Override
    public @Nullable NClob getNClob(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getNClob columnIndex: {0}", columnIndex);
        throw Driver.notImplemented(this.getClass(), "getNClob(int)");
    }

    @Override
    public @Nullable NClob getNClob(String columnName) throws SQLException {
        return this.getNClob(this.findColumn(columnName));
    }

    @Override
    public void updateBlob(@Positive int columnIndex, @Nullable InputStream inputStream, long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateBlob(int, InputStream, long)");
    }

    @Override
    public void updateBlob(String columnName, @Nullable InputStream inputStream, long length) throws SQLException {
        this.updateBlob(this.findColumn(columnName), inputStream, length);
    }

    @Override
    public void updateBlob(@Positive int columnIndex, @Nullable InputStream inputStream) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateBlob(int, InputStream)");
    }

    @Override
    public void updateBlob(String columnName, @Nullable InputStream inputStream) throws SQLException {
        this.updateBlob(this.findColumn(columnName), inputStream);
    }

    @Override
    public void updateClob(@Positive int columnIndex, @Nullable Reader reader, long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateClob(int, Reader, long)");
    }

    @Override
    public void updateClob(String columnName, @Nullable Reader reader, long length) throws SQLException {
        this.updateClob(this.findColumn(columnName), reader, length);
    }

    @Override
    public void updateClob(@Positive int columnIndex, @Nullable Reader reader) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateClob(int, Reader)");
    }

    @Override
    public void updateClob(String columnName, @Nullable Reader reader) throws SQLException {
        this.updateClob(this.findColumn(columnName), reader);
    }

    @Override
    @Pure
    public @Nullable SQLXML getSQLXML(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getSQLXML columnIndex: {0}", columnIndex);
        String data = this.getString(columnIndex);
        if (data == null) {
            return null;
        }
        return new PgSQLXML(this.connection, data);
    }

    @Override
    public @Nullable SQLXML getSQLXML(String columnName) throws SQLException {
        return this.getSQLXML(this.findColumn(columnName));
    }

    @Override
    public void updateSQLXML(@Positive int columnIndex, @Nullable SQLXML xmlObject) throws SQLException {
        this.updateValue(columnIndex, xmlObject);
    }

    @Override
    public void updateSQLXML(String columnName, @Nullable SQLXML xmlObject) throws SQLException {
        this.updateSQLXML(this.findColumn(columnName), xmlObject);
    }

    @Override
    public @Nullable String getNString(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getNString columnIndex: {0}", columnIndex);
        throw Driver.notImplemented(this.getClass(), "getNString(int)");
    }

    @Override
    public @Nullable String getNString(String columnName) throws SQLException {
        return this.getNString(this.findColumn(columnName));
    }

    @Override
    public @Nullable Reader getNCharacterStream(@Positive int columnIndex) throws SQLException {
        this.connection.getLogger().log(Level.FINEST, "  getNCharacterStream columnIndex: {0}", columnIndex);
        throw Driver.notImplemented(this.getClass(), "getNCharacterStream(int)");
    }

    @Override
    public @Nullable Reader getNCharacterStream(String columnName) throws SQLException {
        return this.getNCharacterStream(this.findColumn(columnName));
    }

    public void updateNCharacterStream(@Positive int columnIndex, @Nullable Reader x, int length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateNCharacterStream(int, Reader, int)");
    }

    public void updateNCharacterStream(String columnName, @Nullable Reader x, int length) throws SQLException {
        this.updateNCharacterStream(this.findColumn(columnName), x, length);
    }

    @Override
    public void updateNCharacterStream(@Positive int columnIndex, @Nullable Reader x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateNCharacterStream(int, Reader)");
    }

    @Override
    public void updateNCharacterStream(String columnName, @Nullable Reader x) throws SQLException {
        this.updateNCharacterStream(this.findColumn(columnName), x);
    }

    @Override
    public void updateNCharacterStream(@Positive int columnIndex, @Nullable Reader x, long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateNCharacterStream(int, Reader, long)");
    }

    @Override
    public void updateNCharacterStream(String columnName, @Nullable Reader x, long length) throws SQLException {
        this.updateNCharacterStream(this.findColumn(columnName), x, length);
    }

    @Override
    public void updateCharacterStream(@Positive int columnIndex, @Nullable Reader reader, long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateCharaceterStream(int, Reader, long)");
    }

    @Override
    public void updateCharacterStream(String columnName, @Nullable Reader reader, long length) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnName), reader, length);
    }

    @Override
    public void updateCharacterStream(@Positive int columnIndex, @Nullable Reader reader) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateCharaceterStream(int, Reader)");
    }

    @Override
    public void updateCharacterStream(String columnName, @Nullable Reader reader) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnName), reader);
    }

    @Override
    public void updateBinaryStream(@Positive int columnIndex, @Nullable InputStream inputStream, long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateBinaryStream(int, InputStream, long)");
    }

    @Override
    public void updateBinaryStream(String columnName, @Nullable InputStream inputStream, long length) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnName), inputStream, length);
    }

    @Override
    public void updateBinaryStream(@Positive int columnIndex, @Nullable InputStream inputStream) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateBinaryStream(int, InputStream)");
    }

    @Override
    public void updateBinaryStream(String columnName, @Nullable InputStream inputStream) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnName), inputStream);
    }

    @Override
    public void updateAsciiStream(@Positive int columnIndex, @Nullable InputStream inputStream, long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateAsciiStream(int, InputStream, long)");
    }

    @Override
    public void updateAsciiStream(String columnName, @Nullable InputStream inputStream, long length) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnName), inputStream, length);
    }

    @Override
    public void updateAsciiStream(@Positive int columnIndex, @Nullable InputStream inputStream) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "updateAsciiStream(int, InputStream)");
    }

    @Override
    public void updateAsciiStream(String columnName, @Nullable InputStream inputStream) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnName), inputStream);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(this.getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(this.getClass())) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }

    private Calendar getDefaultCalendar() {
        if (this.getTimestampUtils().hasFastDefaultTimeZone()) {
            return this.getTimestampUtils().getSharedCalendar(null);
        }
        Calendar sharedCalendar = this.getTimestampUtils().getSharedCalendar(this.defaultTimeZone);
        if (this.defaultTimeZone == null) {
            this.defaultTimeZone = sharedCalendar.getTimeZone();
        }
        return sharedCalendar;
    }

    private TimestampUtils getTimestampUtils() {
        if (this.timestampUtils == null) {
            this.timestampUtils = new TimestampUtils(!this.connection.getQueryExecutor().getIntegerDateTimes(), new QueryExecutorTimeZoneProvider(this.connection.getQueryExecutor()));
        }
        return this.timestampUtils;
    }

    protected PgResultSet upperCaseFieldLabels() {
        for (Field field : this.fields) {
            field.upperCaseLabel();
        }
        return this;
    }

    static class NullObject
    extends PGobject {
        NullObject(String type) {
            this.type = type;
        }

        @Override
        public @Nullable String getValue() {
            return null;
        }
    }

    private class PrimaryKey {
        int index;
        String name;

        PrimaryKey(int index, String name) {
            this.index = index;
            this.name = name;
        }

        @Nullable Object getValue() throws SQLException {
            return PgResultSet.this.getObject(this.index);
        }
    }

    public class CursorResultHandler
    extends ResultHandlerBase {
        @Override
        public void handleResultRows(Query fromQuery, Field[] fields, List<Tuple> tuples, @Nullable ResultCursor cursor) {
            PgResultSet.this.rows = tuples;
            PgResultSet.this.cursor = cursor;
        }

        @Override
        public void handleCommandStatus(String status, long updateCount, long insertOID) {
            this.handleError(new PSQLException(GT.tr("Unexpected command status: {0}.", status), PSQLState.PROTOCOL_VIOLATION));
        }

        @Override
        public void handleCompletion() throws SQLException {
            SQLWarning warning = this.getWarning();
            if (warning != null) {
                PgResultSet.this.addWarning(warning);
            }
            super.handleCompletion();
        }
    }
}

