/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.sql.ParameterMetaData;
import java.sql.SQLException;
import org.checkerframework.checker.index.qual.Positive;
import org.postgresql.core.BaseConnection;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class PgParameterMetaData
implements ParameterMetaData {
    private final BaseConnection connection;
    private final int[] oids;

    public PgParameterMetaData(BaseConnection connection, int[] oids) {
        this.connection = connection;
        this.oids = oids;
    }

    @Override
    public String getParameterClassName(@Positive int param) throws SQLException {
        this.checkParamIndex(param);
        return this.connection.getTypeInfo().getJavaClass(this.oids[param - 1]);
    }

    @Override
    public int getParameterCount() {
        return this.oids.length;
    }

    @Override
    public int getParameterMode(int param) throws SQLException {
        this.checkParamIndex(param);
        return 1;
    }

    @Override
    public int getParameterType(int param) throws SQLException {
        this.checkParamIndex(param);
        return this.connection.getTypeInfo().getSQLType(this.oids[param - 1]);
    }

    @Override
    public String getParameterTypeName(int param) throws SQLException {
        this.checkParamIndex(param);
        return Nullness.castNonNull(this.connection.getTypeInfo().getPGType(this.oids[param - 1]));
    }

    @Override
    public int getPrecision(int param) throws SQLException {
        this.checkParamIndex(param);
        return 0;
    }

    @Override
    public int getScale(int param) throws SQLException {
        this.checkParamIndex(param);
        return 0;
    }

    @Override
    public int isNullable(int param) throws SQLException {
        this.checkParamIndex(param);
        return 2;
    }

    @Override
    public boolean isSigned(int param) throws SQLException {
        this.checkParamIndex(param);
        return this.connection.getTypeInfo().isSigned(this.oids[param - 1]);
    }

    private void checkParamIndex(int param) throws PSQLException {
        if (param < 1 || param > this.oids.length) {
            throw new PSQLException(GT.tr("The parameter index is out of range: {0}, number of parameters: {1}.", param, this.oids.length), PSQLState.INVALID_PARAMETER_VALUE);
        }
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
}

