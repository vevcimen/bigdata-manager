package io.pivotal.greenplum.spark

case class RDDPartitionBounds(lower: BigDecimal, upper: BigDecimal)

object RDDPartitionBounds {
  def apply(lower: String, upper: String): RDDPartitionBounds =
    RDDPartitionBounds(BigDecimal(lower), BigDecimal(upper))

  def apply(lower: Long, upper: Long): RDDPartitionBounds =
    RDDPartitionBounds(BigDecimal(lower), BigDecimal(upper))
}
