/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.IntRange;
import org.postgresql.Driver;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.CachedQuery;
import org.postgresql.core.ParameterList;
import org.postgresql.core.Query;
import org.postgresql.core.ServerVersion;
import org.postgresql.core.TypeInfo;
import org.postgresql.core.v3.BatchedQuery;
import org.postgresql.jdbc.ArrayEncoding;
import org.postgresql.jdbc.BooleanTypeUtil;
import org.postgresql.jdbc.PgArray;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.jdbc.PgParameterMetaData;
import org.postgresql.jdbc.PgResultSet;
import org.postgresql.jdbc.PgSQLXML;
import org.postgresql.jdbc.PgStatement;
import org.postgresql.jdbc.PreferQueryMode;
import org.postgresql.jdbc.ResultWrapper;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.postgresql.util.ByteConverter;
import org.postgresql.util.ByteStreamWriter;
import org.postgresql.util.GT;
import org.postgresql.util.HStoreConverter;
import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGTime;
import org.postgresql.util.PGTimestamp;
import org.postgresql.util.PGobject;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.ReaderInputStream;
import org.postgresql.util.internal.Nullness;

class PgPreparedStatement
extends PgStatement
implements PreparedStatement {
    protected final CachedQuery preparedQuery;
    protected final ParameterList preparedParameters;
    private @Nullable TimeZone defaultTimeZone;

    PgPreparedStatement(PgConnection connection, String sql, int rsType, int rsConcurrency, int rsHoldability) throws SQLException {
        this(connection, connection.borrowQuery(sql), rsType, rsConcurrency, rsHoldability);
    }

    PgPreparedStatement(PgConnection connection, CachedQuery query, int rsType, int rsConcurrency, int rsHoldability) throws SQLException {
        super(connection, rsType, rsConcurrency, rsHoldability);
        this.preparedQuery = query;
        this.preparedParameters = this.preparedQuery.query.createParameterList();
        int parameterCount = this.preparedParameters.getParameterCount();
        int maxSupportedParameters = this.maximumNumberOfParameters();
        if (parameterCount > maxSupportedParameters) {
            throw new PSQLException(GT.tr("PreparedStatement can have at most {0} parameters. Please consider using arrays, or splitting the query in several ones, or using COPY. Given query has {1} parameters", maxSupportedParameters, parameterCount), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.setPoolable(true);
    }

    final int maximumNumberOfParameters() {
        return this.connection.getPreferQueryMode() == PreferQueryMode.SIMPLE ? Integer.MAX_VALUE : 65535;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        throw new PSQLException(GT.tr("Can''t use query methods that take a query string on a PreparedStatement.", new Object[0]), PSQLState.WRONG_OBJECT_TYPE);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ResultSet executeQuery() throws SQLException {
        PgPreparedStatement pgPreparedStatement = this;
        synchronized (pgPreparedStatement) {
            if (!this.executeWithFlags(0)) {
                throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
            }
            return this.getSingleResultSet();
        }
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        throw new PSQLException(GT.tr("Can''t use query methods that take a query string on a PreparedStatement.", new Object[0]), PSQLState.WRONG_OBJECT_TYPE);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int executeUpdate() throws SQLException {
        PgPreparedStatement pgPreparedStatement = this;
        synchronized (pgPreparedStatement) {
            this.executeWithFlags(4);
            this.checkNoResultUpdate();
            return this.getUpdateCount();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long executeLargeUpdate() throws SQLException {
        PgPreparedStatement pgPreparedStatement = this;
        synchronized (pgPreparedStatement) {
            this.executeWithFlags(4);
            this.checkNoResultUpdate();
            return this.getLargeUpdateCount();
        }
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        throw new PSQLException(GT.tr("Can''t use query methods that take a query string on a PreparedStatement.", new Object[0]), PSQLState.WRONG_OBJECT_TYPE);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean execute() throws SQLException {
        PgPreparedStatement pgPreparedStatement = this;
        synchronized (pgPreparedStatement) {
            return this.executeWithFlags(0);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean executeWithFlags(int flags) throws SQLException {
        try {
            PgPreparedStatement pgPreparedStatement = this;
            synchronized (pgPreparedStatement) {
                this.checkClosed();
                if (this.connection.getPreferQueryMode() == PreferQueryMode.SIMPLE) {
                    flags |= 0x400;
                }
                this.execute(this.preparedQuery, this.preparedParameters, flags);
                this.checkClosed();
                boolean bl = this.result != null && this.result.getResultSet() != null;
                return bl;
            }
        }
        finally {
            this.defaultTimeZone = null;
        }
    }

    @Override
    protected boolean isOneShotQuery(@Nullable CachedQuery cachedQuery) {
        if (cachedQuery == null) {
            cachedQuery = this.preparedQuery;
        }
        return super.isOneShotQuery(cachedQuery);
    }

    @Override
    public void closeImpl() throws SQLException {
        if (this.preparedQuery != null) {
            ((PgConnection)this.connection).releaseQuery(this.preparedQuery);
        }
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        int oid;
        this.checkClosed();
        if (parameterIndex < 1 || parameterIndex > this.preparedParameters.getParameterCount()) {
            throw new PSQLException(GT.tr("The column index is out of range: {0}, number of columns: {1}.", parameterIndex, this.preparedParameters.getParameterCount()), PSQLState.INVALID_PARAMETER_VALUE);
        }
        switch (sqlType) {
            case 2009: {
                oid = 142;
                break;
            }
            case 4: {
                oid = 23;
                break;
            }
            case -6: 
            case 5: {
                oid = 21;
                break;
            }
            case -5: {
                oid = 20;
                break;
            }
            case 7: {
                oid = 700;
                break;
            }
            case 6: 
            case 8: {
                oid = 701;
                break;
            }
            case 2: 
            case 3: {
                oid = 1700;
                break;
            }
            case 1: {
                oid = 1042;
                break;
            }
            case -1: 
            case 12: {
                oid = this.connection.getStringVarcharFlag() ? 1043 : 0;
                break;
            }
            case 91: {
                oid = 1082;
                break;
            }
            case 92: 
            case 93: 
            case 2013: 
            case 2014: {
                oid = 0;
                break;
            }
            case -7: 
            case 16: {
                oid = 16;
                break;
            }
            case -4: 
            case -3: 
            case -2: {
                oid = 17;
                break;
            }
            case 2004: 
            case 2005: {
                oid = 26;
                break;
            }
            case 2012: {
                oid = 1790;
                break;
            }
            case 0: 
            case 1111: 
            case 2001: 
            case 2002: 
            case 2003: {
                oid = 0;
                break;
            }
            default: {
                throw new PSQLException(GT.tr("Unknown Types value.", new Object[0]), PSQLState.INVALID_PARAMETER_TYPE);
            }
        }
        this.preparedParameters.setNull(parameterIndex, oid);
    }

    @Override
    public void setBoolean(@Positive int parameterIndex, boolean x) throws SQLException {
        this.checkClosed();
        this.bindLiteral(parameterIndex, x ? "TRUE" : "FALSE", 16);
    }

    @Override
    public void setByte(@Positive int parameterIndex, byte x) throws SQLException {
        this.setShort(parameterIndex, x);
    }

    @Override
    public void setShort(@Positive int parameterIndex, short x) throws SQLException {
        this.checkClosed();
        if (this.connection.binaryTransferSend(21)) {
            byte[] val = new byte[2];
            ByteConverter.int2(val, 0, x);
            this.bindBytes(parameterIndex, val, 21);
            return;
        }
        this.bindLiteral(parameterIndex, Integer.toString(x), 21);
    }

    @Override
    public void setInt(@Positive int parameterIndex, int x) throws SQLException {
        this.checkClosed();
        if (this.connection.binaryTransferSend(23)) {
            byte[] val = new byte[4];
            ByteConverter.int4(val, 0, x);
            this.bindBytes(parameterIndex, val, 23);
            return;
        }
        this.bindLiteral(parameterIndex, Integer.toString(x), 23);
    }

    @Override
    public void setLong(@Positive int parameterIndex, long x) throws SQLException {
        this.checkClosed();
        if (this.connection.binaryTransferSend(20)) {
            byte[] val = new byte[8];
            ByteConverter.int8(val, 0, x);
            this.bindBytes(parameterIndex, val, 20);
            return;
        }
        this.bindLiteral(parameterIndex, Long.toString(x), 20);
    }

    @Override
    public void setFloat(@Positive int parameterIndex, float x) throws SQLException {
        this.checkClosed();
        if (this.connection.binaryTransferSend(700)) {
            byte[] val = new byte[4];
            ByteConverter.float4(val, 0, x);
            this.bindBytes(parameterIndex, val, 700);
            return;
        }
        this.bindLiteral(parameterIndex, Float.toString(x), 701);
    }

    @Override
    public void setDouble(@Positive int parameterIndex, double x) throws SQLException {
        this.checkClosed();
        if (this.connection.binaryTransferSend(701)) {
            byte[] val = new byte[8];
            ByteConverter.float8(val, 0, x);
            this.bindBytes(parameterIndex, val, 701);
            return;
        }
        this.bindLiteral(parameterIndex, Double.toString(x), 701);
    }

    @Override
    public void setBigDecimal(@Positive int parameterIndex, @Nullable BigDecimal x) throws SQLException {
        if (x != null && this.connection.binaryTransferSend(1700)) {
            byte[] bytes = ByteConverter.numeric(x);
            this.bindBytes(parameterIndex, bytes, 1700);
            return;
        }
        this.setNumber(parameterIndex, x);
    }

    @Override
    public void setString(@Positive int parameterIndex, @Nullable String x) throws SQLException {
        this.checkClosed();
        this.setString(parameterIndex, x, this.getStringType());
    }

    private int getStringType() {
        return this.connection.getStringVarcharFlag() ? 1043 : 0;
    }

    protected void setString(@Positive int parameterIndex, @Nullable String x, int oid) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.preparedParameters.setNull(parameterIndex, oid);
        } else {
            this.bindString(parameterIndex, x, oid);
        }
    }

    @Override
    public void setBytes(@Positive int parameterIndex, byte @Nullable [] x) throws SQLException {
        this.checkClosed();
        if (null == x) {
            this.setNull(parameterIndex, -3);
            return;
        }
        byte[] copy = new byte[x.length];
        System.arraycopy(x, 0, copy, 0, x.length);
        this.preparedParameters.setBytea(parameterIndex, copy, 0, x.length);
    }

    private void setByteStreamWriter(@Positive int parameterIndex, ByteStreamWriter x) throws SQLException {
        this.preparedParameters.setBytea(parameterIndex, x);
    }

    @Override
    public void setDate(@Positive int parameterIndex, @Nullable java.sql.Date x) throws SQLException {
        this.setDate(parameterIndex, x, null);
    }

    @Override
    public void setTime(@Positive int parameterIndex, @Nullable Time x) throws SQLException {
        this.setTime(parameterIndex, x, null);
    }

    @Override
    public void setTimestamp(@Positive int parameterIndex, @Nullable Timestamp x) throws SQLException {
        this.setTimestamp(parameterIndex, x, null);
    }

    private void setCharacterStreamPost71(@Positive int parameterIndex, @Nullable InputStream x, int length, String encoding) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 12);
            return;
        }
        if (length < 0) {
            throw new PSQLException(GT.tr("Invalid stream length {0}.", length), PSQLState.INVALID_PARAMETER_VALUE);
        }
        try {
            int n;
            InputStreamReader inStream = new InputStreamReader(x, encoding);
            char[] chars = new char[length];
            int charsRead = 0;
            while ((n = inStream.read(chars, charsRead, length - charsRead)) != -1 && (charsRead += n) != length) {
            }
            this.setString(parameterIndex, new String(chars, 0, charsRead), 1043);
        }
        catch (UnsupportedEncodingException uee) {
            throw new PSQLException(GT.tr("The JVM claims not to support the {0} encoding.", encoding), PSQLState.UNEXPECTED_ERROR, (Throwable)uee);
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("Provided InputStream failed.", new Object[0]), PSQLState.UNEXPECTED_ERROR, (Throwable)ioe);
        }
    }

    @Override
    public void setAsciiStream(@Positive int parameterIndex, @Nullable InputStream x, @NonNegative int length) throws SQLException {
        this.checkClosed();
        this.setCharacterStreamPost71(parameterIndex, x, length, "ASCII");
    }

    @Override
    public void setUnicodeStream(@Positive int parameterIndex, @Nullable InputStream x, @NonNegative int length) throws SQLException {
        this.checkClosed();
        this.setCharacterStreamPost71(parameterIndex, x, length, "UTF-8");
    }

    @Override
    public void setBinaryStream(@Positive int parameterIndex, @Nullable InputStream x, @NonNegative int length) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(parameterIndex, -3);
            return;
        }
        if (length < 0) {
            throw new PSQLException(GT.tr("Invalid stream length {0}.", length), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.preparedParameters.setBytea(parameterIndex, x, length);
    }

    @Override
    public void clearParameters() throws SQLException {
        this.preparedParameters.clear();
    }

    private void setPGobject(@Positive int parameterIndex, PGobject x) throws SQLException {
        String typename = x.getType();
        int oid = this.connection.getTypeInfo().getPGType(typename);
        if (oid == 0) {
            throw new PSQLException(GT.tr("Unknown type {0}.", typename), PSQLState.INVALID_PARAMETER_TYPE);
        }
        if (x instanceof PGBinaryObject && this.connection.binaryTransferSend(oid)) {
            PGBinaryObject binObj = (PGBinaryObject)((Object)x);
            int length = binObj.lengthInBytes();
            if (length == 0) {
                this.preparedParameters.setNull(parameterIndex, oid);
                return;
            }
            byte[] data = new byte[length];
            binObj.toBytes(data, 0);
            this.bindBytes(parameterIndex, data, oid);
        } else {
            this.setString(parameterIndex, x.getValue(), oid);
        }
    }

    private void setMap(@Positive int parameterIndex, Map<?, ?> x) throws SQLException {
        int oid = this.connection.getTypeInfo().getPGType("hstore");
        if (oid == 0) {
            throw new PSQLException(GT.tr("No hstore extension installed.", new Object[0]), PSQLState.INVALID_PARAMETER_TYPE);
        }
        if (this.connection.binaryTransferSend(oid)) {
            byte[] data = HStoreConverter.toBytes(x, this.connection.getEncoding());
            this.bindBytes(parameterIndex, data, oid);
        } else {
            this.setString(parameterIndex, HStoreConverter.toString(x), oid);
        }
    }

    private void setNumber(@Positive int parameterIndex, @Nullable Number x) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(parameterIndex, 3);
        } else {
            this.bindLiteral(parameterIndex, x.toString(), 1700);
        }
    }

    @Override
    public void setObject(@Positive int parameterIndex, @Nullable Object in, int targetSqlType, int scale) throws SQLException {
        this.checkClosed();
        if (in == null) {
            this.setNull(parameterIndex, targetSqlType);
            return;
        }
        if (targetSqlType == 1111 && in instanceof UUID && this.connection.haveMinimumServerVersion(ServerVersion.v8_3)) {
            this.setUuid(parameterIndex, (UUID)in);
            return;
        }
        switch (targetSqlType) {
            case 2009: {
                if (in instanceof SQLXML) {
                    this.setSQLXML(parameterIndex, (SQLXML)in);
                    break;
                }
                this.setSQLXML(parameterIndex, new PgSQLXML(this.connection, in.toString()));
                break;
            }
            case 4: {
                this.setInt(parameterIndex, PgPreparedStatement.castToInt(in));
                break;
            }
            case -6: 
            case 5: {
                this.setShort(parameterIndex, PgPreparedStatement.castToShort(in));
                break;
            }
            case -5: {
                this.setLong(parameterIndex, PgPreparedStatement.castToLong(in));
                break;
            }
            case 7: {
                this.setFloat(parameterIndex, PgPreparedStatement.castToFloat(in));
                break;
            }
            case 6: 
            case 8: {
                this.setDouble(parameterIndex, PgPreparedStatement.castToDouble(in));
                break;
            }
            case 2: 
            case 3: {
                this.setBigDecimal(parameterIndex, PgPreparedStatement.castToBigDecimal(in, scale));
                break;
            }
            case 1: {
                this.setString(parameterIndex, PgPreparedStatement.castToString(in), 1042);
                break;
            }
            case 12: {
                this.setString(parameterIndex, PgPreparedStatement.castToString(in), this.getStringType());
                break;
            }
            case -1: {
                if (in instanceof InputStream) {
                    this.preparedParameters.setText(parameterIndex, (InputStream)in);
                    break;
                }
                this.setString(parameterIndex, PgPreparedStatement.castToString(in), this.getStringType());
                break;
            }
            case 91: {
                java.sql.Date tmpd;
                if (in instanceof java.sql.Date) {
                    this.setDate(parameterIndex, (java.sql.Date)in);
                    break;
                }
                if (in instanceof Date) {
                    tmpd = new java.sql.Date(((Date)in).getTime());
                } else {
                    if (in instanceof LocalDate) {
                        this.setDate(parameterIndex, (LocalDate)in);
                        break;
                    }
                    tmpd = this.getTimestampUtils().toDate(this.getDefaultCalendar(), in.toString());
                }
                this.setDate(parameterIndex, tmpd);
                break;
            }
            case 92: {
                Time tmpt;
                if (in instanceof Time) {
                    this.setTime(parameterIndex, (Time)in);
                    break;
                }
                if (in instanceof Date) {
                    tmpt = new Time(((Date)in).getTime());
                } else {
                    if (in instanceof LocalTime) {
                        this.setTime(parameterIndex, (LocalTime)in);
                        break;
                    }
                    if (in instanceof OffsetTime) {
                        this.setTime(parameterIndex, (OffsetTime)in);
                        break;
                    }
                    tmpt = this.getTimestampUtils().toTime(this.getDefaultCalendar(), in.toString());
                }
                this.setTime(parameterIndex, tmpt);
                break;
            }
            case 93: {
                Timestamp tmpts;
                if (in instanceof PGTimestamp) {
                    this.setObject(parameterIndex, in);
                    break;
                }
                if (in instanceof Timestamp) {
                    this.setTimestamp(parameterIndex, (Timestamp)in);
                    break;
                }
                if (in instanceof Date) {
                    tmpts = new Timestamp(((Date)in).getTime());
                } else {
                    if (in instanceof LocalDateTime) {
                        this.setTimestamp(parameterIndex, (LocalDateTime)in);
                        break;
                    }
                    tmpts = this.getTimestampUtils().toTimestamp(this.getDefaultCalendar(), in.toString());
                }
                this.setTimestamp(parameterIndex, tmpts);
                break;
            }
            case 2014: {
                if (in instanceof OffsetDateTime) {
                    this.setTimestamp(parameterIndex, (OffsetDateTime)in);
                    break;
                }
                if (in instanceof PGTimestamp) {
                    this.setObject(parameterIndex, in);
                    break;
                }
                throw new PSQLException(GT.tr("Cannot cast an instance of {0} to type {1}", in.getClass().getName(), "Types.TIMESTAMP_WITH_TIMEZONE"), PSQLState.INVALID_PARAMETER_TYPE);
            }
            case -7: 
            case 16: {
                this.setBoolean(parameterIndex, BooleanTypeUtil.castToBoolean(in));
                break;
            }
            case -4: 
            case -3: 
            case -2: {
                this.setObject(parameterIndex, in);
                break;
            }
            case 2004: {
                if (in instanceof Blob) {
                    this.setBlob(parameterIndex, (Blob)in);
                    break;
                }
                if (in instanceof InputStream) {
                    long oid = this.createBlob(parameterIndex, (InputStream)in, -1L);
                    this.setLong(parameterIndex, oid);
                    break;
                }
                throw new PSQLException(GT.tr("Cannot cast an instance of {0} to type {1}", in.getClass().getName(), "Types.BLOB"), PSQLState.INVALID_PARAMETER_TYPE);
            }
            case 2005: {
                if (in instanceof Clob) {
                    this.setClob(parameterIndex, (Clob)in);
                    break;
                }
                throw new PSQLException(GT.tr("Cannot cast an instance of {0} to type {1}", in.getClass().getName(), "Types.CLOB"), PSQLState.INVALID_PARAMETER_TYPE);
            }
            case 2003: {
                if (in instanceof Array) {
                    this.setArray(parameterIndex, (Array)in);
                    break;
                }
                try {
                    this.setObjectArray(parameterIndex, in);
                    break;
                }
                catch (Exception e) {
                    throw new PSQLException(GT.tr("Cannot cast an instance of {0} to type {1}", in.getClass().getName(), "Types.ARRAY"), PSQLState.INVALID_PARAMETER_TYPE, (Throwable)e);
                }
            }
            case 2001: {
                this.bindString(parameterIndex, in.toString(), 0);
                break;
            }
            case 1111: {
                if (in instanceof PGobject) {
                    this.setPGobject(parameterIndex, (PGobject)in);
                    break;
                }
                if (in instanceof Map) {
                    this.setMap(parameterIndex, (Map)in);
                    break;
                }
                this.bindString(parameterIndex, in.toString(), 0);
                break;
            }
            default: {
                throw new PSQLException(GT.tr("Unsupported Types value: {0}", targetSqlType), PSQLState.INVALID_PARAMETER_TYPE);
            }
        }
    }

    private Class<?> getArrayType(Class<?> type) {
        Class<?> subType = type.getComponentType();
        while (subType != null) {
            type = subType;
            subType = type.getComponentType();
        }
        return type;
    }

    private <A> void setObjectArray(int parameterIndex, A in) throws SQLException {
        ArrayEncoding.ArrayEncoder<A> arraySupport = ArrayEncoding.getArrayEncoder(in);
        TypeInfo typeInfo = this.connection.getTypeInfo();
        int oid = arraySupport.getDefaultArrayTypeOid();
        if (arraySupport.supportBinaryRepresentation(oid) && this.connection.getPreferQueryMode() != PreferQueryMode.SIMPLE) {
            this.bindBytes(parameterIndex, arraySupport.toBinaryRepresentation(this.connection, in, oid), oid);
        } else {
            Class<?> arrayType;
            if (oid == 0 && (oid = typeInfo.getJavaArrayType((arrayType = this.getArrayType(in.getClass())).getName())) == 0) {
                throw new SQLFeatureNotSupportedException();
            }
            int baseOid = typeInfo.getPGArrayElement(oid);
            String baseType = Nullness.castNonNull(typeInfo.getPGType(baseOid));
            Array array = this.getPGConnection().createArrayOf(baseType, in);
            this.setArray(parameterIndex, array);
        }
    }

    private static String asString(Clob in) throws SQLException {
        return in.getSubString(1L, (int)in.length());
    }

    private static int castToInt(Object in) throws SQLException {
        try {
            if (in instanceof String) {
                return Integer.parseInt((String)in);
            }
            if (in instanceof Number) {
                return ((Number)in).intValue();
            }
            if (in instanceof Date) {
                return (int)((Date)in).getTime();
            }
            if (in instanceof Boolean) {
                return (Boolean)in != false ? 1 : 0;
            }
            if (in instanceof Clob) {
                return Integer.parseInt(PgPreparedStatement.asString((Clob)in));
            }
            if (in instanceof Character) {
                return Integer.parseInt(in.toString());
            }
        }
        catch (Exception e) {
            throw PgPreparedStatement.cannotCastException(in.getClass().getName(), "int", e);
        }
        throw PgPreparedStatement.cannotCastException(in.getClass().getName(), "int");
    }

    private static short castToShort(Object in) throws SQLException {
        try {
            if (in instanceof String) {
                return Short.parseShort((String)in);
            }
            if (in instanceof Number) {
                return ((Number)in).shortValue();
            }
            if (in instanceof Date) {
                return (short)((Date)in).getTime();
            }
            if (in instanceof Boolean) {
                return (Boolean)in != false ? (short)1 : 0;
            }
            if (in instanceof Clob) {
                return Short.parseShort(PgPreparedStatement.asString((Clob)in));
            }
            if (in instanceof Character) {
                return Short.parseShort(in.toString());
            }
        }
        catch (Exception e) {
            throw PgPreparedStatement.cannotCastException(in.getClass().getName(), "short", e);
        }
        throw PgPreparedStatement.cannotCastException(in.getClass().getName(), "short");
    }

    private static long castToLong(Object in) throws SQLException {
        try {
            if (in instanceof String) {
                return Long.parseLong((String)in);
            }
            if (in instanceof Number) {
                return ((Number)in).longValue();
            }
            if (in instanceof Date) {
                return ((Date)in).getTime();
            }
            if (in instanceof Boolean) {
                return (Boolean)in != false ? 1L : 0L;
            }
            if (in instanceof Clob) {
                return Long.parseLong(PgPreparedStatement.asString((Clob)in));
            }
            if (in instanceof Character) {
                return Long.parseLong(in.toString());
            }
        }
        catch (Exception e) {
            throw PgPreparedStatement.cannotCastException(in.getClass().getName(), "long", e);
        }
        throw PgPreparedStatement.cannotCastException(in.getClass().getName(), "long");
    }

    private static float castToFloat(Object in) throws SQLException {
        try {
            if (in instanceof String) {
                return Float.parseFloat((String)in);
            }
            if (in instanceof Number) {
                return ((Number)in).floatValue();
            }
            if (in instanceof Date) {
                return ((Date)in).getTime();
            }
            if (in instanceof Boolean) {
                return (Boolean)in != false ? 1.0f : 0.0f;
            }
            if (in instanceof Clob) {
                return Float.parseFloat(PgPreparedStatement.asString((Clob)in));
            }
            if (in instanceof Character) {
                return Float.parseFloat(in.toString());
            }
        }
        catch (Exception e) {
            throw PgPreparedStatement.cannotCastException(in.getClass().getName(), "float", e);
        }
        throw PgPreparedStatement.cannotCastException(in.getClass().getName(), "float");
    }

    private static double castToDouble(Object in) throws SQLException {
        try {
            if (in instanceof String) {
                return Double.parseDouble((String)in);
            }
            if (in instanceof Number) {
                return ((Number)in).doubleValue();
            }
            if (in instanceof Date) {
                return ((Date)in).getTime();
            }
            if (in instanceof Boolean) {
                return (Boolean)in != false ? 1.0 : 0.0;
            }
            if (in instanceof Clob) {
                return Double.parseDouble(PgPreparedStatement.asString((Clob)in));
            }
            if (in instanceof Character) {
                return Double.parseDouble(in.toString());
            }
        }
        catch (Exception e) {
            throw PgPreparedStatement.cannotCastException(in.getClass().getName(), "double", e);
        }
        throw PgPreparedStatement.cannotCastException(in.getClass().getName(), "double");
    }

    private static BigDecimal castToBigDecimal(Object in, int scale) throws SQLException {
        try {
            BigDecimal rc = null;
            if (in instanceof String) {
                rc = new BigDecimal((String)in);
            } else if (in instanceof BigDecimal) {
                rc = (BigDecimal)in;
            } else if (in instanceof BigInteger) {
                rc = new BigDecimal((BigInteger)in);
            } else if (in instanceof Long || in instanceof Integer || in instanceof Short || in instanceof Byte) {
                rc = BigDecimal.valueOf(((Number)in).longValue());
            } else if (in instanceof Double || in instanceof Float) {
                rc = BigDecimal.valueOf(((Number)in).doubleValue());
            } else if (in instanceof Date) {
                rc = BigDecimal.valueOf(((Date)in).getTime());
            } else if (in instanceof Boolean) {
                rc = (Boolean)in != false ? BigDecimal.ONE : BigDecimal.ZERO;
            } else if (in instanceof Clob) {
                rc = new BigDecimal(PgPreparedStatement.asString((Clob)in));
            } else if (in instanceof Character) {
                rc = new BigDecimal(new char[]{((Character)in).charValue()});
            }
            if (rc != null) {
                if (scale >= 0) {
                    rc = rc.setScale(scale, RoundingMode.HALF_UP);
                }
                return rc;
            }
        }
        catch (Exception e) {
            throw PgPreparedStatement.cannotCastException(in.getClass().getName(), "BigDecimal", e);
        }
        throw PgPreparedStatement.cannotCastException(in.getClass().getName(), "BigDecimal");
    }

    private static String castToString(Object in) throws SQLException {
        try {
            if (in instanceof String) {
                return (String)in;
            }
            if (in instanceof Clob) {
                return PgPreparedStatement.asString((Clob)in);
            }
            return in.toString();
        }
        catch (Exception e) {
            throw PgPreparedStatement.cannotCastException(in.getClass().getName(), "String", e);
        }
    }

    private static PSQLException cannotCastException(String fromType, String toType) {
        return PgPreparedStatement.cannotCastException(fromType, toType, null);
    }

    private static PSQLException cannotCastException(String fromType, String toType, @Nullable Exception cause) {
        return new PSQLException(GT.tr("Cannot convert an instance of {0} to type {1}", fromType, toType), PSQLState.INVALID_PARAMETER_TYPE, (Throwable)cause);
    }

    @Override
    public void setObject(@Positive int parameterIndex, @Nullable Object x, int targetSqlType) throws SQLException {
        this.setObject(parameterIndex, x, targetSqlType, -1);
    }

    @Override
    public void setObject(@Positive int parameterIndex, @Nullable Object x) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(parameterIndex, 1111);
        } else if (x instanceof UUID && this.connection.haveMinimumServerVersion(ServerVersion.v8_3)) {
            this.setUuid(parameterIndex, (UUID)x);
        } else if (x instanceof SQLXML) {
            this.setSQLXML(parameterIndex, (SQLXML)x);
        } else if (x instanceof String) {
            this.setString(parameterIndex, (String)x);
        } else if (x instanceof BigDecimal) {
            this.setBigDecimal(parameterIndex, (BigDecimal)x);
        } else if (x instanceof Short) {
            this.setShort(parameterIndex, (Short)x);
        } else if (x instanceof Integer) {
            this.setInt(parameterIndex, (Integer)x);
        } else if (x instanceof Long) {
            this.setLong(parameterIndex, (Long)x);
        } else if (x instanceof Float) {
            this.setFloat(parameterIndex, ((Float)x).floatValue());
        } else if (x instanceof Double) {
            this.setDouble(parameterIndex, (Double)x);
        } else if (x instanceof byte[]) {
            this.setBytes(parameterIndex, (byte[])x);
        } else if (x instanceof ByteStreamWriter) {
            this.setByteStreamWriter(parameterIndex, (ByteStreamWriter)x);
        } else if (x instanceof java.sql.Date) {
            this.setDate(parameterIndex, (java.sql.Date)x);
        } else if (x instanceof Time) {
            this.setTime(parameterIndex, (Time)x);
        } else if (x instanceof Timestamp) {
            this.setTimestamp(parameterIndex, (Timestamp)x);
        } else if (x instanceof Boolean) {
            this.setBoolean(parameterIndex, (Boolean)x);
        } else if (x instanceof Byte) {
            this.setByte(parameterIndex, (Byte)x);
        } else if (x instanceof Blob) {
            this.setBlob(parameterIndex, (Blob)x);
        } else if (x instanceof Clob) {
            this.setClob(parameterIndex, (Clob)x);
        } else if (x instanceof Array) {
            this.setArray(parameterIndex, (Array)x);
        } else if (x instanceof PGobject) {
            this.setPGobject(parameterIndex, (PGobject)x);
        } else if (x instanceof Character) {
            this.setString(parameterIndex, ((Character)x).toString());
        } else if (x instanceof LocalDate) {
            this.setDate(parameterIndex, (LocalDate)x);
        } else if (x instanceof LocalTime) {
            this.setTime(parameterIndex, (LocalTime)x);
        } else if (x instanceof OffsetTime) {
            this.setTime(parameterIndex, (OffsetTime)x);
        } else if (x instanceof LocalDateTime) {
            this.setTimestamp(parameterIndex, (LocalDateTime)x);
        } else if (x instanceof OffsetDateTime) {
            this.setTimestamp(parameterIndex, (OffsetDateTime)x);
        } else if (x instanceof Map) {
            this.setMap(parameterIndex, (Map)x);
        } else if (x instanceof Number) {
            this.setNumber(parameterIndex, (Number)x);
        } else if (x.getClass().isArray()) {
            try {
                this.setObjectArray(parameterIndex, x);
            }
            catch (Exception e) {
                throw new PSQLException(GT.tr("Cannot cast an instance of {0} to type {1}", x.getClass().getName(), "Types.ARRAY"), PSQLState.INVALID_PARAMETER_TYPE, (Throwable)e);
            }
        } else {
            throw new PSQLException(GT.tr("Can''t infer the SQL type to use for an instance of {0}. Use setObject() with an explicit Types value to specify the type to use.", x.getClass().getName()), PSQLState.INVALID_PARAMETER_TYPE);
        }
    }

    public String toString() {
        if (this.preparedQuery == null) {
            return super.toString();
        }
        return this.preparedQuery.query.toString(this.preparedParameters);
    }

    protected void bindLiteral(@Positive int paramIndex, String s2, int oid) throws SQLException {
        this.preparedParameters.setLiteralParameter(paramIndex, s2, oid);
    }

    protected void bindBytes(@Positive int paramIndex, byte[] b, int oid) throws SQLException {
        this.preparedParameters.setBinaryParameter(paramIndex, b, oid);
    }

    private void bindString(@Positive int paramIndex, String s2, int oid) throws SQLException {
        this.preparedParameters.setStringParameter(paramIndex, s2, oid);
    }

    @Override
    public boolean isUseServerPrepare() {
        return this.preparedQuery != null && this.mPrepareThreshold != 0 && this.preparedQuery.getExecuteCount() + 1 >= this.mPrepareThreshold;
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        this.checkClosed();
        throw new PSQLException(GT.tr("Can''t use query methods that take a query string on a PreparedStatement.", new Object[0]), PSQLState.WRONG_OBJECT_TYPE);
    }

    @Override
    public void addBatch() throws SQLException {
        ArrayList<ParameterList> batchParameters;
        this.checkClosed();
        ArrayList<Query> batchStatements = this.batchStatements;
        if (batchStatements == null) {
            this.batchStatements = batchStatements = new ArrayList<Query>();
        }
        if ((batchParameters = this.batchParameters) == null) {
            this.batchParameters = batchParameters = new ArrayList<ParameterList>();
        }
        batchParameters.add(this.preparedParameters.copy());
        Query query = this.preparedQuery.query;
        if (!(query instanceof BatchedQuery) || batchStatements.isEmpty()) {
            batchStatements.add(query);
        }
    }

    @Override
    public @Nullable ResultSetMetaData getMetaData() throws SQLException {
        this.checkClosed();
        ResultSet rs = this.getResultSet();
        if (rs == null || ((PgResultSet)rs).isResultSetClosed()) {
            int flags = 49;
            PgStatement.StatementResultHandler handler = new PgStatement.StatementResultHandler(this);
            this.connection.getQueryExecutor().execute(this.preparedQuery.query, this.preparedParameters, handler, 0, 0, flags);
            ResultWrapper wrapper = handler.getResults();
            if (wrapper != null) {
                rs = wrapper.getResultSet();
            }
        }
        if (rs != null) {
            return rs.getMetaData();
        }
        return null;
    }

    @Override
    public void setArray(int i, @Nullable Array x) throws SQLException {
        PgArray arr;
        byte[] bytes;
        this.checkClosed();
        if (null == x) {
            this.setNull(i, 2003);
            return;
        }
        String typename = x.getBaseTypeName();
        int oid = this.connection.getTypeInfo().getPGArrayType(typename);
        if (oid == 0) {
            throw new PSQLException(GT.tr("Unknown type {0}.", typename), PSQLState.INVALID_PARAMETER_TYPE);
        }
        if (x instanceof PgArray && (bytes = (arr = (PgArray)x).toBytes()) != null) {
            this.bindBytes(i, bytes, oid);
            return;
        }
        this.setString(i, x.toString(), oid);
    }

    protected long createBlob(int i, InputStream inputStream, @NonNegative long length) throws SQLException {
        LargeObjectManager lom = this.connection.getLargeObjectAPI();
        long oid = lom.createLO();
        LargeObject lob = lom.open(oid);
        OutputStream outputStream = lob.getOutputStream();
        byte[] buf = new byte[4096];
        try {
            long remaining = length > 0L ? length : Long.MAX_VALUE;
            int numRead = inputStream.read(buf, 0, length > 0L && remaining < (long)buf.length ? (int)remaining : buf.length);
            while (numRead != -1 && remaining > 0L) {
                outputStream.write(buf, 0, numRead);
                numRead = inputStream.read(buf, 0, length > 0L && remaining < (long)buf.length ? (int)(remaining -= (long)numRead) : buf.length);
            }
        }
        catch (IOException se) {
            throw new PSQLException(GT.tr("Unexpected error writing large object to database.", new Object[0]), PSQLState.UNEXPECTED_ERROR, (Throwable)se);
        }
        finally {
            try {
                outputStream.close();
            }
            catch (Exception exception) {}
        }
        return oid;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setBlob(@Positive int i, @Nullable Blob x) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(i, 2004);
            return;
        }
        InputStream inStream = x.getBinaryStream();
        try {
            long oid = this.createBlob(i, inStream, x.length());
            this.setLong(i, oid);
        }
        finally {
            try {
                inStream.close();
            }
            catch (Exception exception) {}
        }
    }

    private String readerToString(Reader value, int maxLength) throws SQLException {
        try {
            int bufferSize = Math.min(maxLength, 1024);
            StringBuilder v = new StringBuilder(bufferSize);
            char[] buf = new char[bufferSize];
            int nRead = 0;
            while (nRead > -1 && v.length() < maxLength) {
                nRead = value.read(buf, 0, Math.min(bufferSize, maxLength - v.length()));
                if (nRead <= 0) continue;
                v.append(buf, 0, nRead);
            }
            return v.toString();
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("Provided Reader failed.", new Object[0]), PSQLState.UNEXPECTED_ERROR, (Throwable)ioe);
        }
    }

    @Override
    public void setCharacterStream(@Positive int i, @Nullable Reader x, @NonNegative int length) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(i, 12);
            return;
        }
        if (length < 0) {
            throw new PSQLException(GT.tr("Invalid stream length {0}.", length), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.setString(i, this.readerToString(x, length));
    }

    @Override
    public void setClob(@Positive int i, @Nullable Clob x) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(i, 2005);
            return;
        }
        Reader inStream = x.getCharacterStream();
        int length = (int)x.length();
        LargeObjectManager lom = this.connection.getLargeObjectAPI();
        long oid = lom.createLO();
        LargeObject lob = lom.open(oid);
        Charset connectionCharset = Charset.forName(this.connection.getEncoding().name());
        OutputStream los = lob.getOutputStream();
        OutputStreamWriter lw = new OutputStreamWriter(los, connectionCharset);
        try {
            int c = inStream.read();
            for (int p = 0; c > -1 && p < length; ++p) {
                ((Writer)lw).write(c);
                c = inStream.read();
            }
            ((Writer)lw).close();
        }
        catch (IOException se) {
            throw new PSQLException(GT.tr("Unexpected error writing large object to database.", new Object[0]), PSQLState.UNEXPECTED_ERROR, (Throwable)se);
        }
        this.setLong(i, oid);
    }

    @Override
    public void setNull(@Positive int parameterIndex, int t, @Nullable String typeName) throws SQLException {
        if (typeName == null) {
            this.setNull(parameterIndex, t);
            return;
        }
        this.checkClosed();
        TypeInfo typeInfo = this.connection.getTypeInfo();
        int oid = typeInfo.getPGType(typeName);
        this.preparedParameters.setNull(parameterIndex, oid);
    }

    @Override
    public void setRef(@Positive int i, @Nullable Ref x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setRef(int,Ref)");
    }

    @Override
    public void setDate(@Positive int i, @Nullable java.sql.Date d, @Nullable Calendar cal) throws SQLException {
        this.checkClosed();
        if (d == null) {
            this.setNull(i, 91);
            return;
        }
        if (this.connection.binaryTransferSend(1082)) {
            byte[] val = new byte[4];
            TimeZone tz = cal != null ? cal.getTimeZone() : null;
            this.getTimestampUtils().toBinDate(tz, val, d);
            this.preparedParameters.setBinaryParameter(i, val, 1082);
            return;
        }
        if (cal == null) {
            cal = this.getDefaultCalendar();
        }
        this.bindString(i, this.getTimestampUtils().toString(cal, d), 0);
    }

    @Override
    public void setTime(@Positive int i, @Nullable Time t, @Nullable Calendar cal) throws SQLException {
        this.checkClosed();
        if (t == null) {
            this.setNull(i, 92);
            return;
        }
        int oid = 0;
        if (t instanceof PGTime) {
            PGTime pgTime = (PGTime)t;
            if (pgTime.getCalendar() == null) {
                oid = 1083;
            } else {
                oid = 1266;
                cal = pgTime.getCalendar();
            }
        }
        if (cal == null) {
            cal = this.getDefaultCalendar();
        }
        this.bindString(i, this.getTimestampUtils().toString(cal, t), oid);
    }

    @Override
    public void setTimestamp(@Positive int i, @Nullable Timestamp t, @Nullable Calendar cal) throws SQLException {
        this.checkClosed();
        if (t == null) {
            this.setNull(i, 93);
            return;
        }
        int oid = 0;
        if (t instanceof PGTimestamp) {
            PGTimestamp pgTimestamp = (PGTimestamp)t;
            if (pgTimestamp.getCalendar() == null) {
                oid = 1114;
            } else {
                oid = 1184;
                cal = pgTimestamp.getCalendar();
            }
        }
        if (cal == null) {
            cal = this.getDefaultCalendar();
        }
        this.bindString(i, this.getTimestampUtils().toString(cal, t), oid);
    }

    private void setDate(@Positive int i, LocalDate localDate) throws SQLException {
        int oid = 1082;
        this.bindString(i, this.getTimestampUtils().toString(localDate), oid);
    }

    private void setTime(@Positive int i, LocalTime localTime) throws SQLException {
        int oid = 1083;
        this.bindString(i, this.getTimestampUtils().toString(localTime), oid);
    }

    private void setTime(@Positive int i, OffsetTime offsetTime) throws SQLException {
        int oid = 1266;
        this.bindString(i, this.getTimestampUtils().toString(offsetTime), oid);
    }

    private void setTimestamp(@Positive int i, LocalDateTime localDateTime) throws SQLException {
        int oid = 1114;
        this.bindString(i, this.getTimestampUtils().toString(localDateTime), oid);
    }

    private void setTimestamp(@Positive int i, OffsetDateTime offsetDateTime) throws SQLException {
        int oid = 1184;
        this.bindString(i, this.getTimestampUtils().toString(offsetDateTime), oid);
    }

    public ParameterMetaData createParameterMetaData(BaseConnection conn, int[] oids) throws SQLException {
        return new PgParameterMetaData(conn, oids);
    }

    @Override
    public void setObject(@Positive int parameterIndex, @Nullable Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setObject");
    }

    @Override
    public void setObject(@Positive int parameterIndex, @Nullable Object x, SQLType targetSqlType) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setObject");
    }

    @Override
    public void setRowId(@Positive int parameterIndex, @Nullable RowId x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setRowId(int, RowId)");
    }

    @Override
    public void setNString(@Positive int parameterIndex, @Nullable String value) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNString(int, String)");
    }

    @Override
    public void setNCharacterStream(@Positive int parameterIndex, @Nullable Reader value, long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNCharacterStream(int, Reader, long)");
    }

    @Override
    public void setNCharacterStream(@Positive int parameterIndex, @Nullable Reader value) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNCharacterStream(int, Reader)");
    }

    @Override
    public void setCharacterStream(@Positive int parameterIndex, @Nullable Reader value, @NonNegative long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setCharacterStream(int, Reader, long)");
    }

    @Override
    public void setCharacterStream(@Positive int parameterIndex, @Nullable Reader value) throws SQLException {
        if (this.connection.getPreferQueryMode() == PreferQueryMode.SIMPLE) {
            String s2 = value != null ? this.readerToString(value, Integer.MAX_VALUE) : null;
            this.setString(parameterIndex, s2);
            return;
        }
        ReaderInputStream is = value != null ? new ReaderInputStream(value) : null;
        this.setObject(parameterIndex, (Object)is, -1);
    }

    @Override
    public void setBinaryStream(@Positive int parameterIndex, @Nullable InputStream value, @NonNegative @IntRange(from=0L, to=0x7FFFFFFFL) long length) throws SQLException {
        if (length > Integer.MAX_VALUE) {
            throw new PSQLException(GT.tr("Object is too large to send over the protocol.", new Object[0]), PSQLState.NUMERIC_CONSTANT_OUT_OF_RANGE);
        }
        if (value == null) {
            this.preparedParameters.setNull(parameterIndex, 17);
        } else {
            this.preparedParameters.setBytea(parameterIndex, value, (int)length);
        }
    }

    @Override
    public void setBinaryStream(@Positive int parameterIndex, @Nullable InputStream value) throws SQLException {
        if (value == null) {
            this.preparedParameters.setNull(parameterIndex, 17);
        } else {
            this.preparedParameters.setBytea(parameterIndex, value);
        }
    }

    @Override
    public void setAsciiStream(@Positive int parameterIndex, @Nullable InputStream value, @NonNegative long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setAsciiStream(int, InputStream, long)");
    }

    @Override
    public void setAsciiStream(@Positive int parameterIndex, @Nullable InputStream value) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setAsciiStream(int, InputStream)");
    }

    @Override
    public void setNClob(@Positive int parameterIndex, @Nullable NClob value) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNClob(int, NClob)");
    }

    @Override
    public void setClob(@Positive int parameterIndex, @Nullable Reader reader, @NonNegative long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setClob(int, Reader, long)");
    }

    @Override
    public void setClob(@Positive int parameterIndex, @Nullable Reader reader) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setClob(int, Reader)");
    }

    @Override
    public void setBlob(@Positive int parameterIndex, @Nullable InputStream inputStream, @NonNegative long length) throws SQLException {
        this.checkClosed();
        if (inputStream == null) {
            this.setNull(parameterIndex, 2004);
            return;
        }
        if (length < 0L) {
            throw new PSQLException(GT.tr("Invalid stream length {0}.", length), PSQLState.INVALID_PARAMETER_VALUE);
        }
        long oid = this.createBlob(parameterIndex, inputStream, length);
        this.setLong(parameterIndex, oid);
    }

    @Override
    public void setBlob(@Positive int parameterIndex, @Nullable InputStream inputStream) throws SQLException {
        this.checkClosed();
        if (inputStream == null) {
            this.setNull(parameterIndex, 2004);
            return;
        }
        long oid = this.createBlob(parameterIndex, inputStream, -1L);
        this.setLong(parameterIndex, oid);
    }

    @Override
    public void setNClob(@Positive int parameterIndex, @Nullable Reader reader, @NonNegative long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNClob(int, Reader, long)");
    }

    @Override
    public void setNClob(@Positive int parameterIndex, @Nullable Reader reader) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNClob(int, Reader)");
    }

    @Override
    public void setSQLXML(@Positive int parameterIndex, @Nullable SQLXML xmlObject) throws SQLException {
        String stringValue;
        this.checkClosed();
        String string = stringValue = xmlObject == null ? null : xmlObject.getString();
        if (stringValue == null) {
            this.setNull(parameterIndex, 2009);
        } else {
            this.setString(parameterIndex, stringValue, 142);
        }
    }

    private void setUuid(@Positive int parameterIndex, UUID uuid) throws SQLException {
        if (this.connection.binaryTransferSend(2950)) {
            byte[] val = new byte[16];
            ByteConverter.int8(val, 0, uuid.getMostSignificantBits());
            ByteConverter.int8(val, 8, uuid.getLeastSignificantBits());
            this.bindBytes(parameterIndex, val, 2950);
        } else {
            this.bindLiteral(parameterIndex, uuid.toString(), 2950);
        }
    }

    @Override
    public void setURL(@Positive int parameterIndex, @Nullable URL x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setURL(int,URL)");
    }

    @Override
    public int[] executeBatch() throws SQLException {
        try {
            if (this.batchParameters != null && this.batchParameters.size() > 1 && this.mPrepareThreshold > 0) {
                this.preparedQuery.increaseExecuteCount(this.mPrepareThreshold);
            }
            int[] nArray = super.executeBatch();
            return nArray;
        }
        finally {
            this.defaultTimeZone = null;
        }
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

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        int flags = 49;
        PgStatement.StatementResultHandler handler = new PgStatement.StatementResultHandler(this);
        this.connection.getQueryExecutor().execute(this.preparedQuery.query, this.preparedParameters, handler, 0, 0, flags);
        int[] oids = this.preparedParameters.getTypeOIDs();
        return this.createParameterMetaData(this.connection, oids);
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    @Override
    protected void transformQueriesAndParameters() throws SQLException {
        @Nullable ArrayList batchParameters = this.batchParameters;
        if (batchParameters == null || batchParameters.size() <= 1 || !(this.preparedQuery.query instanceof BatchedQuery)) {
            return;
        }
        BatchedQuery originalQuery = (BatchedQuery)this.preparedQuery.query;
        int bindCount = originalQuery.getBindCount();
        int highestBlockCount = 128;
        int maxValueBlocks = bindCount == 0 ? 1024 : Integer.highestOneBit(Math.min(Math.max(1, this.maximumNumberOfParameters() / bindCount), 128));
        int unprocessedBatchCount = batchParameters.size();
        int fullValueBlocksCount = unprocessedBatchCount / maxValueBlocks;
        int partialValueBlocksCount = Integer.bitCount(unprocessedBatchCount % maxValueBlocks);
        int count = fullValueBlocksCount + partialValueBlocksCount;
        ArrayList<BatchedQuery> newBatchStatements = new ArrayList<BatchedQuery>(count);
        ArrayList<@Nullable ParameterList> newBatchParameters = new ArrayList<ParameterList>(count);
        int offset = 0;
        for (int i = 0; i < count; ++i) {
            int valueBlock = unprocessedBatchCount >= maxValueBlocks ? maxValueBlocks : Integer.highestOneBit(unprocessedBatchCount);
            BatchedQuery bq = originalQuery.deriveForMultiBatch(valueBlock);
            ParameterList newPl = bq.createParameterList();
            for (int j = 0; j < valueBlock; ++j) {
                ParameterList pl;
                if ((pl = (ParameterList)batchParameters.get(offset++)) == null) continue;
                newPl.appendAll(pl);
            }
            newBatchStatements.add(bq);
            newBatchParameters.add(newPl);
            unprocessedBatchCount -= valueBlock;
        }
        this.batchStatements = newBatchStatements;
        this.batchParameters = newBatchParameters;
    }
}

