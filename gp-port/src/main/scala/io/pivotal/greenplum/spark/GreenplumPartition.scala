package io.pivotal.greenplum.spark

import org.apache.spark.Partition

/** A single Greenplum RDD partition represented by a SQL WHERE clause. */
case class GreenplumPartition(whereClause: String, idx: Int) extends Partition {
  override def index: Int = idx
}
