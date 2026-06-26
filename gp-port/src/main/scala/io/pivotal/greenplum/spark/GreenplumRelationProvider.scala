package io.pivotal.greenplum.spark

import com.typesafe.scalalogging.LazyLogging
import io.pivotal.greenplum.spark.conf.GreenplumOptions
import io.pivotal.greenplum.spark.externaltable.{GreenplumQualifiedName, GreenplumTableManager, PartitionWriter}
import io.pivotal.greenplum.spark.jdbc.{ColumnValueRange, ConnectionManager, Jdbc}
import org.apache.spark.sql.sources._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset, Row, SQLContext, SaveMode}

import scala.util.Using

/**
 * Spark DataSource V1 entry point for Greenplum.
 * Registered via META-INF/services/org.apache.spark.sql.sources.DataSourceRegister.
 */
class GreenplumRelationProvider
    extends RelationProvider
    with CreatableRelationProvider
    with DataSourceRegister
    with LazyLogging {

  override def shortName(): String = "greenplum"

  // ── READ ──────────────────────────────────────────────────────────────────

  override def createRelation(sqlContext: SQLContext,
                               parameters: Map[String, String]): BaseRelation = {
    val sparkContext     = sqlContext.sparkContext
    val greenplumOptions = new GreenplumOptions(parameters, sparkContext.getConf)
    val conn             = ConnectionManager.getConnection(greenplumOptions)

    try {
      val table            = GreenplumQualifiedName.forTable(greenplumOptions.dbSchema, greenplumOptions.dbTable)
      val schema           = Jdbc.resolveTable(conn, greenplumOptions.url, table)
      val gpSegmentIds     = Jdbc.retrieveSegmentIds(conn)
      val partitions       = computePartitions(conn, table, schema, greenplumOptions, gpSegmentIds)

      logger.debug(s"NumPartitions = ${partitions.length}")
      GreenplumRelation(schema, partitions, greenplumOptions, sqlContext)
    } finally {
      conn.close()
    }
  }

  def computePartitions(conn: java.sql.Connection,
                         table           : GreenplumQualifiedName.Table,
                         schema          : StructType,
                         greenplumOptions: GreenplumOptions,
                         gpSegmentIds    : Array[Int]): Array[GreenplumPartition] = {
    val partitionColumnName = greenplumOptions.partitionColumn
    if (partitionColumnName == GreenplumOptions.DEFAULT_PARTITION_COLUMN_NAME) {
      require(greenplumOptions.partitions.isEmpty,
        s"When using '$partitionColumnName' for '${GreenplumOptions.GPDB_PARTITION_COLUMN}', " +
        s"'${GreenplumOptions.GPDB_PARTITIONS}' is expected to be empty.")
      Partitioner.segmentPartitions(gpSegmentIds)
    } else {
      val partitionColumn = schema.fields
        .find(_.name == partitionColumnName)
        .getOrElse(throw new IllegalArgumentException(
          s"'$partitionColumnName' does not exist in $table table"))

      checkPartitionColumnType(partitionColumn).foreach(e => throw e)

      val numPartitions = greenplumOptions.partitions.getOrElse(gpSegmentIds.length)
      val range         = Jdbc.computeColumnValueRange(conn, table, partitionColumnName)
      Partitioner.columnPartitions(numPartitions, partitionColumn, range)
    }
  }

  private def checkPartitionColumnType(col: org.apache.spark.sql.types.StructField)
      : Either[Unit, IllegalArgumentException] = {
    import org.apache.spark.sql.types._
    col.dataType match {
      case _: NumericType | DateType | TimestampType => Left(())
      case dt => Right(new IllegalArgumentException(
        s"Column '${col.name}' has unsupported type '$dt' for partitioning. " +
        "Use a numeric, date, or timestamp column."))
    }
  }

  // ── WRITE ─────────────────────────────────────────────────────────────────

  override def createRelation(sqlContext: SQLContext, mode: SaveMode,
                               parameters: Map[String, String],
                               data: DataFrame): BaseRelation = {
    val sparkContext     = sqlContext.sparkContext
    val greenplumOptions = new GreenplumOptions(parameters, sparkContext.getConf)
    val destTable        = GreenplumQualifiedName.forTable(greenplumOptions.dbSchema, greenplumOptions.dbTable)

    val conn = ConnectionManager.getConnection(greenplumOptions)
    try {
      GreenplumTableManager.write(conn, destTable, data, greenplumOptions, mode, sparkContext)
    } finally {
      conn.close()
    }

    // Return a thin relation (schema only, no real partitions)
    createRelation(sqlContext, parameters)
  }
}
