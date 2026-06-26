package io.pivotal.greenplum.spark

import io.pivotal.greenplum.spark.jdbc.ColumnValueRange
import org.apache.spark.sql.types.StructField

/** Computes GreenplumPartition arrays from segment IDs or column ranges. */
object Partitioner {

  /** One partition per GP primary segment, filtering by gp_segment_id. */
  def segmentPartitions(gpSegmentIds: Array[Int]): Array[GreenplumPartition] =
    gpSegmentIds.map(id => GreenplumPartition(s"gp_segment_id = $id", id))

  /** Numeric range partitions over a user-specified partition column. */
  def columnPartitions(numPartitions: Int, partitionColumn: StructField,
                        range: ColumnValueRange): Array[GreenplumPartition] = {
    if (numPartitions <= 0) return Array.empty
    val col    = partitionColumn.name
    val stride = (range.max - range.min) / numPartitions
    if (stride == 0) return Array(GreenplumPartition("1=1", 0))
    (0 until numPartitions).map { i =>
      val lower = range.min + stride * i
      val upper = if (i == numPartitions - 1) range.max else lower + stride
      val clause =
        if (i == numPartitions - 1) s""""$col" >= $lower AND "$col" <= $upper"""
        else                         s""""$col" >= $lower AND "$col" < $upper"""
      GreenplumPartition(clause, i)
    }.toArray
  }
}
