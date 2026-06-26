/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.BaseStatement;
import org.postgresql.core.ServerVersion;
import org.postgresql.core.TypeInfo;
import org.postgresql.util.GT;
import org.postgresql.util.PGobject;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class TypeInfoCache
implements TypeInfo {
    private static final Logger LOGGER = Logger.getLogger(TypeInfoCache.class.getName());
    private Map<String, Integer> pgNameToSQLType;
    private Map<Integer, Integer> oidToSQLType;
    private Map<String, String> pgNameToJavaClass;
    private Map<Integer, String> oidToPgName;
    private Map<String, Integer> pgNameToOid;
    private Map<String, Integer> javaArrayTypeToOid;
    private Map<String, Class<? extends PGobject>> pgNameToPgObject;
    private Map<Integer, Integer> pgArrayToPgType;
    private Map<Integer, Character> arrayOidToDelimiter;
    private final BaseConnection conn;
    private final int unknownLength;
    private @Nullable PreparedStatement getOidStatementSimple;
    private @Nullable PreparedStatement getOidStatementComplexNonArray;
    private @Nullable PreparedStatement getOidStatementComplexArray;
    private @Nullable PreparedStatement getNameStatement;
    private @Nullable PreparedStatement getArrayElementOidStatement;
    private @Nullable PreparedStatement getArrayDelimiterStatement;
    private @Nullable PreparedStatement getTypeInfoStatement;
    private @Nullable PreparedStatement getAllTypeInfoStatement;
    private static final Object[][] types = new Object[][]{{"int2", 21, 5, "java.lang.Integer", 1005}, {"int4", 23, 4, "java.lang.Integer", 1007}, {"oid", 26, -5, "java.lang.Long", 1028}, {"int8", 20, -5, "java.lang.Long", 1016}, {"money", 790, 8, "java.lang.Double", 791}, {"numeric", 1700, 2, "java.math.BigDecimal", 1231}, {"float4", 700, 7, "java.lang.Float", 1021}, {"float8", 701, 8, "java.lang.Double", 1022}, {"char", 18, 1, "java.lang.String", 1002}, {"bpchar", 1042, 1, "java.lang.String", 1014}, {"varchar", 1043, 12, "java.lang.String", 1015}, {"text", 25, 12, "java.lang.String", 1009}, {"name", 19, 12, "java.lang.String", 1003}, {"bytea", 17, -2, "[B", 1001}, {"bool", 16, -7, "java.lang.Boolean", 1000}, {"bit", 1560, -7, "java.lang.Boolean", 1561}, {"date", 1082, 91, "java.sql.Date", 1182}, {"time", 1083, 92, "java.sql.Time", 1183}, {"timetz", 1266, 92, "java.sql.Time", 1270}, {"timestamp", 1114, 93, "java.sql.Timestamp", 1115}, {"timestamptz", 1184, 93, "java.sql.Timestamp", 1185}, {"refcursor", 1790, 2012, "java.sql.ResultSet", 2201}, {"json", 114, 1111, "org.postgresql.util.PGobject", 199}, {"point", 600, 1111, "org.postgresql.geometric.PGpoint", 1017}};
    private static final ConcurrentMap<String, String> TYPE_ALIASES = new ConcurrentHashMap<String, String>(20);

    public TypeInfoCache(BaseConnection conn, int unknownLength) {
        this.conn = conn;
        this.unknownLength = unknownLength;
        this.oidToPgName = new HashMap<Integer, String>((int)Math.round((double)types.length * 1.5));
        this.pgNameToOid = new HashMap<String, Integer>((int)Math.round((double)types.length * 1.5));
        this.javaArrayTypeToOid = new HashMap<String, Integer>((int)Math.round((double)types.length * 1.5));
        this.pgNameToJavaClass = new HashMap<String, String>((int)Math.round((double)types.length * 1.5));
        this.pgNameToPgObject = new HashMap<String, Class<? extends PGobject>>((int)Math.round((double)types.length * 1.5));
        this.pgArrayToPgType = new HashMap<Integer, Integer>((int)Math.round((double)types.length * 1.5));
        this.arrayOidToDelimiter = new HashMap<Integer, Character>((int)Math.round((double)types.length * 2.5));
        this.pgNameToSQLType = Collections.synchronizedMap(new HashMap((int)Math.round((double)types.length * 1.5)));
        this.oidToSQLType = Collections.synchronizedMap(new HashMap((int)Math.round((double)types.length * 1.5)));
        for (Object[] type : types) {
            String pgTypeName = (String)type[0];
            Integer oid = (Integer)type[1];
            Integer sqlType = (Integer)type[2];
            String javaClass = (String)type[3];
            Integer arrayOid = (Integer)type[4];
            this.addCoreType(pgTypeName, oid, sqlType, javaClass, arrayOid);
        }
        this.pgNameToJavaClass.put("hstore", Map.class.getName());
    }

    @Override
    public synchronized void addCoreType(String pgTypeName, Integer oid, Integer sqlType, String javaClass, Integer arrayOid) {
        this.pgNameToJavaClass.put(pgTypeName, javaClass);
        this.pgNameToOid.put(pgTypeName, oid);
        this.oidToPgName.put(oid, pgTypeName);
        this.javaArrayTypeToOid.put(javaClass, arrayOid);
        this.pgArrayToPgType.put(arrayOid, oid);
        this.pgNameToSQLType.put(pgTypeName, sqlType);
        this.oidToSQLType.put(oid, sqlType);
        Character delim = Character.valueOf(',');
        this.arrayOidToDelimiter.put(oid, delim);
        this.arrayOidToDelimiter.put(arrayOid, delim);
        String pgArrayTypeName = pgTypeName + "[]";
        this.pgNameToJavaClass.put(pgArrayTypeName, "java.sql.Array");
        this.pgNameToSQLType.put(pgArrayTypeName, 2003);
        this.oidToSQLType.put(arrayOid, 2003);
        this.pgNameToOid.put(pgArrayTypeName, arrayOid);
        pgArrayTypeName = "_" + pgTypeName;
        if (!this.pgNameToJavaClass.containsKey(pgArrayTypeName)) {
            this.pgNameToJavaClass.put(pgArrayTypeName, "java.sql.Array");
            this.pgNameToSQLType.put(pgArrayTypeName, 2003);
            this.pgNameToOid.put(pgArrayTypeName, arrayOid);
            this.oidToPgName.put(arrayOid, pgArrayTypeName);
        }
    }

    @Override
    public synchronized void addDataType(String type, Class<? extends PGobject> klass) throws SQLException {
        this.pgNameToPgObject.put(type, klass);
        this.pgNameToJavaClass.put(type, klass.getName());
    }

    @Override
    public Iterator<String> getPGTypeNamesWithSQLTypes() {
        return this.pgNameToSQLType.keySet().iterator();
    }

    @Override
    public Iterator<Integer> getPGTypeOidsWithSQLTypes() {
        return this.oidToSQLType.keySet().iterator();
    }

    private String getSQLTypeQuery(boolean typoidParam) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT typinput='array_in'::regproc as is_array, typtype, typname, pg_type.oid ");
        sql.append("  FROM pg_catalog.pg_type ");
        sql.append("  LEFT JOIN (select ns.oid as nspoid, ns.nspname, r.r ");
        sql.append("          from pg_namespace as ns ");
        sql.append("          join ( select s.r, (current_schemas(false))[s.r] as nspname ");
        sql.append("                   from generate_series(1, array_upper(current_schemas(false), 1)) as s(r) ) as r ");
        sql.append("         using ( nspname ) ");
        sql.append("       ) as sp ");
        sql.append("    ON sp.nspoid = typnamespace ");
        if (typoidParam) {
            sql.append(" WHERE pg_type.oid = ? ");
        }
        sql.append(" ORDER BY sp.r, pg_type.oid DESC;");
        return sql.toString();
    }

    private int getSQLTypeFromQueryResult(ResultSet rs) throws SQLException {
        Integer type = null;
        boolean isArray = rs.getBoolean("is_array");
        String typtype = rs.getString("typtype");
        if (isArray) {
            type = 2003;
        } else if ("c".equals(typtype)) {
            type = 2002;
        } else if ("d".equals(typtype)) {
            type = 2001;
        } else if ("e".equals(typtype)) {
            type = 12;
        }
        if (type == null) {
            type = 1111;
        }
        return type;
    }

    private PreparedStatement prepareGetAllTypeInfoStatement() throws SQLException {
        PreparedStatement getAllTypeInfoStatement = this.getAllTypeInfoStatement;
        if (getAllTypeInfoStatement == null) {
            this.getAllTypeInfoStatement = getAllTypeInfoStatement = this.conn.prepareStatement(this.getSQLTypeQuery(false));
        }
        return getAllTypeInfoStatement;
    }

    public void cacheSQLTypes() throws SQLException {
        LOGGER.log(Level.FINEST, "caching all SQL typecodes");
        PreparedStatement getAllTypeInfoStatement = this.prepareGetAllTypeInfoStatement();
        if (!((BaseStatement)((Object)getAllTypeInfoStatement)).executeWithFlags(16)) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        ResultSet rs = Nullness.castNonNull(getAllTypeInfoStatement.getResultSet());
        while (rs.next()) {
            Integer typeOid;
            String typeName = Nullness.castNonNull(rs.getString("typname"));
            Integer type = this.getSQLTypeFromQueryResult(rs);
            if (!this.pgNameToSQLType.containsKey(typeName)) {
                this.pgNameToSQLType.put(typeName, type);
            }
            if (this.oidToSQLType.containsKey(typeOid = Integer.valueOf(this.longOidToInt(Nullness.castNonNull(rs.getLong("oid")))))) continue;
            this.oidToSQLType.put(typeOid, type);
        }
        rs.close();
    }

    private PreparedStatement prepareGetTypeInfoStatement() throws SQLException {
        PreparedStatement getTypeInfoStatement = this.getTypeInfoStatement;
        if (getTypeInfoStatement == null) {
            this.getTypeInfoStatement = getTypeInfoStatement = this.conn.prepareStatement(this.getSQLTypeQuery(true));
        }
        return getTypeInfoStatement;
    }

    @Override
    public synchronized int getSQLType(String pgTypeName) throws SQLException {
        if (pgTypeName.endsWith("[]")) {
            return 2003;
        }
        Integer i = this.pgNameToSQLType.get(pgTypeName);
        if (i != null) {
            return i;
        }
        i = this.getSQLType(Nullness.castNonNull(this.getPGType(pgTypeName)));
        this.pgNameToSQLType.put(pgTypeName, i);
        return i;
    }

    @Override
    public synchronized int getJavaArrayType(String className) throws SQLException {
        Integer oid = this.javaArrayTypeToOid.get(className);
        if (oid == null) {
            return 0;
        }
        return oid;
    }

    @Override
    public synchronized int getSQLType(int typeOid) throws SQLException {
        if (typeOid == 0) {
            return 1111;
        }
        Integer i = this.oidToSQLType.get(typeOid);
        if (i != null) {
            return i;
        }
        LOGGER.log(Level.FINEST, "querying SQL typecode for pg type oid '{0}'", this.intOidToLong(typeOid));
        PreparedStatement getTypeInfoStatement = this.prepareGetTypeInfoStatement();
        getTypeInfoStatement.setLong(1, this.intOidToLong(typeOid));
        if (!((BaseStatement)((Object)getTypeInfoStatement)).executeWithFlags(16)) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        ResultSet rs = Nullness.castNonNull(getTypeInfoStatement.getResultSet());
        int sqlType = 1111;
        if (rs.next()) {
            sqlType = this.getSQLTypeFromQueryResult(rs);
        }
        rs.close();
        this.oidToSQLType.put(typeOid, sqlType);
        return sqlType;
    }

    private PreparedStatement getOidStatement(String pgTypeName) throws SQLException {
        String name;
        String schema;
        String fullName;
        PreparedStatement oidStatementComplex;
        String sql;
        boolean isArray = pgTypeName.endsWith("[]");
        boolean hasQuote = pgTypeName.contains("\"");
        int dotIndex = pgTypeName.indexOf(46);
        if (dotIndex == -1 && !hasQuote && !isArray) {
            if (this.getOidStatementSimple == null) {
                String sql2 = "SELECT pg_type.oid, typname   FROM pg_catalog.pg_type   LEFT   JOIN (select ns.oid as nspoid, ns.nspname, r.r           from pg_namespace as ns           join ( select s.r, (current_schemas(false))[s.r] as nspname                    from generate_series(1, array_upper(current_schemas(false), 1)) as s(r) ) as r          using ( nspname )        ) as sp     ON sp.nspoid = typnamespace  WHERE typname = ?  ORDER BY sp.r, pg_type.oid DESC LIMIT 1;";
                this.getOidStatementSimple = this.conn.prepareStatement(sql2);
            }
            String lcName = pgTypeName.toLowerCase(Locale.ROOT);
            this.getOidStatementSimple.setString(1, lcName);
            return this.getOidStatementSimple;
        }
        if (isArray) {
            if (this.getOidStatementComplexArray == null) {
                sql = this.conn.haveMinimumServerVersion(ServerVersion.v8_3) ? "SELECT t.typarray, arr.typname   FROM pg_catalog.pg_type t  JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid  JOIN pg_catalog.pg_type arr ON arr.oid = t.typarray WHERE t.typname = ? AND (n.nspname = ? OR ? AND n.nspname = ANY (current_schemas(true))) ORDER BY t.oid DESC LIMIT 1" : "SELECT t.oid, t.typname   FROM pg_catalog.pg_type t  JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid WHERE t.typelem = (SELECT oid FROM pg_catalog.pg_type WHERE typname = ?) AND substring(t.typname, 1, 1) = '_' AND t.typlen = -1 AND (n.nspname = ? OR ? AND n.nspname = ANY (current_schemas(true))) ORDER BY t.typelem DESC LIMIT 1";
                this.getOidStatementComplexArray = this.conn.prepareStatement(sql);
            }
            oidStatementComplex = this.getOidStatementComplexArray;
        } else {
            if (this.getOidStatementComplexNonArray == null) {
                sql = "SELECT t.oid, t.typname   FROM pg_catalog.pg_type t  JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = ? AND (n.nspname = ? OR ? AND n.nspname = ANY (current_schemas(true))) ORDER BY t.oid DESC LIMIT 1";
                this.getOidStatementComplexNonArray = this.conn.prepareStatement(sql);
            }
            oidStatementComplex = this.getOidStatementComplexNonArray;
        }
        String string = fullName = isArray ? pgTypeName.substring(0, pgTypeName.length() - 2) : pgTypeName;
        if (dotIndex == -1) {
            schema = null;
            name = fullName;
        } else if (fullName.startsWith("\"")) {
            if (fullName.endsWith("\"")) {
                String[] parts = fullName.split("\"\\.\"");
                schema = parts.length == 2 ? parts[0] + "\"" : null;
                name = parts.length == 2 ? "\"" + parts[1] : parts[0];
            } else {
                int lastDotIndex = fullName.lastIndexOf(46);
                name = fullName.substring(lastDotIndex + 1);
                schema = fullName.substring(0, lastDotIndex);
            }
        } else {
            schema = fullName.substring(0, dotIndex);
            name = fullName.substring(dotIndex + 1);
        }
        if (schema != null && schema.startsWith("\"") && schema.endsWith("\"")) {
            schema = schema.substring(1, schema.length() - 1);
        } else if (schema != null) {
            schema = schema.toLowerCase(Locale.ROOT);
        }
        name = name.startsWith("\"") && name.endsWith("\"") ? name.substring(1, name.length() - 1) : name.toLowerCase(Locale.ROOT);
        oidStatementComplex.setString(1, name);
        oidStatementComplex.setString(2, schema);
        oidStatementComplex.setBoolean(3, schema == null);
        return oidStatementComplex;
    }

    @Override
    public synchronized int getPGType(String pgTypeName) throws SQLException {
        if (pgTypeName == null) {
            return 0;
        }
        Integer oid = this.pgNameToOid.get(pgTypeName);
        if (oid != null) {
            return oid;
        }
        PreparedStatement oidStatement = this.getOidStatement(pgTypeName);
        if (!((BaseStatement)((Object)oidStatement)).executeWithFlags(16)) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        oid = 0;
        ResultSet rs = Nullness.castNonNull(oidStatement.getResultSet());
        if (rs.next()) {
            oid = (int)rs.getLong(1);
            String internalName = Nullness.castNonNull(rs.getString(2));
            this.oidToPgName.put(oid, internalName);
            this.pgNameToOid.put(internalName, oid);
        }
        this.pgNameToOid.put(pgTypeName, oid);
        rs.close();
        return oid;
    }

    @Override
    public synchronized @Nullable String getPGType(int oid) throws SQLException {
        if (oid == 0) {
            return null;
        }
        String pgTypeName = this.oidToPgName.get(oid);
        if (pgTypeName != null) {
            return pgTypeName;
        }
        PreparedStatement getNameStatement = this.prepareGetNameStatement();
        getNameStatement.setInt(1, oid);
        if (!((BaseStatement)((Object)getNameStatement)).executeWithFlags(16)) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        ResultSet rs = Nullness.castNonNull(getNameStatement.getResultSet());
        if (rs.next()) {
            boolean onPath = rs.getBoolean(1);
            String schema = Nullness.castNonNull(rs.getString(2), "schema");
            String name = Nullness.castNonNull(rs.getString(3), "name");
            if (onPath) {
                pgTypeName = name;
                this.pgNameToOid.put(schema + "." + name, oid);
            } else {
                pgTypeName = "\"" + schema + "\".\"" + name + "\"";
                if (schema.equals(schema.toLowerCase(Locale.ROOT)) && schema.indexOf(46) == -1 && name.equals(name.toLowerCase(Locale.ROOT)) && name.indexOf(46) == -1) {
                    this.pgNameToOid.put(schema + "." + name, oid);
                }
            }
            this.pgNameToOid.put(pgTypeName, oid);
            this.oidToPgName.put(oid, pgTypeName);
        }
        rs.close();
        return pgTypeName;
    }

    private PreparedStatement prepareGetNameStatement() throws SQLException {
        PreparedStatement getNameStatement = this.getNameStatement;
        if (getNameStatement == null) {
            String sql = "SELECT n.nspname = ANY(current_schemas(true)), n.nspname, t.typname FROM pg_catalog.pg_type t JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid WHERE t.oid = ?";
            this.getNameStatement = getNameStatement = this.conn.prepareStatement(sql);
        }
        return getNameStatement;
    }

    @Override
    public int getPGArrayType(@Nullable String elementTypeName) throws SQLException {
        elementTypeName = this.getTypeForAlias(elementTypeName);
        return this.getPGType(elementTypeName + "[]");
    }

    protected synchronized int convertArrayToBaseOid(int oid) {
        Integer i = this.pgArrayToPgType.get(oid);
        if (i == null) {
            return oid;
        }
        return i;
    }

    @Override
    public synchronized char getArrayDelimiter(int oid) throws SQLException {
        if (oid == 0) {
            return ',';
        }
        Character delim = this.arrayOidToDelimiter.get(oid);
        if (delim != null) {
            return delim.charValue();
        }
        PreparedStatement getArrayDelimiterStatement = this.prepareGetArrayDelimiterStatement();
        getArrayDelimiterStatement.setInt(1, oid);
        if (!((BaseStatement)((Object)getArrayDelimiterStatement)).executeWithFlags(16)) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        ResultSet rs = Nullness.castNonNull(getArrayDelimiterStatement.getResultSet());
        if (!rs.next()) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        String s2 = Nullness.castNonNull(rs.getString(1));
        delim = Character.valueOf(s2.charAt(0));
        this.arrayOidToDelimiter.put(oid, delim);
        rs.close();
        return delim.charValue();
    }

    private PreparedStatement prepareGetArrayDelimiterStatement() throws SQLException {
        PreparedStatement getArrayDelimiterStatement = this.getArrayDelimiterStatement;
        if (getArrayDelimiterStatement == null) {
            String sql = "SELECT e.typdelim FROM pg_catalog.pg_type t, pg_catalog.pg_type e WHERE t.oid = ? and t.typelem = e.oid";
            this.getArrayDelimiterStatement = getArrayDelimiterStatement = this.conn.prepareStatement(sql);
        }
        return getArrayDelimiterStatement;
    }

    @Override
    public synchronized int getPGArrayElement(int oid) throws SQLException {
        if (oid == 0) {
            return 0;
        }
        Integer pgType = this.pgArrayToPgType.get(oid);
        if (pgType != null) {
            return pgType;
        }
        PreparedStatement getArrayElementOidStatement = this.prepareGetArrayElementOidStatement();
        getArrayElementOidStatement.setInt(1, oid);
        if (!((BaseStatement)((Object)getArrayElementOidStatement)).executeWithFlags(16)) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        ResultSet rs = Nullness.castNonNull(getArrayElementOidStatement.getResultSet());
        if (!rs.next()) {
            throw new PSQLException(GT.tr("No results were returned by the query.", new Object[0]), PSQLState.NO_DATA);
        }
        pgType = (int)rs.getLong(1);
        boolean onPath = rs.getBoolean(2);
        String schema = rs.getString(3);
        String name = Nullness.castNonNull(rs.getString(4));
        this.pgArrayToPgType.put(oid, pgType);
        this.pgNameToOid.put(schema + "." + name, pgType);
        String fullName = "\"" + schema + "\".\"" + name + "\"";
        this.pgNameToOid.put(fullName, pgType);
        if (onPath && name.equals(name.toLowerCase(Locale.ROOT))) {
            this.oidToPgName.put(pgType, name);
            this.pgNameToOid.put(name, pgType);
        } else {
            this.oidToPgName.put(pgType, fullName);
        }
        rs.close();
        return pgType;
    }

    private PreparedStatement prepareGetArrayElementOidStatement() throws SQLException {
        PreparedStatement getArrayElementOidStatement = this.getArrayElementOidStatement;
        if (getArrayElementOidStatement == null) {
            String sql = "SELECT e.oid, n.nspname = ANY(current_schemas(true)), n.nspname, e.typname FROM pg_catalog.pg_type t JOIN pg_catalog.pg_type e ON t.typelem = e.oid JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid WHERE t.oid = ?";
            this.getArrayElementOidStatement = getArrayElementOidStatement = this.conn.prepareStatement(sql);
        }
        return getArrayElementOidStatement;
    }

    @Override
    public synchronized @Nullable Class<? extends PGobject> getPGobject(String type) {
        return this.pgNameToPgObject.get(type);
    }

    @Override
    public synchronized String getJavaClass(int oid) throws SQLException {
        String pgTypeName = this.getPGType(oid);
        if (pgTypeName == null) {
            return "java.lang.String";
        }
        String result = this.pgNameToJavaClass.get(pgTypeName);
        if (result != null) {
            return result;
        }
        if (this.getSQLType(pgTypeName) == 2003) {
            result = "java.sql.Array";
            this.pgNameToJavaClass.put(pgTypeName, result);
        }
        return result == null ? "java.lang.String" : result;
    }

    @Override
    public @Nullable String getTypeForAlias(@Nullable String alias) {
        if (alias == null) {
            return null;
        }
        String type = (String)TYPE_ALIASES.get(alias);
        if (type != null) {
            return type;
        }
        type = (String)TYPE_ALIASES.get(alias.toLowerCase(Locale.ROOT));
        if (type == null) {
            type = alias;
        }
        TYPE_ALIASES.put(alias, type);
        return type;
    }

    @Override
    public int getPrecision(int oid, int typmod) {
        oid = this.convertArrayToBaseOid(oid);
        switch (oid) {
            case 21: {
                return 5;
            }
            case 23: 
            case 26: {
                return 10;
            }
            case 20: {
                return 19;
            }
            case 700: {
                return 8;
            }
            case 701: {
                return 17;
            }
            case 1700: {
                if (typmod == -1) {
                    return 0;
                }
                return (typmod - 4 & 0xFFFF0000) >> 16;
            }
            case 16: 
            case 18: {
                return 1;
            }
            case 1042: 
            case 1043: {
                if (typmod == -1) {
                    return this.unknownLength;
                }
                return typmod - 4;
            }
            case 1082: 
            case 1083: 
            case 1114: 
            case 1184: 
            case 1186: 
            case 1266: {
                return this.getDisplaySize(oid, typmod);
            }
            case 1560: {
                return typmod;
            }
            case 1562: {
                if (typmod == -1) {
                    return this.unknownLength;
                }
                return typmod;
            }
        }
        return this.unknownLength;
    }

    @Override
    public int getScale(int oid, int typmod) {
        oid = this.convertArrayToBaseOid(oid);
        switch (oid) {
            case 700: {
                return 8;
            }
            case 701: {
                return 17;
            }
            case 1700: {
                if (typmod == -1) {
                    return 0;
                }
                return typmod - 4 & 0xFFFF;
            }
            case 1083: 
            case 1114: 
            case 1184: 
            case 1266: {
                if (typmod == -1) {
                    return 6;
                }
                return typmod;
            }
            case 1186: {
                if (typmod == -1) {
                    return 6;
                }
                return typmod & 0xFFFF;
            }
        }
        return 0;
    }

    @Override
    public boolean isCaseSensitive(int oid) {
        oid = this.convertArrayToBaseOid(oid);
        switch (oid) {
            case 16: 
            case 20: 
            case 21: 
            case 23: 
            case 26: 
            case 700: 
            case 701: 
            case 1082: 
            case 1083: 
            case 1114: 
            case 1184: 
            case 1186: 
            case 1266: 
            case 1560: 
            case 1562: 
            case 1700: {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSigned(int oid) {
        oid = this.convertArrayToBaseOid(oid);
        switch (oid) {
            case 20: 
            case 21: 
            case 23: 
            case 700: 
            case 701: 
            case 1700: {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getDisplaySize(int oid, int typmod) {
        oid = this.convertArrayToBaseOid(oid);
        switch (oid) {
            case 21: {
                return 6;
            }
            case 23: {
                return 11;
            }
            case 26: {
                return 10;
            }
            case 20: {
                return 20;
            }
            case 700: {
                return 15;
            }
            case 701: {
                return 25;
            }
            case 18: {
                return 1;
            }
            case 16: {
                return 1;
            }
            case 1082: {
                return 13;
            }
            case 1083: 
            case 1114: 
            case 1184: 
            case 1266: {
                int secondSize;
                switch (typmod) {
                    case -1: {
                        secondSize = 7;
                        break;
                    }
                    case 0: {
                        secondSize = 0;
                        break;
                    }
                    case 1: {
                        secondSize = 3;
                        break;
                    }
                    default: {
                        secondSize = typmod + 1;
                    }
                }
                switch (oid) {
                    case 1083: {
                        return 8 + secondSize;
                    }
                    case 1266: {
                        return 8 + secondSize + 6;
                    }
                    case 1114: {
                        return 22 + secondSize;
                    }
                    case 1184: {
                        return 22 + secondSize + 6;
                    }
                }
            }
            case 1186: {
                return 49;
            }
            case 1042: 
            case 1043: {
                if (typmod == -1) {
                    return this.unknownLength;
                }
                return typmod - 4;
            }
            case 1700: {
                if (typmod == -1) {
                    return 131089;
                }
                int precision = typmod - 4 >> 16 & 0xFFFF;
                int scale = typmod - 4 & 0xFFFF;
                return 1 + precision + (scale != 0 ? 1 : 0);
            }
            case 1560: {
                return typmod;
            }
            case 1562: {
                if (typmod == -1) {
                    return this.unknownLength;
                }
                return typmod;
            }
            case 17: 
            case 25: {
                return this.unknownLength;
            }
        }
        return this.unknownLength;
    }

    @Override
    public int getMaximumPrecision(int oid) {
        oid = this.convertArrayToBaseOid(oid);
        switch (oid) {
            case 1700: {
                return 1000;
            }
            case 1083: 
            case 1266: {
                return 6;
            }
            case 1114: 
            case 1184: 
            case 1186: {
                return 6;
            }
            case 1042: 
            case 1043: {
                return 0xA00000;
            }
            case 1560: 
            case 1562: {
                return 0x5000000;
            }
        }
        return 0;
    }

    @Override
    public boolean requiresQuoting(int oid) throws SQLException {
        int sqlType = this.getSQLType(oid);
        return this.requiresQuotingSqlType(sqlType);
    }

    @Override
    public boolean requiresQuotingSqlType(int sqlType) throws SQLException {
        switch (sqlType) {
            case -6: 
            case -5: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 7: 
            case 8: {
                return false;
            }
        }
        return true;
    }

    @Override
    public int longOidToInt(long oid) throws SQLException {
        if ((oid & 0xFFFFFFFF00000000L) != 0L) {
            throw new PSQLException(GT.tr("Value is not an OID: {0}", oid), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
        }
        return (int)oid;
    }

    @Override
    public long intOidToLong(int oid) {
        return (long)oid & 0xFFFFFFFFL;
    }

    static {
        TYPE_ALIASES.put("bool", "bool");
        TYPE_ALIASES.put("boolean", "bool");
        TYPE_ALIASES.put("smallint", "int2");
        TYPE_ALIASES.put("int2", "int2");
        TYPE_ALIASES.put("int", "int4");
        TYPE_ALIASES.put("integer", "int4");
        TYPE_ALIASES.put("int4", "int4");
        TYPE_ALIASES.put("long", "int8");
        TYPE_ALIASES.put("int8", "int8");
        TYPE_ALIASES.put("bigint", "int8");
        TYPE_ALIASES.put("float", "float4");
        TYPE_ALIASES.put("float4", "float4");
        TYPE_ALIASES.put("double", "float8");
        TYPE_ALIASES.put("float8", "float8");
        TYPE_ALIASES.put("decimal", "numeric");
        TYPE_ALIASES.put("numeric", "numeric");
    }
}

