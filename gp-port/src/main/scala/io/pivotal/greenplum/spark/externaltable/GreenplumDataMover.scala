package io.pivotal.greenplum.spark.externaltable

import com.typesafe.scalalogging.LazyLogging
import io.pivotal.greenplum.spark.ConnectorUtils
import io.pivotal.greenplum.spark.conf.GreenplumOptions
import org.apache.spark.SparkEnv

import scala.util.Try

/**
 * Moves a single Spark partition to Greenplum via a readable external gpfdist table.
 *
 * Write path:
 *   1. Create (or reuse) a readable external TEMP TABLE pointing at our gpfdist server.
 *   2. Fetch the distributed transaction ID from Greenplum.
 *   3. Register the partition data with the gpfdist service so GP can GET it.
 *   4. Execute `INSERT INTO target SELECT * FROM extTable` — GP pulls rows from gpfdist.
 *   5. Remove the partition data entry from the service.
 */
class GreenplumDataMover(
    applicationId   : String,
    greenplumOptions: GreenplumOptions,
    tableManager    : GreenplumTableManager,
    service         : GpfdistService,
    connectorUtils  : ConnectorUtils = new ConnectorUtils()
) extends LazyLogging {

  def moveData(partitionData: PartitionData): Try[Int] = {
    val executorId       = SparkEnv.get.executorId
    val threadId         = Thread.currentThread().getId
    val targetTable      = GreenplumQualifiedName.forTable(
      greenplumOptions.dbSchema, greenplumOptions.dbTable)
    val externalTableName = GreenplumTableManager.generateExternalTableName(
      applicationId, targetTable.name, executorId, threadId)
    val extTable         = GreenplumQualifiedName.forTempTable(externalTableName)
    val location         = connectorUtils.getLocation(
      greenplumOptions.connectorOptions, service.getPort)
    val pathPrefix       = connectorUtils.getLocationPathPrefix(applicationId, executorId, threadId)
    val locationWithPath = location.withPath(pathPrefix)
    val pathKey          = locationWithPath.path

    tableManager.createReadableExternalTableIfNotExists(targetTable, extTable, locationWithPath)
      .flatMap { _ =>
        logger.debug(
          s"Setting RDD(index=${partitionData.partitionIndex}) data for path $pathKey")
        val rowsCopied =
          service.setPartitionDataFor(pathKey, partitionData)
            .flatMap(_ => tableManager.copyTableFromExternal(extTable, targetTable))
        service.removePartitionDataFor(pathKey)
        rowsCopied
      }
  }
}

object GreenplumDataMover {
  def apply(applicationId: String, greenplumOptions: GreenplumOptions,
            tableManager: GreenplumTableManager, service: GpfdistService): GreenplumDataMover =
    new GreenplumDataMover(applicationId, greenplumOptions, tableManager, service)
}
