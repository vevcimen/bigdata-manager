package io.pivotal.greenplum.spark.externaltable

import org.apache.spark.sql.catalyst.expressions.SpecificInternalRow
import org.apache.spark.sql.types._
import org.apache.spark.unsafe.types.UTF8String

import java.time._
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, DateTimeParseException}
import java.time.temporal.{ChronoField, Temporal}

/**
 * Builds a per-column converter array that populates a SpecificInternalRow from CSV string values.
 */
object DataTypeConverterFactory {

  private val epochDate             = LocalDate.parse("1970-01-01")
  private val localTimeZone         = ZoneId.systemDefault()
  private val dateFormatter         = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  private val timeFormatter         = DateTimeFormatter.ofPattern("HH:mm:ss")
  private val optionalTzPattern     = "[xxx][x]"
  private val timeTzFormatter       = new DateTimeFormatterBuilder()
    .append(timeFormatter).appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true)
    .appendPattern(optionalTzPattern).toFormatter()
  private val timestampFmtBuilder   = new DateTimeFormatterBuilder()
    .append(dateFormatter).appendLiteral(" ").append(timeFormatter)
    .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true)
  private val timestampFormatter    = timestampFmtBuilder.toFormatter()
  private val timestampTzFormatter  = timestampFmtBuilder.appendPattern(optionalTzPattern).toFormatter()

  type Converter = (String, SpecificInternalRow) => Unit

  def create(schema: StructType): Array[Converter] =
    schema.fields.zipWithIndex.map { case (field, index) =>
      makeConverter(field, index)
    }

  private def makeConverter(field: StructField, index: Int): Converter =
    field.dataType match {
      case BooleanType   => (v, row) => row.setBoolean(index, parseBoolean(v))
      case _: DecimalType=> (v, row) => row.update(index, org.apache.spark.sql.types.Decimal(v))
      case DoubleType    => (v, row) => row.setDouble(index, v.toDouble)
      case FloatType     => (v, row) => row.setFloat(index, v.toFloat)
      case IntegerType   => (v, row) => row.setInt(index, v.toInt)
      case LongType      => (v, row) => row.setLong(index, v.toLong)
      case ShortType     => (v, row) => row.setShort(index, v.toShort)
      case StringType    => (v, row) => row.update(index, UTF8String.fromString(v))
      case DateType      => (v, row) => row.update(index, parseDate(v))
      case TimestampType => (v, row) => row.update(index, convertToSqlTimestamp(v))
      case other         => throw new RuntimeException(s"Unsupported data type: ${other.typeName}")
    }

  private def parseBoolean(s: String): Boolean =
    if (s == null) throw new IllegalArgumentException("For input string: \"null\"")
    else s.toLowerCase match {
      case "t" | "true"  => true
      case "f" | "false" => false
      case _             => throw new IllegalArgumentException(s"""For input string: "$s"""")
    }

  private def parseDate(value: String): Int =
    LocalDate.parse(value, dateFormatter).toEpochDay.toInt

  private def convertToSqlTimestamp(value: String): Long =
    parseTimestamp(value)
      .orElse(parseTimestampTz(value))
      .orElse(parseTime(value))
      .orElse(parseTz(value))
      .getOrElse(throw new DateTimeParseException(s"Text '$value' could not be parsed", value, -1))

  private def parseTimestamp(v: String): Option[Long] =
    scala.util.Try {
      microsFor(LocalDateTime.parse(v, timestampFormatter).atZone(localTimeZone))
    }.toOption

  private def parseTimestampTz(v: String): Option[Long] =
    scala.util.Try { microsFor(OffsetDateTime.parse(v, timestampTzFormatter)) }.toOption

  private def parseTime(v: String): Option[Long] =
    scala.util.Try {
      microsFor(LocalTime.parse(v).atDate(epochDate).atZone(localTimeZone))
    }.toOption

  private def parseTz(v: String): Option[Long] =
    scala.util.Try {
      val t = OffsetTime.parse(v, timeTzFormatter)
      val secondOfDay    = t.getLong(ChronoField.SECOND_OF_DAY)
      val offsetSeconds  = t.getLong(ChronoField.OFFSET_SECONDS)
      (secondOfDay - offsetSeconds) * 1_000_000L + t.getLong(ChronoField.MICRO_OF_SECOND)
    }.toOption

  private def microsFor(t: Temporal): Long =
    t.getLong(ChronoField.INSTANT_SECONDS) * 1_000_000L + t.getLong(ChronoField.MICRO_OF_SECOND)
}
