/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.types.BinaryType$
 *  org.apache.spark.sql.types.BooleanType$
 *  org.apache.spark.sql.types.DataType
 *  org.apache.spark.sql.types.DateType$
 *  org.apache.spark.sql.types.DecimalType
 *  org.apache.spark.sql.types.DecimalType$
 *  org.apache.spark.sql.types.DoubleType$
 *  org.apache.spark.sql.types.FloatType$
 *  org.apache.spark.sql.types.IntegerType$
 *  org.apache.spark.sql.types.LongType$
 *  org.apache.spark.sql.types.ShortType$
 *  org.apache.spark.sql.types.StringType$
 *  org.apache.spark.sql.types.StructField
 *  org.apache.spark.sql.types.StructField$
 *  org.apache.spark.sql.types.StructType
 *  org.apache.spark.sql.types.TimestampType$
 *  scala.Function0
 *  scala.Function1
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Option$
 *  scala.PartialFunction
 *  scala.Predef$
 *  scala.Serializable
 *  scala.Some
 *  scala.Tuple2
 *  scala.collection.IterableLike
 *  scala.collection.Seq
 *  scala.collection.Seq$
 *  scala.collection.TraversableOnce
 *  scala.collection.immutable.IndexedSeq$
 *  scala.collection.immutable.StringOps
 *  scala.collection.immutable.Vector
 *  scala.collection.immutable.Vector$
 *  scala.package$
 *  scala.reflect.ClassManifestFactory$
 *  scala.reflect.ClassTag
 *  scala.reflect.ClassTag$
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.RichInt$
 *  scala.runtime.java8.JFunction1$mcVI$sp
 *  scala.util.Failure
 *  scala.util.Success
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.jdbc;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.GreenplumCSVFormat$;
import io.pivotal.greenplum.spark.externaltable.GpfdistLocation;
import io.pivotal.greenplum.spark.externaltable.GreenplumQualifiedName;
import io.pivotal.greenplum.spark.externaltable.SqlObjectNameUtils$;
import io.pivotal.greenplum.spark.jdbc.ColumnValueRange;
import io.pivotal.greenplum.spark.jdbc.ColumnValueRange$;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.spark.sql.types.BinaryType$;
import org.apache.spark.sql.types.BooleanType$;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DateType$;
import org.apache.spark.sql.types.DecimalType;
import org.apache.spark.sql.types.DecimalType$;
import org.apache.spark.sql.types.DoubleType$;
import org.apache.spark.sql.types.FloatType$;
import org.apache.spark.sql.types.IntegerType$;
import org.apache.spark.sql.types.LongType$;
import org.apache.spark.sql.types.ShortType$;
import org.apache.spark.sql.types.StringType$;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructField$;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.types.TimestampType$;
import resource.ExtractableManagedResource;
import resource.ManagedResource;
import resource.Resource$;
import scala.Function0;
import scala.Function1;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Option$;
import scala.PartialFunction;
import scala.Predef$;
import scala.Some;
import scala.Tuple2;
import scala.collection.IterableLike;
import scala.collection.Seq;
import scala.collection.Seq$;
import scala.collection.TraversableOnce;
import scala.collection.immutable.IndexedSeq$;
import scala.collection.immutable.StringOps;
import scala.collection.immutable.Vector;
import scala.collection.immutable.Vector$;
import scala.package$;
import scala.reflect.ClassManifestFactory$;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.runtime.RichInt$;
import scala.runtime.java8.JFunction1;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;

public final class Jdbc$
implements LazyLogging {
    public static Jdbc$ MODULE$;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    static {
        new Jdbc$();
    }

    private Logger logger$lzycompute() {
        Jdbc$ jdbc$ = this;
        synchronized (jdbc$) {
            if (!this.bitmap$trans$0) {
                this.logger = LazyLogging.logger$(this);
                this.bitmap$trans$0 = true;
            }
        }
        return this.logger;
    }

    @Override
    public Logger logger() {
        if (!this.bitmap$trans$0) {
            return this.logger$lzycompute();
        }
        return this.logger;
    }

    public void copyTableToExternal(Connection conn, GreenplumQualifiedName srcTable, GreenplumQualifiedName.TempTable extTable, String predicate, Seq<String> columns) {
        String colNames = columns.isEmpty() ? "1" : ((TraversableOnce)columns.map((Function1 & Serializable & scala.Serializable)colName -> SqlObjectNameUtils$.MODULE$.escape((String)colName), Seq$.MODULE$.canBuildFrom())).mkString(",");
        String sqlQuery = new StringBuilder(33).append("INSERT INTO ").append(extTable).append(" SELECT ").append(colNames).append(" FROM ").append(srcTable).append(" WHERE ").append(predicate).toString();
        ExtractableManagedResource result = resource.package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> conn.createStatement(), Resource$.MODULE$.statementResource(), ClassManifestFactory$.MODULE$.classType(Statement.class)).map((Function1 & Serializable & scala.Serializable)statement -> {
            rowsCopied = statement.executeUpdate(sqlQuery);
            return BoxedUnit.UNIT;
        });
        result.tried().get();
    }

    public boolean externalTableExists(Connection conn, GreenplumQualifiedName.TempTable table) {
        String sqlQuery = "SELECT EXISTS ( SELECT 1 FROM information_schema.tables WHERE table_name = ? );";
        ManagedResource result = resource.package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> {
            PreparedStatement prepared = conn.prepareStatement(sqlQuery);
            prepared.setString(1, table.name());
            return prepared;
        }, Resource$.MODULE$.statementResource(), ClassManifestFactory$.MODULE$.classType(PreparedStatement.class)).flatMap((Function1 & Serializable & scala.Serializable)statement -> resource.package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> statement.executeQuery(), Resource$.MODULE$.resultSetResource(), ClassManifestFactory$.MODULE$.classType(ResultSet.class)).map((Function1 & Serializable & scala.Serializable)resultSet -> resultSet));
        return BoxesRunTime.unboxToBoolean(result.acquireAndGet((Function1 & Serializable & scala.Serializable)rs -> BoxesRunTime.boxToBoolean((boolean)Jdbc$.$anonfun$externalTableExists$5(rs))));
    }

    public Seq<String> getColumnsMetadata(Connection conn, GreenplumQualifiedName.Table table, Seq<String> columnNames) {
        String schema = table.schema();
        String srcTable = table.name();
        String sqlQuery = this.formatSqlQuery(new StringBuilder(489).append("\n         |SELECT '\"' || a.attname || '\" ' ||\n         |pg_catalog.format_type(a.atttypid, a.atttypmod) as column_metadata\n         |FROM pg_catalog.pg_attribute a, pg_class b, pg_namespace n\n         |WHERE n.nspname = ?\n         |AND n.oid = b.relnamespace\n         |AND a.attrelid = b.oid\n         |AND b.relname = ?\n         |AND a.attnum > 0 AND NOT a.attisdropped\n         |AND a.attname = ANY (?)\n         |ORDER BY strpos(\n         |E'\\'").append(columnNames.mkString("\\',\\'")).append("\\'',\n         |E'\\'' || a.attname || E'\\'');").toString());
        Seq results = this.retrieveResults(conn, sqlQuery, (Seq<Object>)Predef$.MODULE$.genericWrapArray((Object)new Object[]{schema, srcTable, conn.createArrayOf("text", (Object[])columnNames.toArray(ClassTag$.MODULE$.Object()))}), (Function1 & Serializable & scala.Serializable)x$1 -> x$1.getString("column_metadata"), ClassTag$.MODULE$.apply(String.class));
        if (results.size() != columnNames.size()) {
            throw new RuntimeException(new StringBuilder(35).append("Error retrieving metadata for ").append(columnNames.mkString(",")).append(" for ").append(srcTable).toString());
        }
        return results;
    }

    public String getDistributionPolicyGp5(Connection conn, GreenplumQualifiedName.Table table) {
        String gp5DistributionColName = "distribution_cols";
        String sqlQuery = this.formatSqlQuery(new StringBuilder(647).append("\n         |SELECT a.attname as ").append(gp5DistributionColName).append("\n         |FROM (\n         |    SELECT *, row_number() over () as order FROM (\n         |        SELECT b.oid, unnest(d.attrnums) as key\n         |        FROM pg_class b, pg_namespace n, gp_distribution_policy d\n         |        WHERE n.nspname = ?\n         |        AND n.oid = b.relnamespace\n         |        AND b.relname = ?\n         |        AND b.oid = d.localoid\n         |    ) as info\n         |) as dist_keys, pg_catalog.pg_attribute a\n         |WHERE dist_keys.oid = a.attrelid\n         |AND a.attnum > 0 AND NOT a.attisdropped\n         |AND a.attnum = dist_keys.key\n         |ORDER BY dist_keys.order;").toString());
        Seq results = this.retrieveResults(conn, sqlQuery, (Seq<Object>)Predef$.MODULE$.genericWrapArray((Object)new Object[]{table.schema(), table.name()}), (Function1 & Serializable & scala.Serializable)x$2 -> x$2.getString(gp5DistributionColName), ClassTag$.MODULE$.apply(String.class));
        if (results.isEmpty()) {
            BoxedUnit boxedUnit;
            if (this.logger().underlying().isDebugEnabled()) {
                this.logger().underlying().debug("No distribution columns found for {}; assuming random distribution", new Object[]{table});
                boxedUnit = BoxedUnit.UNIT;
            } else {
                boxedUnit = BoxedUnit.UNIT;
            }
            return "DISTRIBUTED RANDOMLY";
        }
        return new StringBuilder(17).append("DISTRIBUTED BY (").append(results.mkString(", ")).append(")").toString();
    }

    public String getDistributionPolicy(Connection conn, GreenplumQualifiedName.Table table) {
        String distributionPolicyColName = "distribution_policy";
        String sqlQuery = this.formatSqlQuery(new StringBuilder(215).append("\n         |SELECT pg_catalog.pg_get_table_distributedBy(b.oid) as ").append(distributionPolicyColName).append("\n         |FROM pg_class b, pg_namespace n\n         |WHERE n.nspname = ?\n         |AND n.oid = b.relnamespace\n         |AND b.relname = ?;\n         |").toString());
        Seq results = this.retrieveResults(conn, sqlQuery, (Seq<Object>)Predef$.MODULE$.genericWrapArray((Object)new Object[]{table.schema(), table.name()}), (Function1 & Serializable & scala.Serializable)x$3 -> x$3.getString(distributionPolicyColName), ClassTag$.MODULE$.apply(String.class));
        if (results.size() < 1) {
            throw new RuntimeException(new StringBuilder(41).append("Error retrieving distribution policy for ").append(table).toString());
        }
        if (((String)results.head()).isEmpty()) {
            BoxedUnit boxedUnit;
            if (this.logger().underlying().isDebugEnabled()) {
                this.logger().underlying().debug("No distribution columns found for {}; assuming random distribution", new Object[]{table});
                boxedUnit = BoxedUnit.UNIT;
            } else {
                boxedUnit = BoxedUnit.UNIT;
            }
            return "DISTRIBUTED RANDOMLY";
        }
        return (String)results.head();
    }

    public String determineDistributionPolicy(Connection conn, GreenplumQualifiedName.Table table) {
        int postgresMajorVersion = conn.getMetaData().getDatabaseMajorVersion();
        if (postgresMajorVersion < 9) {
            return this.getDistributionPolicyGp5(conn, table);
        }
        return this.getDistributionPolicy(conn, table);
    }

    public void createGpfdistWritableExternalTable(Connection conn, GreenplumQualifiedName.Table srcTable, GreenplumQualifiedName.TempTable extTable, GpfdistLocation gpfdistLocation, Seq<String> columns, String distributionPolicy) {
        BoxedUnit boxedUnit;
        String url = gpfdistLocation.getUrl();
        String projectedColumnsMetadata = columns.isEmpty() ? "dummy int" : this.getColumnsMetadata(conn, srcTable, columns).mkString(",");
        String sqlQuery = this.formatSqlQuery(new StringBuilder(178).append("CREATE WRITABLE EXTERNAL TEMP TABLE ").append(extTable).append(" (").append(projectedColumnsMetadata).append(")\n           |LOCATION ('").append(url).append("')\n           |FORMAT 'CSV'\n           |(DELIMITER '").append(GreenplumCSVFormat$.MODULE$.CHAR_DELIMITER()).append("'\n           | NULL AS '").append(GreenplumCSVFormat$.MODULE$.VALUE_OF_NULL()).append("')\n           |ENCODING '").append(GreenplumCSVFormat$.MODULE$.DEFAULT_ENCODING()).append("'\n           |").append(distributionPolicy).toString());
        if (this.logger().underlying().isDebugEnabled()) {
            this.logger().underlying().debug(sqlQuery);
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        resource.package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> conn.createStatement(), Resource$.MODULE$.statementResource(), ClassManifestFactory$.MODULE$.classType(Statement.class)).foreach((Function1 & Serializable & scala.Serializable)statement -> {
            statement.execute(sqlQuery);
            return BoxedUnit.UNIT;
        });
    }

    public int[] retrieveSegmentIds(Connection conn) {
        String sql = "SELECT content FROM gp_segment_configuration WHERE content != -1 AND role = 'p' ORDER BY content;";
        return (int[])this.retrieveResults(conn, sql, (Seq<Object>)Predef$.MODULE$.genericWrapArray((Object)new Object[0]), (Function1 & Serializable & scala.Serializable)x$4 -> BoxesRunTime.boxToInteger((int)x$4.getInt("content")), ClassTag$.MODULE$.Int()).toArray(ClassTag$.MODULE$.Int());
    }

    public <T> Seq<T> retrieveResults(Connection conn, String sqlQuery, Seq<Object> sqlParameters, Function1<ResultSet, T> getter, ClassTag<T> evidence$1) {
        ManagedResource result = resource.package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> {
            PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery);
            ((IterableLike)sqlParameters.zipWithIndex(Seq$.MODULE$.canBuildFrom())).foreach((Function1 & Serializable & scala.Serializable)x0$1 -> {
                Jdbc$.$anonfun$retrieveResults$2(preparedStatement, sqlQuery, x0$1);
                return BoxedUnit.UNIT;
            });
            return preparedStatement;
        }, Resource$.MODULE$.statementResource(), ClassManifestFactory$.MODULE$.classType(PreparedStatement.class)).flatMap((Function1 & Serializable & scala.Serializable)statement -> resource.package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> statement.executeQuery(), Resource$.MODULE$.resultSetResource(), ClassManifestFactory$.MODULE$.classType(ResultSet.class)).map((Function1 & Serializable & scala.Serializable)resultSet -> resultSet));
        return (Seq)result.acquireAndGet((Function1 & Serializable & scala.Serializable)rs -> MODULE$.collectFrom((ResultSet)rs, getter, evidence$1));
    }

    public ColumnValueRange computeColumnValueRange(Connection conn, GreenplumQualifiedName.Table table, String columnName) {
        String tableName = table.name();
        String sqlStatSubQuery = this.formatSqlQuery(new StringBuilder(325).append("SELECT CAST(UNNEST(string_to_array(array_to_string\n       |             (most_common_vals || histogram_bounds, ','), ',')) AS\n       |              NUMERIC) AS combined_histogram\n       |      FROM pg_stats ps\n       |      WHERE tablename = '").append(tableName).append("'\n       |      AND ps.attname = '").append(columnName).append("'\n       |      AND histogram_bounds IS NOT NULL").toString());
        String sqlStatQuery = this.formatSqlQuery(new StringBuilder(248).append("\n         |SELECT\n         |  MIN(combined_histogram) AS col_min,\n         |  MAX(combined_histogram) AS col_max\n         |FROM (").append(sqlStatSubQuery).append(") t\n         |      HAVING MIN(combined_histogram) IS NOT NULL\n         |      AND MAX(combined_histogram) IS NOT NULL;").toString());
        return (ColumnValueRange)this.io$pivotal$greenplum$spark$jdbc$Jdbc$$queryColumnValueRange(conn, sqlStatQuery).recoverWith((PartialFunction)new scala.Serializable(tableName, columnName, table, conn){
            public static final long serialVersionUID = 0L;
            private final String tableName$1;
            private final String columnName$1;
            private final GreenplumQualifiedName.Table table$2;
            private final Connection conn$5;

            public final <A1 extends Throwable, B1> B1 applyOrElse(A1 x1, Function1<A1, B1> function1) {
                A1 A1 = x1;
                if (A1 != null) {
                    BoxedUnit boxedUnit;
                    BoxedUnit boxedUnit2;
                    A1 A12 = A1;
                    if (Jdbc$.MODULE$.logger().underlying().isDebugEnabled()) {
                        Jdbc$.MODULE$.logger().underlying().debug("unable to use approximate stats because {}", new Object[]{A12.getMessage()});
                        boxedUnit2 = BoxedUnit.UNIT;
                    } else {
                        boxedUnit2 = BoxedUnit.UNIT;
                    }
                    if (Jdbc$.MODULE$.logger().underlying().isInfoEnabled()) {
                        Jdbc$.MODULE$.logger().underlying().info("Using accurate aggregated data for column {}.{} range", new String[]{this.tableName$1, this.columnName$1});
                        boxedUnit = BoxedUnit.UNIT;
                    } else {
                        boxedUnit = BoxedUnit.UNIT;
                    }
                    String sqlAggQuery = new StringBuilder(52).append("SELECT MIN(\"").append(this.columnName$1).append("\") AS col_min, MAX(\"").append(this.columnName$1).append("\") AS col_max FROM ").append(this.table$2).append(";").toString();
                    return (B1)Jdbc$.MODULE$.io$pivotal$greenplum$spark$jdbc$Jdbc$$queryColumnValueRange(this.conn$5, sqlAggQuery);
                }
                return (B1)function1.apply(x1);
            }

            public final boolean isDefinedAt(Throwable x1) {
                Throwable throwable = x1;
                return throwable != null;
            }
            {
                this.tableName$1 = tableName$1;
                this.columnName$1 = columnName$1;
                this.table$2 = table$2;
                this.conn$5 = conn$5;
            }
        }).transform((Function1 & Serializable & scala.Serializable)s2 -> new Success(s2), (Function1 & Serializable & scala.Serializable)e -> new Failure((Throwable)new IllegalArgumentException(new StringBuilder(40).append("Unable to determine range for a column ").append(tableName).append(".").append(columnName).toString(), (Throwable)e))).get();
    }

    public Try<ColumnValueRange> io$pivotal$greenplum$spark$jdbc$Jdbc$$queryColumnValueRange(Connection conn, String sqlQuery) {
        Try try_ = resource.package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> conn.createStatement(), Resource$.MODULE$.statementResource(), ClassManifestFactory$.MODULE$.classType(Statement.class)).flatMap((Function1 & Serializable & scala.Serializable)stmt -> resource.package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> stmt.executeQuery(sqlQuery), Resource$.MODULE$.resultSetResource(), ClassManifestFactory$.MODULE$.classType(ResultSet.class))).map((Function1 & Serializable & scala.Serializable)resultSet -> {
            Vector columnValueRanges = MODULE$.collectFrom((ResultSet)resultSet, (Function1 & Serializable & scala.Serializable)rs -> MODULE$.parseColumnValueRange((ResultSet)rs), (ClassTag)ClassTag$.MODULE$.apply(Try.class));
            Predef$.MODULE$.require(columnValueRanges.length() == 1, (Function0 & Serializable & scala.Serializable)() -> new StringBuilder(35).append("Expected exactly one row; received ").append(columnValueRanges.length()).toString());
            return (Try)columnValueRanges.head();
        }).tried();
        if (try_ instanceof Success) {
            Success success = (Success)try_;
            Try value = (Try)success.value();
            return value;
        }
        if (try_ instanceof Failure) {
            Failure failure = (Failure)try_;
            Throwable exception = failure.exception();
            return new Failure(exception);
        }
        throw new MatchError(try_);
    }

    public Try<ColumnValueRange> parseColumnValueRange(ResultSet rs) {
        Option colMax;
        Option colMin = Option$.MODULE$.apply((Object)rs.getBigDecimal("col_min"));
        Tuple2 tuple2 = new Tuple2((Object)colMin, (Object)(colMax = Option$.MODULE$.apply((Object)rs.getBigDecimal("col_max"))));
        if (tuple2 != null) {
            Option option = (Option)tuple2._1();
            Option option2 = (Option)tuple2._2();
            if (option instanceof Some) {
                Some some = (Some)option;
                BigDecimal min2 = (BigDecimal)some.value();
                if (option2 instanceof Some) {
                    Some some2 = (Some)option2;
                    BigDecimal max = (BigDecimal)some2.value();
                    return new Success((Object)ColumnValueRange$.MODULE$.apply(min2, max));
                }
            }
        }
        if (tuple2 != null) {
            Option option = (Option)tuple2._1();
            Option option3 = (Option)tuple2._2();
            if (None$.MODULE$.equals(option) && None$.MODULE$.equals(option3)) {
                return new Success((Object)ColumnValueRange$.MODULE$.apply(0L, 0L));
            }
        }
        return new Failure((Throwable)new RuntimeException("Error finding min or max of the specified partition column"));
    }

    public StructType resolveTable(Connection conn, String url, GreenplumQualifiedName table) {
        StructType structType;
        String sql = new StringBuilder(22).append("SELECT * FROM ").append(table).append(" LIMIT 0").toString();
        try (Statement statement = conn.createStatement();){
            statement.execute("set optimizer = off;");
            try (ResultSet rs = statement.executeQuery(sql);){
                structType = this.getSchema(rs.getMetaData());
            }
        }
        return structType;
    }

    public Seq<String> getColumnNames(ResultSetMetaData meta) {
        return (Seq)RichInt$.MODULE$.to$extension0(Predef$.MODULE$.intWrapper(1), meta.getColumnCount()).map((Function1 & Serializable & scala.Serializable)i -> meta.getColumnLabel(BoxesRunTime.unboxToInt((Object)i)), IndexedSeq$.MODULE$.canBuildFrom());
    }

    public StructType getSchema(ResultSetMetaData meta) {
        StructField[] fields = new StructField[meta.getColumnCount()];
        RichInt$.MODULE$.to$extension0(Predef$.MODULE$.intWrapper(1), meta.getColumnCount()).foreach$mVc$sp((Function1)(JFunction1.mcVI.sp & Serializable & scala.Serializable)i -> {
            boolean bl;
            String columnLabel = meta.getColumnLabel(i);
            int dataType = meta.getColumnType(i);
            int fieldSize = meta.getPrecision(i);
            int fieldScale = meta.getScale(i);
            boolean isSigned = meta.isSigned(i);
            int n = meta.isNullable(i);
            if (1 == n) {
                bl = true;
            } else if (0 == n) {
                bl = false;
            } else {
                if (2 == n ? true : true) {
                    throw new SQLException(new StringBuilder(42).append("Could not determine if column ").append(columnLabel).append(" is nullable").toString());
                }
                throw new MatchError((Object)BoxesRunTime.boxToInteger((int)n));
            }
            boolean nullable = bl;
            DataType columnType = MODULE$.getCatalystType(dataType, fieldSize, fieldScale, isSigned);
            fields$1[i - 1] = new StructField(columnLabel, columnType, nullable, StructField$.MODULE$.apply$default$4());
        });
        return new StructType(fields);
    }

    private DataType getCatalystType(int sqlType, int precision, int scale, boolean signed) {
        BinaryType$ binaryType$;
        int n = sqlType;
        switch (n) {
            case 0: 
            case 70: 
            case 1111: 
            case 2000: 
            case 2001: 
            case 2003: 
            case 2012: 
            case 2013: 
            case 2014: {
                binaryType$ = null;
                break;
            }
            case -5: {
                if (signed) {
                    binaryType$ = LongType$.MODULE$;
                    break;
                }
                binaryType$ = new DecimalType(20, 0);
                break;
            }
            case -4: 
            case -3: 
            case -2: 
            case 2004: {
                binaryType$ = BinaryType$.MODULE$;
                break;
            }
            case -7: {
                if (precision == 1) {
                    binaryType$ = BooleanType$.MODULE$;
                    break;
                }
                binaryType$ = BinaryType$.MODULE$;
                break;
            }
            case 16: {
                binaryType$ = BooleanType$.MODULE$;
                break;
            }
            case -16: 
            case -15: 
            case -9: 
            case -1: 
            case 1: 
            case 12: 
            case 2002: 
            case 2005: 
            case 2006: 
            case 2009: 
            case 2011: {
                binaryType$ = StringType$.MODULE$;
                break;
            }
            case 91: {
                binaryType$ = DateType$.MODULE$;
                break;
            }
            case 5: {
                binaryType$ = ShortType$.MODULE$;
                break;
            }
            case -6: {
                binaryType$ = IntegerType$.MODULE$;
                break;
            }
            case 2: 
            case 3: {
                if (precision != 0 || scale != 0) {
                    binaryType$ = new DecimalType(precision, scale);
                    break;
                }
                binaryType$ = DecimalType$.MODULE$.SYSTEM_DEFAULT();
                break;
            }
            case 8: {
                binaryType$ = DoubleType$.MODULE$;
                break;
            }
            case 6: 
            case 7: {
                binaryType$ = FloatType$.MODULE$;
                break;
            }
            case 4: {
                if (signed) {
                    binaryType$ = IntegerType$.MODULE$;
                    break;
                }
                binaryType$ = LongType$.MODULE$;
                break;
            }
            case -8: {
                binaryType$ = LongType$.MODULE$;
                break;
            }
            case 92: 
            case 93: {
                binaryType$ = TimestampType$.MODULE$;
                break;
            }
            default: {
                throw new SQLException(new StringBuilder(22).append("Unrecognized SQL type ").append(sqlType).toString());
            }
        }
        BinaryType$ answer = binaryType$;
        if (answer == null) {
            throw new IllegalArgumentException(new StringBuilder(17).append("Unsupported type ").append(JDBCType.valueOf(sqlType).getName()).toString());
        }
        return answer;
    }

    private <T> Vector<T> collectFrom(ResultSet resultSet, Function1<ResultSet, T> getter, ClassTag<T> evidence$2) {
        return this.inner$1(package$.MODULE$.Vector().empty(), resultSet, getter);
    }

    public String getDistributedTransactionId(Connection conn) {
        String sqlQuery = this.formatSqlQuery("SELECT x.distributed_id\n        |FROM pg_settings s, gp_distributed_xacts x\n        |WHERE s.setting::int = x.gp_session_id\n        |AND s.name = 'gp_session_id'\n        |AND x.state IN ('Active Distributed', 'None');");
        ManagedResource result = resource.package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> conn.createStatement(), Resource$.MODULE$.statementResource(), ClassManifestFactory$.MODULE$.classType(Statement.class)).flatMap((Function1 & Serializable & scala.Serializable)statement -> resource.package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> statement.executeQuery(sqlQuery), Resource$.MODULE$.resultSetResource(), ClassManifestFactory$.MODULE$.classType(ResultSet.class)).map((Function1 & Serializable & scala.Serializable)rs -> {
            Vector transactionIdVector = MODULE$.collectFrom((ResultSet)rs, (Function1 & Serializable & scala.Serializable)x$5 -> x$5.getString("distributed_id"), (ClassTag)ClassTag$.MODULE$.apply(String.class));
            if (transactionIdVector.size() != 1) {
                throw new RuntimeException("Unable to retrieve current distributed transaction id");
            }
            return (String)transactionIdVector.apply(0);
        }));
        return (String)result.map((Function1 & Serializable & scala.Serializable)x -> x).opt().get();
    }

    public String formatSqlQuery(String s2) {
        return new StringOps(Predef$.MODULE$.augmentString(new StringOps(Predef$.MODULE$.augmentString(s2)).stripMargin())).replaceAllLiterally("\n", " ").trim();
    }

    public static final /* synthetic */ boolean $anonfun$externalTableExists$5(ResultSet rs) {
        rs.next();
        return rs.getBoolean(1);
    }

    public static final /* synthetic */ void $anonfun$retrieveResults$2(PreparedStatement preparedStatement$1, String sqlQuery$4, Tuple2 x0$1) {
        Tuple2 tuple2 = x0$1;
        if (tuple2 != null) {
            Object arg = tuple2._1();
            int i = tuple2._2$mcI$sp();
            if (arg instanceof Integer) {
                int n = BoxesRunTime.unboxToInt((Object)arg);
                int n2 = i;
                preparedStatement$1.setInt(n2 + 1, n);
                return;
            }
        }
        if (tuple2 != null) {
            Object arg = tuple2._1();
            int i = tuple2._2$mcI$sp();
            if (arg instanceof String) {
                String string = (String)arg;
                int n = i;
                preparedStatement$1.setString(n + 1, string);
                return;
            }
        }
        if (tuple2 != null) {
            Object arg = tuple2._1();
            int i = tuple2._2$mcI$sp();
            if (arg instanceof Array) {
                Array array = (Array)arg;
                int n = i;
                preparedStatement$1.setArray(n + 1, array);
                return;
            }
        }
        if (tuple2 != null) {
            Object arg = tuple2._1();
            throw new IllegalArgumentException(new StringBuilder(61).append("Unsupported type ").append(arg.getClass().getSimpleName()).append(" for prepared ").append("statement argument ").append(arg).append(". SQL was: ").append(sqlQuery$4).toString());
        }
        throw new MatchError((Object)tuple2);
    }

    private final Vector inner$1(Vector acc, ResultSet rs, Function1 getter$2) {
        while (rs.next()) {
            Vector vector = (Vector)acc.$colon$plus(getter$2.apply((Object)rs), Vector$.MODULE$.canBuildFrom());
            acc = vector;
        }
        return acc;
    }

    private Jdbc$() {
        MODULE$ = this;
        LazyLogging.$init$(this);
    }
}

