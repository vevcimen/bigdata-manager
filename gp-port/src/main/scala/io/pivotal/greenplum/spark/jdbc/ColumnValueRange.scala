package io.pivotal.greenplum.spark.jdbc

/** Min/max range of a partition column used to compute WHERE clauses. */
case class ColumnValueRange(min: BigDecimal, max: BigDecimal)

object ColumnValueRange {
  def apply(min: java.math.BigDecimal, max: java.math.BigDecimal): ColumnValueRange =
    ColumnValueRange(BigDecimal(min), BigDecimal(max))

  def apply(min: String, max: String): ColumnValueRange =
    ColumnValueRange(BigDecimal(min), BigDecimal(max))

  def apply(min: Long, max: Long): ColumnValueRange =
    ColumnValueRange(BigDecimal(min), BigDecimal(max))
}
