package io.pivotal.greenplum.spark

import io.pivotal.greenplum.spark.conf.GreenplumOptions
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.sources.{BaseRelation, Filter, PrunedFilteredScan}
import org.apache.spark.sql.types.{StructField, StructType}

/** A Spark DataSource V1 relation backed by a Greenplum table. */
case class GreenplumRelation(
    override val schema: StructType,
    parts              : Array[GreenplumPartition],
    greenplumOptions   : GreenplumOptions,
    override val sqlContext: SQLContext
) extends BaseRelation with PrunedFilteredScan {

  override def unhandledFilters(filters: Array[Filter]): Array[Filter] =
    filters.filter(f => FilterCompiler.compileFilter(f).isEmpty)

  override def buildScan(requiredColumns: Array[String], filters: Array[Filter]): RDD[Row] = {
    val projectedSchema = StructType(requiredColumns.map(col => schema.find(_.name == col).get))
    new GreenplumRDD(
      sqlContext.sparkContext,
      projectedSchema,
      parts,
      greenplumOptions,
      requiredColumns.toSeq,
      filters
    ).asInstanceOf[RDD[Row]]
  }
}
