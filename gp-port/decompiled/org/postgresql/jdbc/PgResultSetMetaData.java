/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGResultSetMetaData;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Field;
import org.postgresql.core.ServerVersion;
import org.postgresql.jdbc.FieldMetadata;
import org.postgresql.util.GT;
import org.postgresql.util.Gettable;
import org.postgresql.util.GettableHashMap;
import org.postgresql.util.JdbcBlackHole;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class PgResultSetMetaData
implements ResultSetMetaData,
PGResultSetMetaData {
    protected final BaseConnection connection;
    protected final Field[] fields;
    private boolean fieldInfoFetched;

    public PgResultSetMetaData(BaseConnection connection, Field[] fields) {
        this.connection = connection;
        this.fields = fields;
        this.fieldInfoFetched = false;
    }

    @Override
    public int getColumnCount() throws SQLException {
        return this.fields.length;
    }

    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        this.fetchFieldMetaData();
        Field field = this.getField(column);
        FieldMetadata metadata = field.getMetadata();
        return metadata != null && metadata.autoIncrement;
    }

    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        Field field = this.getField(column);
        return this.connection.getTypeInfo().isCaseSensitive(field.getOID());
    }

    @Override
    public boolean isSearchable(int column) throws SQLException {
        return true;
    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        String typeName = this.getPGType(column);
        return "cash".equals(typeName) || "money".equals(typeName);
    }

    @Override
    public int isNullable(int column) throws SQLException {
        this.fetchFieldMetaData();
        Field field = this.getField(column);
        FieldMetadata metadata = field.getMetadata();
        return metadata == null ? 1 : metadata.nullable;
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        Field field = this.getField(column);
        return this.connection.getTypeInfo().isSigned(field.getOID());
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        Field field = this.getField(column);
        return this.connection.getTypeInfo().getDisplaySize(field.getOID(), field.getMod());
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        Field field = this.getField(column);
        return field.getColumnLabel();
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return this.getColumnLabel(column);
    }

    @Override
    public String getBaseColumnName(int column) throws SQLException {
        Field field = this.getField(column);
        if (field.getTableOid() == 0) {
            return "";
        }
        this.fetchFieldMetaData();
        FieldMetadata metadata = field.getMetadata();
        return metadata == null ? "" : metadata.columnName;
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        return "";
    }

    private boolean populateFieldsWithMetadata(Gettable<FieldMetadata.Key, FieldMetadata> metadata) {
        boolean allOk = true;
        for (Field field : this.fields) {
            if (field.getMetadata() != null) continue;
            FieldMetadata fieldMetadata = metadata.get(new FieldMetadata.Key(field.getTableOid(), field.getPositionInTable()));
            if (fieldMetadata == null) {
                allOk = false;
                continue;
            }
            field.setMetadata(fieldMetadata);
        }
        this.fieldInfoFetched |= allOk;
        return allOk;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void fetchFieldMetaData() throws SQLException {
        if (this.fieldInfoFetched) {
            return;
        }
        if (this.populateFieldsWithMetadata(this.connection.getFieldMetadataCache())) {
            return;
        }
        StringBuilder sql = new StringBuilder("SELECT c.oid, a.attnum, a.attname, c.relname, n.nspname, a.attnotnull OR (t.typtype = 'd' AND t.typnotnull), ");
        if (this.connection.haveMinimumServerVersion(ServerVersion.v10)) {
            sql.append("a.attidentity != '' OR pg_catalog.pg_get_expr(d.adbin, d.adrelid) LIKE '%nextval(%' ");
        } else {
            sql.append("pg_catalog.pg_get_expr(d.adbin, d.adrelid) LIKE '%nextval(%' ");
        }
        sql.append("FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON (c.relnamespace = n.oid) JOIN pg_catalog.pg_attribute a ON (c.oid = a.attrelid) JOIN pg_catalog.pg_type t ON (a.atttypid = t.oid) LEFT JOIN pg_catalog.pg_attrdef d ON (d.adrelid = a.attrelid AND d.adnum = a.attnum) JOIN (");
        boolean hasSourceInfo = false;
        for (Field field : this.fields) {
            if (field.getMetadata() != null) continue;
            if (hasSourceInfo) {
                sql.append(" UNION ALL ");
            }
            sql.append("SELECT ");
            sql.append(field.getTableOid());
            if (!hasSourceInfo) {
                sql.append(" AS oid ");
            }
            sql.append(", ");
            sql.append(field.getPositionInTable());
            if (!hasSourceInfo) {
                sql.append(" AS attnum");
            }
            if (hasSourceInfo) continue;
            hasSourceInfo = true;
        }
        sql.append(") vals ON (c.oid = vals.oid AND a.attnum = vals.attnum) ");
        if (!hasSourceInfo) {
            this.fieldInfoFetched = true;
            return;
        }
        Statement stmt = this.connection.createStatement();
        ResultSet rs = null;
        GettableHashMap<FieldMetadata.Key, FieldMetadata> md = new GettableHashMap<FieldMetadata.Key, FieldMetadata>();
        try {
            rs = stmt.executeQuery(sql.toString());
            while (rs.next()) {
                int table = (int)rs.getLong(1);
                int column = (int)rs.getLong(2);
                String columnName = Nullness.castNonNull(rs.getString(3));
                String tableName = Nullness.castNonNull(rs.getString(4));
                String schemaName = Nullness.castNonNull(rs.getString(5));
                int nullable = rs.getBoolean(6) ? 0 : 1;
                boolean autoIncrement = rs.getBoolean(7);
                FieldMetadata fieldMetadata = new FieldMetadata(columnName, tableName, schemaName, nullable, autoIncrement);
                FieldMetadata.Key key = new FieldMetadata.Key(table, column);
                md.put(key, fieldMetadata);
            }
        }
        catch (Throwable throwable) {
            JdbcBlackHole.close(rs);
            JdbcBlackHole.close(stmt);
            throw throwable;
        }
        JdbcBlackHole.close(rs);
        JdbcBlackHole.close(stmt);
        this.populateFieldsWithMetadata(md);
        this.connection.getFieldMetadataCache().putAll(md);
    }

    @Override
    public String getBaseSchemaName(int column) throws SQLException {
        this.fetchFieldMetaData();
        Field field = this.getField(column);
        FieldMetadata metadata = field.getMetadata();
        return metadata == null ? "" : metadata.schemaName;
    }

    @Override
    public int getPrecision(int column) throws SQLException {
        Field field = this.getField(column);
        return this.connection.getTypeInfo().getPrecision(field.getOID(), field.getMod());
    }

    @Override
    public int getScale(int column) throws SQLException {
        Field field = this.getField(column);
        return this.connection.getTypeInfo().getScale(field.getOID(), field.getMod());
    }

    @Override
    public String getTableName(int column) throws SQLException {
        return this.getBaseTableName(column);
    }

    @Override
    public String getBaseTableName(int column) throws SQLException {
        this.fetchFieldMetaData();
        Field field = this.getField(column);
        FieldMetadata metadata = field.getMetadata();
        return metadata == null ? "" : metadata.tableName;
    }

    @Override
    public String getCatalogName(int column) throws SQLException {
        return "";
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        return this.getSQLType(column);
    }

    @Override
    public int getFormat(int column) throws SQLException {
        return this.getField(column).getFormat();
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        String type = this.getPGType(column);
        if (this.isAutoIncrement(column)) {
            if ("int4".equals(type)) {
                return "serial";
            }
            if ("int8".equals(type)) {
                return "bigserial";
            }
            if ("int2".equals(type) && this.connection.haveMinimumServerVersion(ServerVersion.v9_2)) {
                return "smallserial";
            }
        }
        return Nullness.castNonNull(type);
    }

    @Override
    public boolean isReadOnly(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isWritable(int column) throws SQLException {
        return !this.isReadOnly(column);
    }

    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        return false;
    }

    protected Field getField(int columnIndex) throws SQLException {
        if (columnIndex < 1 || columnIndex > this.fields.length) {
            throw new PSQLException(GT.tr("The column index is out of range: {0}, number of columns: {1}.", columnIndex, this.fields.length), PSQLState.INVALID_PARAMETER_VALUE);
        }
        return this.fields[columnIndex - 1];
    }

    protected @Nullable String getPGType(int columnIndex) throws SQLException {
        return this.connection.getTypeInfo().getPGType(this.getField(columnIndex).getOID());
    }

    protected int getSQLType(int columnIndex) throws SQLException {
        return this.connection.getTypeInfo().getSQLType(this.getField(columnIndex).getOID());
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        Field field = this.getField(column);
        String result = this.connection.getTypeInfo().getJavaClass(field.getOID());
        if (result != null) {
            return result;
        }
        int sqlType = this.getSQLType(column);
        switch (sqlType) {
            case 2003: {
                return "java.sql.Array";
            }
        }
        String type = this.getPGType(column);
        if ("unknown".equals(type)) {
            return "java.lang.String";
        }
        return "java.lang.Object";
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

