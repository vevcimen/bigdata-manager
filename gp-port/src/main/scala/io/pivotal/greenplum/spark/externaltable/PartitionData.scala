package io.pivotal.greenplum.spark.externaltable

import org.apache.spark.sql.Row

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Holds the data for one Spark partition to be served via the gpfdist HTTP server.
 *
 * Either rowIterator (streaming) or rows (materialized list) is populated, not both.
 * rowTransformer reorders columns to match Greenplum's column order.
 */
class PartitionData(
    val partitionIndex: Int,
    val rowIterator   : Iterator[Row],
    var rows          : List[Row],
    val rowTransformer: Row => Row) {

  val handled: AtomicBoolean = new AtomicBoolean(false)
}

object PartitionData {
  def apply(
      partitionIndex: Int,
      rowIterator   : Iterator[Row] = null,
      rows          : List[Row]     = Nil,
      rowTransformer: Row => Row    = identity): PartitionData =
    new PartitionData(partitionIndex, rowIterator, rows, rowTransformer)
}
