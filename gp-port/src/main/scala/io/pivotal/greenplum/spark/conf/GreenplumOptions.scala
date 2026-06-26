package io.pivotal.greenplum.spark.conf

import com.typesafe.scalalogging.LazyLogging
import io.pivotal.greenplum.spark.ConnectorUtils
import org.apache.spark.SparkConf
import org.greenplum.spark.GpfdistConf

/**
 * All Greenplum connector options parsed from the Spark DataSource parameters map.
 */
class GreenplumOptions(val parameters: Map[String, String], sparkConf: SparkConf)
    extends Serializable with Options with LazyLogging {

  import GreenplumOptions._

  val originalParameters: Map[String, String] = parameters

  val url            : String         = option(GPDB_URL,            ErrorIfMissing)
  val user           : String         = option(GPDB_USER,           ErrorIfMissing)
  val password       : Option[String] = option(GPDB_PASSWORD)
  val dbSchema       : String         = option(GPDB_SCHEMA_NAME,    Default("public"))
  val dbTable        : String         = option(GPDB_TABLE_NAME,     ErrorIfMissing)
  val partitionColumn: String         = option(GPDB_PARTITION_COLUMN, Default(DEFAULT_PARTITION_COLUMN_NAME))
  val partitions     : Option[Int]    = option(GPDB_PARTITIONS,     positiveInt)
  val driver         : String         = option(GPDB_DRIVER,         Default("org.postgresql.Driver"))
  val truncateTable  : Boolean        = option(GPDB_TRUNCATE_TABLE, Default("false"), bool)
  val distributedBy  : Option[String] = option(GPDB_DISTRIBUTED_BY)
  val iteratorOptimization: Boolean   = option(GPDB_ITERATOR_OPTIMIZATION, Default("true"), bool)

  val connectorOptions      : ConnectorOptions       =
    new ConnectorOptions(parameters, new GpfdistConf(sparkConf), Map.empty, new ConnectorUtils())
  val connectionPoolOptions : ConnectionPoolOptions  =
    ConnectionPoolOptions(originalParameters)
}

object GreenplumOptions {
  val DEFAULT_PARTITION_COLUMN_NAME = "gp_segment_id"

  val GPDB_URL                   = "url"
  val GPDB_USER                  = "user"
  val GPDB_PASSWORD              = "password"
  val GPDB_SCHEMA_NAME           = "dbSchema"
  val GPDB_TABLE_NAME            = "dbtable"
  val GPDB_PARTITION_COLUMN      = "partitionColumn"
  val GPDB_PARTITIONS            = "partitions"
  val GPDB_DRIVER                = "driver"
  val GPDB_TRUNCATE_TABLE        = "truncate"
  val GPDB_DISTRIBUTED_BY        = "distributedBy"
  val GPDB_ITERATOR_OPTIMIZATION = "iteratorOptimization"
  val GPDB_ENV_PREFIX            = "spark.greenplum."
}
