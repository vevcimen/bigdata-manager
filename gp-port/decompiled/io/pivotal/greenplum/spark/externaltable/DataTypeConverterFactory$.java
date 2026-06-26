/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.catalyst.expressions.SpecificInternalRow
 *  org.apache.spark.sql.types.BooleanType$
 *  org.apache.spark.sql.types.DataType
 *  org.apache.spark.sql.types.DateType$
 *  org.apache.spark.sql.types.Decimal$
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
 *  org.apache.spark.unsafe.types.UTF8String
 *  scala.Array$
 *  scala.Function0
 *  scala.Function1
 *  scala.Function2
 *  scala.MatchError
 *  scala.PartialFunction
 *  scala.Predef$
 *  scala.Serializable
 *  scala.Tuple2
 *  scala.collection.immutable.StringOps
 *  scala.collection.mutable.ArrayOps$ofRef
 *  scala.reflect.ClassTag$
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.java8.JFunction0$mcJ$sp
 *  scala.util.Failure
 *  scala.util.Try
 *  scala.util.Try$
 */
package io.pivotal.greenplum.spark.externaltable;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import org.apache.spark.sql.catalyst.expressions.SpecificInternalRow;
import org.apache.spark.sql.types.BooleanType$;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DateType$;
import org.apache.spark.sql.types.Decimal$;
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
import org.apache.spark.unsafe.types.UTF8String;
import scala.Array$;
import scala.Function0;
import scala.Function1;
import scala.Function2;
import scala.MatchError;
import scala.PartialFunction;
import scala.Predef$;
import scala.Tuple2;
import scala.collection.immutable.StringOps;
import scala.collection.mutable.ArrayOps;
import scala.reflect.ClassTag$;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.runtime.java8.JFunction0;
import scala.util.Failure;
import scala.util.Try;
import scala.util.Try$;

public final class DataTypeConverterFactory$ {
    public static DataTypeConverterFactory$ MODULE$;
    private final LocalDate epochDate;
    private final ZoneId localTimeZone;
    private final DateTimeFormatter dateFormatter;
    private final DateTimeFormatter timeFormatter;
    private final String optionalTzPattern;
    private final DateTimeFormatter timeTzFormatter;
    private final DateTimeFormatterBuilder timestampFormatterBuilder;
    private final DateTimeFormatter timestampFormatter;
    private final DateTimeFormatter timestampTzFormatter;

    static {
        new DataTypeConverterFactory$();
    }

    private boolean parseBoolean(String s2) {
        if (s2 != null) {
            String string = s2.toLowerCase();
            if ("t".equals(string)) {
                return true;
            }
            if ("f".equals(string)) {
                return false;
            }
            throw new IllegalArgumentException(new StringBuilder(20).append("For input string: \"").append(s2).append("\"").toString());
        }
        throw new IllegalArgumentException("For input string: \"null\"");
    }

    private LocalDate epochDate() {
        return this.epochDate;
    }

    private ZoneId localTimeZone() {
        return this.localTimeZone;
    }

    private DateTimeFormatter dateFormatter() {
        return this.dateFormatter;
    }

    private DateTimeFormatter timeFormatter() {
        return this.timeFormatter;
    }

    private String optionalTzPattern() {
        return this.optionalTzPattern;
    }

    private DateTimeFormatter timeTzFormatter() {
        return this.timeTzFormatter;
    }

    private DateTimeFormatterBuilder timestampFormatterBuilder() {
        return this.timestampFormatterBuilder;
    }

    private DateTimeFormatter timestampFormatter() {
        return this.timestampFormatter;
    }

    private DateTimeFormatter timestampTzFormatter() {
        return this.timestampTzFormatter;
    }

    public Function2<String, SpecificInternalRow, BoxedUnit>[] create(StructType schema) {
        Function2[] conversionFunctions = (Function2[])new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])schema.fields())).zipWithIndex(Array$.MODULE$.canBuildFrom(ClassTag$.MODULE$.apply(Tuple2.class))))).map((Function1 & Serializable & scala.Serializable)x0$1 -> {
            Tuple2 tuple2 = x0$1;
            if (tuple2 != null) {
                StructField field = (StructField)tuple2._1();
                int index = tuple2._2$mcI$sp();
                DataType dataType = field.dataType();
                if (BooleanType$.MODULE$.equals(dataType)) {
                    return (Function2 & Serializable & scala.Serializable)(value, row) -> {
                        row.setBoolean(index, DataTypeConverterFactory$.MODULE$.parseBoolean(value));
                        return BoxedUnit.UNIT;
                    };
                }
                if (dataType instanceof DecimalType) {
                    return (Function2 & Serializable & scala.Serializable)(value, row) -> {
                        row.update(index, (Object)Decimal$.MODULE$.apply(value));
                        return BoxedUnit.UNIT;
                    };
                }
                if (DoubleType$.MODULE$.equals(dataType)) {
                    return (Function2 & Serializable & scala.Serializable)(value, row) -> {
                        row.setDouble(index, new StringOps(Predef$.MODULE$.augmentString(value)).toDouble());
                        return BoxedUnit.UNIT;
                    };
                }
                if (FloatType$.MODULE$.equals(dataType)) {
                    return (Function2 & Serializable & scala.Serializable)(value, row) -> {
                        row.setFloat(index, new StringOps(Predef$.MODULE$.augmentString(value)).toFloat());
                        return BoxedUnit.UNIT;
                    };
                }
                if (IntegerType$.MODULE$.equals(dataType)) {
                    return (Function2 & Serializable & scala.Serializable)(value, row) -> {
                        row.setInt(index, new StringOps(Predef$.MODULE$.augmentString(value)).toInt());
                        return BoxedUnit.UNIT;
                    };
                }
                if (LongType$.MODULE$.equals(dataType)) {
                    return (Function2 & Serializable & scala.Serializable)(value, row) -> {
                        row.setLong(index, new StringOps(Predef$.MODULE$.augmentString(value)).toLong());
                        return BoxedUnit.UNIT;
                    };
                }
                if (ShortType$.MODULE$.equals(dataType)) {
                    return (Function2 & Serializable & scala.Serializable)(value, row) -> {
                        row.setShort(index, new StringOps(Predef$.MODULE$.augmentString(value)).toShort());
                        return BoxedUnit.UNIT;
                    };
                }
                if (StringType$.MODULE$.equals(dataType)) {
                    return (Function2 & Serializable & scala.Serializable)(value, row) -> {
                        row.update(index, (Object)UTF8String.fromString((String)value));
                        return BoxedUnit.UNIT;
                    };
                }
                if (DateType$.MODULE$.equals(dataType)) {
                    return (Function2 & Serializable & scala.Serializable)(value, row) -> {
                        row.update(index, (Object)BoxesRunTime.boxToInteger((int)DataTypeConverterFactory$.MODULE$.parseDate(value)));
                        return BoxedUnit.UNIT;
                    };
                }
                if (TimestampType$.MODULE$.equals(dataType)) {
                    return (Function2 & Serializable & scala.Serializable)(value, row) -> {
                        row.update(index, (Object)BoxesRunTime.boxToLong((long)DataTypeConverterFactory$.MODULE$.convertToSqlTimestamp(value)));
                        return BoxedUnit.UNIT;
                    };
                }
                String columnTypeName = field.dataType().typeName();
                throw new RuntimeException(new StringBuilder(23).append("Unsupported data type: ").append(columnTypeName).toString());
            }
            throw new MatchError((Object)tuple2);
        }, Array$.MODULE$.canBuildFrom(ClassTag$.MODULE$.apply(Function2.class)));
        return conversionFunctions;
    }

    private long convertToSqlTimestamp(String value) {
        return BoxesRunTime.unboxToLong((Object)this.parseTimestamp(value).orElse((Function0 & Serializable & scala.Serializable)() -> MODULE$.parseTimestampTz(value)).orElse((Function0 & Serializable & scala.Serializable)() -> MODULE$.parseTime(value)).orElse((Function0 & Serializable & scala.Serializable)() -> MODULE$.parseTimeTz(value)).recoverWith((PartialFunction)new scala.Serializable(value){
            public static final long serialVersionUID = 0L;
            private final String value$1;

            public final <A1 extends Throwable, B1> B1 applyOrElse(A1 x1, Function1<A1, B1> function1) {
                A1 A1 = x1;
                return (B1)new Failure((Throwable)new DateTimeParseException(new StringBuilder(27).append("Text '").append(this.value$1).append("' could not be parsed").toString(), this.value$1, -1));
            }

            public final boolean isDefinedAt(Throwable x1) {
                Throwable throwable = x1;
                return true;
            }
            {
                this.value$1 = value$1;
            }
        }).get());
    }

    private int parseDate(String value) {
        return (int)LocalDate.parse(value, this.dateFormatter()).getLong(ChronoField.EPOCH_DAY);
    }

    private Try<Object> parseTimestamp(String value) {
        return Try$.MODULE$.apply((Function0)(JFunction0.mcJ.sp & Serializable & scala.Serializable)() -> {
            ZonedDateTime dateTime = LocalDateTime.parse(value, MODULE$.timestampFormatter()).atZone(MODULE$.localTimeZone());
            return MODULE$.microsFor(dateTime);
        });
    }

    private Try<Object> parseTimestampTz(String value) {
        return Try$.MODULE$.apply((Function0)(JFunction0.mcJ.sp & Serializable & scala.Serializable)() -> {
            OffsetDateTime dateTime = OffsetDateTime.parse(value, MODULE$.timestampTzFormatter());
            return MODULE$.microsFor(dateTime);
        });
    }

    private Try<Object> parseTimeTz(String value) {
        return Try$.MODULE$.apply((Function0)(JFunction0.mcJ.sp & Serializable & scala.Serializable)() -> {
            OffsetTime dateTime = OffsetTime.parse(value, MODULE$.timeTzFormatter());
            return MODULE$.microsFor(dateTime);
        });
    }

    private Try<Object> parseTime(String value) {
        return Try$.MODULE$.apply((Function0)(JFunction0.mcJ.sp & Serializable & scala.Serializable)() -> {
            ZonedDateTime dateTime = LocalTime.parse(value).atDate(MODULE$.epochDate()).atZone(MODULE$.localTimeZone());
            return MODULE$.microsFor(dateTime);
        });
    }

    private long microsFor(Temporal temporal) {
        return temporal.getLong(ChronoField.INSTANT_SECONDS) * 1000L * 1000L + temporal.getLong(ChronoField.MICRO_OF_SECOND);
    }

    private long microsFor(OffsetTime dateTime) {
        long secondOfDay = dateTime.getLong(ChronoField.SECOND_OF_DAY);
        long offsetSeconds = dateTime.getLong(ChronoField.OFFSET_SECONDS);
        long offsetMicrosOfDay = (secondOfDay - offsetSeconds) * 1000L * 1000L;
        return offsetMicrosOfDay + dateTime.getLong(ChronoField.MICRO_OF_SECOND);
    }

    private DataTypeConverterFactory$() {
        MODULE$ = this;
        this.epochDate = LocalDate.parse("1970-01-01");
        this.localTimeZone = ZoneId.systemDefault();
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        this.optionalTzPattern = "[xxx][x]";
        this.timeTzFormatter = new DateTimeFormatterBuilder().append(this.timeFormatter()).appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true).appendPattern(this.optionalTzPattern()).toFormatter();
        this.timestampFormatterBuilder = new DateTimeFormatterBuilder().append(this.dateFormatter()).appendLiteral(" ").append(this.timeFormatter()).appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true);
        this.timestampFormatter = this.timestampFormatterBuilder().toFormatter();
        this.timestampTzFormatter = this.timestampFormatterBuilder().appendPattern(this.optionalTzPattern()).toFormatter();
    }
}

