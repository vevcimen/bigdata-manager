/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.types.AtomicType
 *  org.apache.spark.sql.types.BinaryType$
 *  org.apache.spark.sql.types.BooleanType$
 *  org.apache.spark.sql.types.DataType
 *  org.apache.spark.sql.types.DateType$
 *  org.apache.spark.sql.types.DecimalType
 *  org.apache.spark.sql.types.DoubleType$
 *  org.apache.spark.sql.types.FloatType$
 *  org.apache.spark.sql.types.IntegerType$
 *  org.apache.spark.sql.types.LongType$
 *  org.apache.spark.sql.types.ShortType$
 *  org.apache.spark.sql.types.StringType$
 *  org.apache.spark.sql.types.StructField
 *  org.apache.spark.sql.types.StructType
 *  org.apache.spark.sql.types.TimestampType$
 *  scala.Array$
 *  scala.Function0
 *  scala.Function1
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Predef$
 *  scala.Serializable
 *  scala.Some
 *  scala.collection.Seq
 *  scala.collection.Seq$
 *  scala.collection.TraversableOnce
 *  scala.collection.immutable.IndexedSeq$
 *  scala.collection.immutable.Nil$
 *  scala.collection.mutable.ArrayOps$ofRef
 *  scala.reflect.ClassTag$
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.RichInt$
 *  scala.util.Try
 *  scala.util.Try$
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.conf.GreenplumOptions;
import io.pivotal.greenplum.spark.externaltable.GreenplumQualifiedName;
import io.pivotal.greenplum.spark.externaltable.SqlObjectNameUtils$;
import java.io.Serializable;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.spark.sql.types.AtomicType;
import org.apache.spark.sql.types.BinaryType$;
import org.apache.spark.sql.types.BooleanType$;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DateType$;
import org.apache.spark.sql.types.DecimalType;
import org.apache.spark.sql.types.DoubleType$;
import org.apache.spark.sql.types.FloatType$;
import org.apache.spark.sql.types.IntegerType$;
import org.apache.spark.sql.types.LongType$;
import org.apache.spark.sql.types.ShortType$;
import org.apache.spark.sql.types.StringType$;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.types.TimestampType$;
import scala.Array$;
import scala.Function0;
import scala.Function1;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Predef$;
import scala.Some;
import scala.collection.Seq;
import scala.collection.Seq$;
import scala.collection.TraversableOnce;
import scala.collection.immutable.IndexedSeq$;
import scala.collection.immutable.Nil$;
import scala.collection.mutable.ArrayOps;
import scala.reflect.ClassTag$;
import scala.runtime.BoxesRunTime;
import scala.runtime.RichInt$;
import scala.util.Try;
import scala.util.Try$;

public final class GreenplumTableManager$ {
    public static GreenplumTableManager$ MODULE$;

    static {
        new GreenplumTableManager$();
    }

    public Option<String> gpdbColumnType(DataType dt) {
        DataType dataType = dt;
        if (StringType$.MODULE$.equals(dataType)) {
            return new Some((Object)"TEXT");
        }
        if (BinaryType$.MODULE$.equals(dataType)) {
            return new Some((Object)"BYTEA");
        }
        if (BooleanType$.MODULE$.equals(dataType)) {
            return new Some((Object)"BOOLEAN");
        }
        if (FloatType$.MODULE$.equals(dataType)) {
            return new Some((Object)"FLOAT4");
        }
        if (DoubleType$.MODULE$.equals(dataType)) {
            return new Some((Object)"FLOAT8");
        }
        if (ShortType$.MODULE$.equals(dataType)) {
            return new Some((Object)"SMALLINT");
        }
        if (dataType instanceof DecimalType) {
            DecimalType decimalType = (DecimalType)dataType;
            return new Some((Object)new StringBuilder(10).append("NUMERIC(").append(decimalType.precision()).append(",").append(decimalType.scale()).append(")").toString());
        }
        if (IntegerType$.MODULE$.equals(dataType)) {
            return new Some((Object)"INTEGER");
        }
        if (LongType$.MODULE$.equals(dataType)) {
            return new Some((Object)"BIGINT");
        }
        if (TimestampType$.MODULE$.equals(dataType)) {
            return new Some((Object)"TIMESTAMP");
        }
        if (DateType$.MODULE$.equals(dataType)) {
            return new Some((Object)"DATE");
        }
        return None$.MODULE$;
    }

    public Try<String> createTableColumnList(StructType sparkSchema) {
        return Try$.MODULE$.apply((Function0 & Serializable & scala.Serializable)() -> new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])sparkSchema.fields())).map((Function1 & Serializable & scala.Serializable)x0$1 -> {
            StructField structField = x0$1;
            if (structField != null) {
                String name = structField.name();
                DataType dataType = structField.dataType();
                boolean nullable = structField.nullable();
                String nullableStr = nullable ? "" : " NOT NULL";
                String typ = (String)MODULE$.gpdbColumnType(dataType).getOrElse((Function0 & Serializable & scala.Serializable)() -> {
                    throw new IllegalArgumentException(new StringBuilder(81).append("Column ").append(name).append(" has unsupported type ").append(dataType).append(". ").append("Please use one of the supported Spark data types: ").append(((TraversableOnce)MODULE$.getSupportedSparkDataTypes().map((Function1 & Serializable & scala.Serializable)x$3 -> x$3.toString(), Seq$.MODULE$.canBuildFrom())).mkString(", ")).toString());
                });
                return new StringBuilder(1).append(SqlObjectNameUtils$.MODULE$.escape(name)).append(" ").append(typ).append(nullableStr).toString();
            }
            throw new MatchError((Object)structField);
        }, Array$.MODULE$.canBuildFrom(ClassTag$.MODULE$.apply(String.class))))).mkString(", "));
    }

    public Seq<DataType> getSupportedSparkDataTypes() {
        return (Seq)Seq$.MODULE$.apply((Seq)Predef$.MODULE$.wrapRefArray((Object[])new AtomicType[]{StringType$.MODULE$, BinaryType$.MODULE$, BooleanType$.MODULE$, FloatType$.MODULE$, DoubleType$.MODULE$, ShortType$.MODULE$, new DecimalType(){

            public String toString() {
                return "DecimalType";
            }
        }, IntegerType$.MODULE$, LongType$.MODULE$, TimestampType$.MODULE$, DateType$.MODULE$}));
    }

    public Try<String> createTableStatement(GreenplumQualifiedName tableName, StructType schema, GreenplumOptions options) {
        String string;
        Option<String> option = options.distributedBy();
        if (option instanceof Some) {
            Some some = (Some)option;
            String columns2 = (String)some.value();
            string = new StringBuilder(18).append(" DISTRIBUTED BY (").append(columns2).append(")").toString();
        } else if (None$.MODULE$.equals(option)) {
            string = " DISTRIBUTED RANDOMLY";
        } else {
            throw new MatchError(option);
        }
        String distribution = string;
        return this.createTableColumnList(schema).map((Function1 & Serializable & scala.Serializable)columns -> new StringBuilder(17).append("CREATE TABLE ").append(tableName).append(" (").append((String)columns).append(")").append(distribution).append(";").toString());
    }

    public String generateExternalTableNamePrefix(String applicationId, String tableName) {
        String applicationTableHash = this.foldedMd5Hex(new StringBuilder(0).append(applicationId).append(tableName).toString());
        return new StringBuilder(6).append("spark_").append(applicationTableHash).toString();
    }

    public String generateExternalTableNameColumnPrefix(String applicationId, String table, Seq<String> columns) {
        String applicationTableHash = this.generateExternalTableNamePrefix(applicationId, table);
        String columnsHash = this.foldedMd5Hex(columns.mkString(","));
        return new StringBuilder(1).append(applicationTableHash).append("_").append(columnsHash).toString();
    }

    public String generateExternalTableName(String applicationId, String table, String executorId, long threadId, Seq<String> columns) {
        String prefix = this.generateExternalTableNameColumnPrefix(applicationId, table, columns);
        return new StringBuilder(2).append(prefix).append("_").append(executorId).append("_").append(threadId).toString();
    }

    public Seq<String> generateExternalTableName$default$5() {
        return (Seq)Nil$.MODULE$;
    }

    private String foldedMd5Hex(String s2) {
        byte[] md5In16 = DigestUtils.md5(s2);
        byte[] md5In8 = (byte[])((TraversableOnce)RichInt$.MODULE$.to$extension0(Predef$.MODULE$.intWrapper(0), 7).map((Function1 & Serializable & scala.Serializable)i -> BoxesRunTime.boxToByte((byte)GreenplumTableManager$.$anonfun$foldedMd5Hex$1(md5In16, BoxesRunTime.unboxToInt((Object)i))), IndexedSeq$.MODULE$.canBuildFrom())).toArray(ClassTag$.MODULE$.Byte());
        String md5In16Hex = Hex.encodeHexString(md5In8);
        return md5In16Hex;
    }

    public static final /* synthetic */ byte $anonfun$foldedMd5Hex$1(byte[] md5In16$1, int i) {
        return (byte)(md5In16$1[i] ^ md5In16$1[i + 8]);
    }

    private GreenplumTableManager$() {
        MODULE$ = this;
    }
}

