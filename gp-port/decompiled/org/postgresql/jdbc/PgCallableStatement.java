/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.Driver;
import org.postgresql.core.ParameterList;
import org.postgresql.core.Query;
import org.postgresql.jdbc.BatchResultHandler;
import org.postgresql.jdbc.BooleanTypeUtil;
import org.postgresql.jdbc.CallableBatchResultHandler;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.jdbc.PgPreparedStatement;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

class PgCallableStatement
extends PgPreparedStatement
implements CallableStatement {
    private final boolean isFunction;
    private int @Nullable [] functionReturnType;
    private int @Nullable [] testReturn;
    private boolean returnTypeSet;
    protected @Nullable Object @Nullable [] callResult;
    private int lastIndex = 0;

    PgCallableStatement(PgConnection connection, String sql, int rsType, int rsConcurrency, int rsHoldability) throws SQLException {
        super(connection, connection.borrowCallableQuery(sql), rsType, rsConcurrency, rsHoldability);
        this.isFunction = this.preparedQuery.isFunction;
        if (this.isFunction) {
            int inParamCount = this.preparedParameters.getInParameterCount() + 1;
            this.testReturn = new int[inParamCount];
            this.functionReturnType = new int[inParamCount];
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        if (this.isFunction) {
            this.executeWithFlags(0);
            return 0;
        }
        return super.executeUpdate();
    }

    @Override
    public @Nullable Object getObject(@Positive int i, @Nullable Map<String, Class<?>> map) throws SQLException {
        return this.getObjectImpl(i, map);
    }

    @Override
    public @Nullable Object getObject(String s2, @Nullable Map<String, Class<?>> map) throws SQLException {
        return this.getObjectImpl(s2, map);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean executeWithFlags(int flags) throws SQLException {
        PgCallableStatement pgCallableStatement = this;
        synchronized (pgCallableStatement) {
            int outParameterCount;
            boolean hasResultSet = super.executeWithFlags(flags);
            int[] functionReturnType = this.functionReturnType;
            if (!this.isFunction || !this.returnTypeSet || functionReturnType == null) {
                return hasResultSet;
            }
            if (!hasResultSet) {
                throw new PSQLException(GT.tr("A CallableStatement was executed with nothing returned.", new Object[0]), PSQLState.NO_DATA);
            }
            ResultSet rs = Nullness.castNonNull(this.getResultSet());
            if (!rs.next()) {
                throw new PSQLException(GT.tr("A CallableStatement was executed with nothing returned.", new Object[0]), PSQLState.NO_DATA);
            }
            int cols = rs.getMetaData().getColumnCount();
            if (cols != (outParameterCount = this.preparedParameters.getOutParameterCount())) {
                throw new PSQLException(GT.tr("A CallableStatement was executed with an invalid number of parameters", new Object[0]), PSQLState.SYNTAX_ERROR);
            }
            this.lastIndex = 0;
            @Nullable Object[] callResult = new Object[this.preparedParameters.getParameterCount() + 1];
            this.callResult = callResult;
            int i = 0;
            int j = 0;
            while (i < cols) {
                while (j < functionReturnType.length && functionReturnType[j] == 0) {
                    ++j;
                }
                callResult[j] = rs.getObject(i + 1);
                int columnType = rs.getMetaData().getColumnType(i + 1);
                if (columnType != functionReturnType[j]) {
                    if (columnType == 8 && functionReturnType[j] == 7) {
                        Object result = callResult[j];
                        if (result != null) {
                            callResult[j] = Float.valueOf(((Double)result).floatValue());
                        }
                    } else if (columnType != 2012 || functionReturnType[j] != 1111) {
                        throw new PSQLException(GT.tr("A CallableStatement function was executed and the out parameter {0} was of type {1} however type {2} was registered.", i + 1, "java.sql.Types=" + columnType, "java.sql.Types=" + functionReturnType[j]), PSQLState.DATA_TYPE_MISMATCH);
                    }
                }
                ++i;
                ++j;
            }
            rs.close();
            this.result = null;
        }
        return false;
    }

    @Override
    public void registerOutParameter(@Positive int parameterIndex, int sqlType) throws SQLException {
        this.checkClosed();
        switch (sqlType) {
            case -6: {
                sqlType = 5;
                break;
            }
            case -1: {
                sqlType = 12;
                break;
            }
            case 3: {
                sqlType = 2;
                break;
            }
            case 6: {
                sqlType = 8;
                break;
            }
            case -4: 
            case -3: {
                sqlType = -2;
                break;
            }
            case 16: {
                sqlType = -7;
                break;
            }
        }
        int[] functionReturnType = this.functionReturnType;
        int[] testReturn = this.testReturn;
        if (!this.isFunction || functionReturnType == null || testReturn == null) {
            throw new PSQLException(GT.tr("This statement does not declare an OUT parameter.  Use '{' ?= call ... '}' to declare one.", new Object[0]), PSQLState.STATEMENT_NOT_ALLOWED_IN_FUNCTION_CALL);
        }
        this.preparedParameters.registerOutParameter(parameterIndex, sqlType);
        functionReturnType[parameterIndex - 1] = sqlType;
        testReturn[parameterIndex - 1] = sqlType;
        if (functionReturnType[parameterIndex - 1] == 1 || functionReturnType[parameterIndex - 1] == -1) {
            testReturn[parameterIndex - 1] = 12;
        } else if (functionReturnType[parameterIndex - 1] == 6) {
            testReturn[parameterIndex - 1] = 7;
        }
        this.returnTypeSet = true;
    }

    @Override
    public boolean wasNull() throws SQLException {
        if (this.lastIndex == 0 || this.callResult == null) {
            throw new PSQLException(GT.tr("wasNull cannot be call before fetching a result.", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        return this.callResult[this.lastIndex - 1] == null;
    }

    @Override
    public @Nullable String getString(@Positive int parameterIndex) throws SQLException {
        Object result = this.checkIndex(parameterIndex, 12, "String");
        return (String)result;
    }

    @Override
    public boolean getBoolean(@Positive int parameterIndex) throws SQLException {
        Object result = this.checkIndex(parameterIndex, -7, "Boolean");
        if (result == null) {
            return false;
        }
        return BooleanTypeUtil.castToBoolean(result);
    }

    @Override
    public byte getByte(@Positive int parameterIndex) throws SQLException {
        Object result = this.checkIndex(parameterIndex, 5, "Byte");
        if (result == null) {
            return 0;
        }
        return ((Integer)result).byteValue();
    }

    @Override
    public short getShort(@Positive int parameterIndex) throws SQLException {
        Object result = this.checkIndex(parameterIndex, 5, "Short");
        if (result == null) {
            return 0;
        }
        return ((Integer)result).shortValue();
    }

    @Override
    public int getInt(@Positive int parameterIndex) throws SQLException {
        Object result = this.checkIndex(parameterIndex, 4, "Int");
        if (result == null) {
            return 0;
        }
        return (Integer)result;
    }

    @Override
    public long getLong(@Positive int parameterIndex) throws SQLException {
        Object result = this.checkIndex(parameterIndex, -5, "Long");
        if (result == null) {
            return 0L;
        }
        return (Long)result;
    }

    @Override
    public float getFloat(@Positive int parameterIndex) throws SQLException {
        Object result = this.checkIndex(parameterIndex, 7, "Float");
        if (result == null) {
            return 0.0f;
        }
        return ((Float)result).floatValue();
    }

    @Override
    public double getDouble(@Positive int parameterIndex) throws SQLException {
        Object result = this.checkIndex(parameterIndex, 8, "Double");
        if (result == null) {
            return 0.0;
        }
        return (Double)result;
    }

    @Override
    public @Nullable BigDecimal getBigDecimal(@Positive int parameterIndex, int scale) throws SQLException {
        Object result = this.checkIndex(parameterIndex, 2, "BigDecimal");
        return (BigDecimal)result;
    }

    @Override
    public byte @Nullable [] getBytes(@Positive int parameterIndex) throws SQLException {
        Object result = this.checkIndex(parameterIndex, -3, -2, "Bytes");
        return (byte[])result;
    }

    @Override
    public @Nullable Date getDate(@Positive int parameterIndex) throws SQLException {
        Object result = this.checkIndex(parameterIndex, 91, "Date");
        return (Date)result;
    }

    @Override
    public @Nullable Time getTime(@Positive int parameterIndex) throws SQLException {
        Object result = this.checkIndex(parameterIndex, 92, "Time");
        return (Time)result;
    }

    @Override
    public @Nullable Timestamp getTimestamp(@Positive int parameterIndex) throws SQLException {
        Object result = this.checkIndex(parameterIndex, 93, "Timestamp");
        return (Timestamp)result;
    }

    @Override
    public @Nullable Object getObject(@Positive int parameterIndex) throws SQLException {
        return this.getCallResult(parameterIndex);
    }

    protected @Nullable Object checkIndex(@Positive int parameterIndex, int type1, int type2, String getName) throws SQLException {
        int testReturn;
        Object result = this.getCallResult(parameterIndex);
        int n = testReturn = this.testReturn != null ? this.testReturn[parameterIndex - 1] : -1;
        if (type1 != testReturn && type2 != testReturn) {
            throw new PSQLException(GT.tr("Parameter of type {0} was registered, but call to get{1} (sqltype={2}) was made.", "java.sql.Types=" + testReturn, getName, "java.sql.Types=" + type1), PSQLState.MOST_SPECIFIC_TYPE_DOES_NOT_MATCH);
        }
        return result;
    }

    protected @Nullable Object checkIndex(@Positive int parameterIndex, int type, String getName) throws SQLException {
        int testReturn;
        Object result = this.getCallResult(parameterIndex);
        int n = testReturn = this.testReturn != null ? this.testReturn[parameterIndex - 1] : -1;
        if (type != testReturn) {
            throw new PSQLException(GT.tr("Parameter of type {0} was registered, but call to get{1} (sqltype={2}) was made.", "java.sql.Types=" + testReturn, getName, "java.sql.Types=" + type), PSQLState.MOST_SPECIFIC_TYPE_DOES_NOT_MATCH);
        }
        return result;
    }

    private @Nullable Object getCallResult(@Positive int parameterIndex) throws SQLException {
        this.checkClosed();
        if (!this.isFunction) {
            throw new PSQLException(GT.tr("A CallableStatement was declared, but no call to registerOutParameter(1, <some type>) was made.", new Object[0]), PSQLState.STATEMENT_NOT_ALLOWED_IN_FUNCTION_CALL);
        }
        if (!this.returnTypeSet) {
            throw new PSQLException(GT.tr("No function outputs were registered.", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        @Nullable Object @Nullable [] callResult = this.callResult;
        if (callResult == null) {
            throw new PSQLException(GT.tr("Results cannot be retrieved from a CallableStatement before it is executed.", new Object[0]), PSQLState.NO_DATA);
        }
        this.lastIndex = parameterIndex;
        return callResult[parameterIndex - 1];
    }

    @Override
    protected BatchResultHandler createBatchHandler(Query[] queries, @Nullable ParameterList[] parameterLists) {
        return new CallableBatchResultHandler(this, queries, parameterLists);
    }

    @Override
    public @Nullable Array getArray(int i) throws SQLException {
        Object result = this.checkIndex(i, 2003, "Array");
        return (Array)result;
    }

    @Override
    public @Nullable BigDecimal getBigDecimal(@Positive int parameterIndex) throws SQLException {
        Object result = this.checkIndex(parameterIndex, 2, "BigDecimal");
        return (BigDecimal)result;
    }

    @Override
    public @Nullable Blob getBlob(int i) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getBlob(int)");
    }

    @Override
    public @Nullable Clob getClob(int i) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getClob(int)");
    }

    public @Nullable Object getObjectImpl(int i, @Nullable Map<String, Class<?>> map) throws SQLException {
        if (map == null || map.isEmpty()) {
            return this.getObject(i);
        }
        throw Driver.notImplemented(this.getClass(), "getObjectImpl(int,Map)");
    }

    @Override
    public @Nullable Ref getRef(int i) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getRef(int)");
    }

    @Override
    public @Nullable Date getDate(int i, @Nullable Calendar cal) throws SQLException {
        Object result = this.checkIndex(i, 91, "Date");
        if (result == null) {
            return null;
        }
        String value = result.toString();
        return this.getTimestampUtils().toDate(cal, value);
    }

    @Override
    public @Nullable Time getTime(int i, @Nullable Calendar cal) throws SQLException {
        Object result = this.checkIndex(i, 92, "Time");
        if (result == null) {
            return null;
        }
        String value = result.toString();
        return this.getTimestampUtils().toTime(cal, value);
    }

    @Override
    public @Nullable Timestamp getTimestamp(int i, @Nullable Calendar cal) throws SQLException {
        Object result = this.checkIndex(i, 93, "Timestamp");
        if (result == null) {
            return null;
        }
        String value = result.toString();
        return this.getTimestampUtils().toTimestamp(cal, value);
    }

    @Override
    public void registerOutParameter(@Positive int parameterIndex, int sqlType, String typeName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "registerOutParameter(int,int,String)");
    }

    @Override
    public void setObject(String parameterName, @Nullable Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setObject");
    }

    @Override
    public void setObject(String parameterName, @Nullable Object x, SQLType targetSqlType) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setObject");
    }

    @Override
    public void registerOutParameter(@Positive int parameterIndex, SQLType sqlType) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "registerOutParameter");
    }

    @Override
    public void registerOutParameter(@Positive int parameterIndex, SQLType sqlType, int scale) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "registerOutParameter");
    }

    @Override
    public void registerOutParameter(@Positive int parameterIndex, SQLType sqlType, String typeName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "registerOutParameter");
    }

    @Override
    public void registerOutParameter(String parameterName, SQLType sqlType) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "registerOutParameter");
    }

    @Override
    public void registerOutParameter(String parameterName, SQLType sqlType, int scale) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "registerOutParameter");
    }

    @Override
    public void registerOutParameter(String parameterName, SQLType sqlType, String typeName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "registerOutParameter");
    }

    @Override
    public @Nullable RowId getRowId(@Positive int parameterIndex) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getRowId(int)");
    }

    @Override
    public @Nullable RowId getRowId(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getRowId(String)");
    }

    @Override
    public void setRowId(String parameterName, @Nullable RowId x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setRowId(String, RowId)");
    }

    @Override
    public void setNString(String parameterName, @Nullable String value) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNString(String, String)");
    }

    @Override
    public void setNCharacterStream(String parameterName, @Nullable Reader value, long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNCharacterStream(String, Reader, long)");
    }

    @Override
    public void setNCharacterStream(String parameterName, @Nullable Reader value) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNCharacterStream(String, Reader)");
    }

    @Override
    public void setCharacterStream(String parameterName, @Nullable Reader value, long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setCharacterStream(String, Reader, long)");
    }

    @Override
    public void setCharacterStream(String parameterName, @Nullable Reader value) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setCharacterStream(String, Reader)");
    }

    @Override
    public void setBinaryStream(String parameterName, @Nullable InputStream value, long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setBinaryStream(String, InputStream, long)");
    }

    @Override
    public void setBinaryStream(String parameterName, @Nullable InputStream value) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setBinaryStream(String, InputStream)");
    }

    @Override
    public void setAsciiStream(String parameterName, @Nullable InputStream value, long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setAsciiStream(String, InputStream, long)");
    }

    @Override
    public void setAsciiStream(String parameterName, @Nullable InputStream value) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setAsciiStream(String, InputStream)");
    }

    @Override
    public void setNClob(String parameterName, @Nullable NClob value) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNClob(String, NClob)");
    }

    @Override
    public void setClob(String parameterName, @Nullable Reader reader, long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setClob(String, Reader, long)");
    }

    @Override
    public void setClob(String parameterName, @Nullable Reader reader) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setClob(String, Reader)");
    }

    @Override
    public void setBlob(String parameterName, @Nullable InputStream inputStream, long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setBlob(String, InputStream, long)");
    }

    @Override
    public void setBlob(String parameterName, @Nullable InputStream inputStream) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setBlob(String, InputStream)");
    }

    @Override
    public void setBlob(String parameterName, @Nullable Blob x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setBlob(String, Blob)");
    }

    @Override
    public void setClob(String parameterName, @Nullable Clob x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setClob(String, Clob)");
    }

    @Override
    public void setNClob(String parameterName, @Nullable Reader reader, long length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNClob(String, Reader, long)");
    }

    @Override
    public void setNClob(String parameterName, @Nullable Reader reader) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNClob(String, Reader)");
    }

    @Override
    public @Nullable NClob getNClob(@Positive int parameterIndex) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getNClob(int)");
    }

    @Override
    public @Nullable NClob getNClob(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getNClob(String)");
    }

    @Override
    public void setSQLXML(String parameterName, @Nullable SQLXML xmlObject) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setSQLXML(String, SQLXML)");
    }

    @Override
    public @Nullable SQLXML getSQLXML(@Positive int parameterIndex) throws SQLException {
        Object result = this.checkIndex(parameterIndex, 2009, "SQLXML");
        return (SQLXML)result;
    }

    @Override
    public @Nullable SQLXML getSQLXML(String parameterIndex) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getSQLXML(String)");
    }

    @Override
    public String getNString(@Positive int parameterIndex) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getNString(int)");
    }

    @Override
    public @Nullable String getNString(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getNString(String)");
    }

    @Override
    public @Nullable Reader getNCharacterStream(@Positive int parameterIndex) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getNCharacterStream(int)");
    }

    @Override
    public @Nullable Reader getNCharacterStream(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getNCharacterStream(String)");
    }

    @Override
    public @Nullable Reader getCharacterStream(@Positive int parameterIndex) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getCharacterStream(int)");
    }

    @Override
    public @Nullable Reader getCharacterStream(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getCharacterStream(String)");
    }

    @Override
    public <T> @Nullable T getObject(@Positive int parameterIndex, Class<T> type) throws SQLException {
        if (type == ResultSet.class) {
            return type.cast(this.getObject(parameterIndex));
        }
        throw new PSQLException(GT.tr("Unsupported type conversion to {1}.", type), PSQLState.INVALID_PARAMETER_VALUE);
    }

    @Override
    public <T> @Nullable T getObject(String parameterName, Class<T> type) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getObject(String, Class<T>)");
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "registerOutParameter(String,int)");
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "registerOutParameter(String,int,int)");
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "registerOutParameter(String,int,String)");
    }

    @Override
    public @Nullable URL getURL(@Positive int parameterIndex) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getURL(String)");
    }

    @Override
    public void setURL(String parameterName, @Nullable URL val) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setURL(String,URL)");
    }

    @Override
    public void setNull(String parameterName, int sqlType) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNull(String,int)");
    }

    @Override
    public void setBoolean(String parameterName, boolean x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setBoolean(String,boolean)");
    }

    @Override
    public void setByte(String parameterName, byte x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setByte(String,byte)");
    }

    @Override
    public void setShort(String parameterName, short x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setShort(String,short)");
    }

    @Override
    public void setInt(String parameterName, int x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setInt(String,int)");
    }

    @Override
    public void setLong(String parameterName, long x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setLong(String,long)");
    }

    @Override
    public void setFloat(String parameterName, float x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setFloat(String,float)");
    }

    @Override
    public void setDouble(String parameterName, double x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setDouble(String,double)");
    }

    @Override
    public void setBigDecimal(String parameterName, @Nullable BigDecimal x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setBigDecimal(String,BigDecimal)");
    }

    @Override
    public void setString(String parameterName, @Nullable String x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setString(String,String)");
    }

    @Override
    public void setBytes(String parameterName, byte @Nullable [] x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setBytes(String,byte)");
    }

    @Override
    public void setDate(String parameterName, @Nullable Date x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setDate(String,Date)");
    }

    @Override
    public void setTime(String parameterName, @Nullable Time x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setTime(String,Time)");
    }

    @Override
    public void setTimestamp(String parameterName, @Nullable Timestamp x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setTimestamp(String,Timestamp)");
    }

    @Override
    public void setAsciiStream(String parameterName, @Nullable InputStream x, int length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setAsciiStream(String,InputStream,int)");
    }

    @Override
    public void setBinaryStream(String parameterName, @Nullable InputStream x, int length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setBinaryStream(String,InputStream,int)");
    }

    @Override
    public void setObject(String parameterName, @Nullable Object x, int targetSqlType, int scale) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setObject(String,Object,int,int)");
    }

    @Override
    public void setObject(String parameterName, @Nullable Object x, int targetSqlType) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setObject(String,Object,int)");
    }

    @Override
    public void setObject(String parameterName, @Nullable Object x) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setObject(String,Object)");
    }

    @Override
    public void setCharacterStream(String parameterName, @Nullable Reader reader, int length) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setCharacterStream(String,Reader,int)");
    }

    @Override
    public void setDate(String parameterName, @Nullable Date x, @Nullable Calendar cal) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setDate(String,Date,Calendar)");
    }

    @Override
    public void setTime(String parameterName, @Nullable Time x, @Nullable Calendar cal) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setTime(String,Time,Calendar)");
    }

    @Override
    public void setTimestamp(String parameterName, @Nullable Timestamp x, @Nullable Calendar cal) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setTimestamp(String,Timestamp,Calendar)");
    }

    @Override
    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "setNull(String,int,String)");
    }

    @Override
    public @Nullable String getString(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getString(String)");
    }

    @Override
    public boolean getBoolean(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getBoolean(String)");
    }

    @Override
    public byte getByte(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getByte(String)");
    }

    @Override
    public short getShort(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getShort(String)");
    }

    @Override
    public int getInt(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getInt(String)");
    }

    @Override
    public long getLong(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getLong(String)");
    }

    @Override
    public float getFloat(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getFloat(String)");
    }

    @Override
    public double getDouble(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getDouble(String)");
    }

    @Override
    public byte @Nullable [] getBytes(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getBytes(String)");
    }

    @Override
    public @Nullable Date getDate(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getDate(String)");
    }

    @Override
    public Time getTime(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getTime(String)");
    }

    @Override
    public @Nullable Timestamp getTimestamp(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getTimestamp(String)");
    }

    @Override
    public @Nullable Object getObject(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getObject(String)");
    }

    @Override
    public @Nullable BigDecimal getBigDecimal(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getBigDecimal(String)");
    }

    public @Nullable Object getObjectImpl(String parameterName, @Nullable Map<String, Class<?>> map) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getObject(String,Map)");
    }

    @Override
    public @Nullable Ref getRef(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getRef(String)");
    }

    @Override
    public @Nullable Blob getBlob(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getBlob(String)");
    }

    @Override
    public @Nullable Clob getClob(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getClob(String)");
    }

    @Override
    public @Nullable Array getArray(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getArray(String)");
    }

    @Override
    public @Nullable Date getDate(String parameterName, @Nullable Calendar cal) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getDate(String,Calendar)");
    }

    @Override
    public @Nullable Time getTime(String parameterName, @Nullable Calendar cal) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getTime(String,Calendar)");
    }

    @Override
    public @Nullable Timestamp getTimestamp(String parameterName, @Nullable Calendar cal) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getTimestamp(String,Calendar)");
    }

    @Override
    public @Nullable URL getURL(String parameterName) throws SQLException {
        throw Driver.notImplemented(this.getClass(), "getURL(String)");
    }

    @Override
    public void registerOutParameter(@Positive int parameterIndex, int sqlType, int scale) throws SQLException {
        this.registerOutParameter(parameterIndex, sqlType);
    }
}

