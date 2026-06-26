package io.pivotal.greenplum.spark.externaltable

import com.typesafe.scalalogging.LazyLogging
import io.pivotal.greenplum.spark.SqlExecutor
import io.pivotal.greenplum.spark.conf.GreenplumOptions
import io.pivotal.greenplum.spark.jdbc.ConnectionManager
import org.apache.spark.sql.Row

import java.sql.{Connection, DriverManager}
import scala.util.Using

/**
 * Serializable Spark closure for writing Spark partitions to Greenplum via gpfdist.
 *
 * `gpfdistService` is `@transient lazy` so it is initialised on each executor the first
 * time `getClosure` is invoked, not during driver serialization.
 */
class PartitionWriter(
    applicationId   : String,
    greenplumOptions: GreenplumOptions,
    rowTransformer  : Row => Row
) extends Serializable with LazyLogging {

  @transient lazy val gpfdistService: GpfdistService =
    GpfdistServiceManager.getService(greenplumOptions.connectorOptions)

  /** Returns the per-partition closure suitable for RDD.mapPartitionsWithIndex. */
  def getClosure(): (Int, Iterator[Row]) => Iterator[Int] = {
    (idx: Int, it: Iterator[Row]) => {
      if (it.isEmpty) {
        logger.debug(s"Datamover $idx skipped work as partition iterator is empty")
        Iterator(0)
      } else {
        gpfdistService.start()

        val partitionData =
          if (greenplumOptions.iteratorOptimization)
            new PartitionData(idx, it, Nil, rowTransformer)
          else
            new PartitionData(idx, null, it.toList, rowTransformer)

        Using.resource(getDirectConnection()) { conn =>
          val dataMover = getDataMover(conn)
          val count     = dataMover.moveData(partitionData).get
          conn.commit()
          logger.debug(s"Datamover $idx copied $count rows")
          Iterator(count)
        }
      }
    }
  }

  def getDataMover(conn: Connection): GreenplumDataMover = {
    val sqlExecutor  = new SqlExecutor(conn)
    val tableManager = new GreenplumTableManager(sqlExecutor)
    new GreenplumDataMover(applicationId, greenplumOptions, tableManager, gpfdistService)
  }

  /**
   * Creates a direct JDBC connection (bypassing HikariCP pool).
   * Each partition gets its own dedicated connection so that:
   *  - TEMP TABLEs remain visible within the same session
   *  - Connection count is bounded by the number of active partitions
   *  - Connections are closed immediately when the partition finishes
   */
  def getDirectConnection(): Connection = {
    Class.forName(greenplumOptions.driver)
    val conn = greenplumOptions.password match {
      case Some(pw) =>
        DriverManager.getConnection(greenplumOptions.url, greenplumOptions.user, pw)
      case None =>
        val props = new java.util.Properties()
        props.setProperty("user", greenplumOptions.user)
        DriverManager.getConnection(greenplumOptions.url, props)
    }
    conn.setAutoCommit(false)
    conn
  }
}
