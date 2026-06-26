package io.pivotal.greenplum.spark

import io.pivotal.greenplum.spark.externaltable.GreenplumRowIterator
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.StructType
import org.apache.spark.{Partition, SparkContext, TaskContext}

import scala.reflect.ClassTag

/** RDD that reads rows from Greenplum via the gpfdist protocol. */
class GreenplumRDD(
    sc             : SparkContext,
    schema         : StructType,
    partitions     : Array[GreenplumPartition],
    greenplumOptions: io.pivotal.greenplum.spark.conf.GreenplumOptions,
    requiredColumns: Seq[String],
    filters        : Array[Filter]
) extends RDD[InternalRow](sc, Nil)(ClassTag(classOf[InternalRow])) {

  private val applicationId = sc.applicationId

  override def getPartitions: Array[Partition] = partitions.asInstanceOf[Array[Partition]]

  override def compute(thePart: Partition, context: TaskContext): Iterator[InternalRow] = {
    val partition = thePart.asInstanceOf[GreenplumPartition]
    new GreenplumRowIterator(applicationId, partition, schema, greenplumOptions,
                              requiredColumns, filters)
  }
}
