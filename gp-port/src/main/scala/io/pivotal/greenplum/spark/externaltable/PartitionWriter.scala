package io.pivotal.greenplum.spark.externaltable

import com.typesafe.scalalogging.LazyLogging
import io.pivotal.greenplum.spark.SqlExecutor
import io.pivotal.greenplum.spark.conf.GreenplumOptions
import io.pivotal.greenplum.spark.jdbc.ConnectionManager
import org.apache.spark.sql.Row

import java.sql.Connection
import scala.util.Using

/**
 * Serializable Spark closure for writing Spark partitions to Greenplum via gpfdist.
 *
 * `gpfdistService` is `@transient lazy` so it is initialised on each executor the first
 * time `getClosure` is invoked, not during driver serialization.
 */
@SerialVersionUID(-7341052219816373304L)
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

        Using.resource(getConnection()) { conn =>
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

  def getConnection(): Connection =
    ConnectionManager.getConnection(greenplumOptions, autoCommit = false)
}
