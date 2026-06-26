package io.pivotal.greenplum.spark.externaltable

import com.typesafe.scalalogging.LazyLogging
import io.pivotal.greenplum.spark.{ConnectorUtils, ErrorHandling, FilterCompiler}
import io.pivotal.greenplum.spark.GreenplumPartition
import io.pivotal.greenplum.spark.conf.GreenplumOptions
import io.pivotal.greenplum.spark.jdbc.{ConnectionManager, Jdbc}
import org.apache.spark.SparkEnv
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.SpecificInternalRow
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.StructType
import org.postgresql.util.PSQLException

import scala.util.{Failure, Success}

/**
 * Iterator that reads rows from Greenplum for one partition via the gpfdist protocol.
 *
 * Read path:
 *   1. Open a JDBC connection (autoCommit=false).
 *   2. Ensure a writable external TEMP TABLE exists pointing at our gpfdist server.
 *   3. Get the distributed transaction ID.
 *   4. Execute `INSERT INTO extTable SELECT … FROM internalTable WHERE predicate`
 *      — Greenplum pushes CSV rows to our Jetty HTTP server.
 *   5. Commit, close the JDBC connection.
 *   6. Return rows lazily via DataIterator (reads from the accumulated InputStream).
 */
class GreenplumRowIterator(
    applicationId   : String,
    partition       : GreenplumPartition,
    schema          : StructType,
    greenplumOptions: GreenplumOptions,
    allColumns      : Seq[String],
    filters         : Array[Filter],
    connectorUtils  : ConnectorUtils = new ConnectorUtils()
) extends Iterator[InternalRow] with LazyLogging {

  private val filterWhereClause: String = GreenplumRowIterator.filterWherePredicate(filters)

  private val wherePredicate: String =
    if (filterWhereClause.nonEmpty) {
      logger.debug(s"Including '$filterWhereClause' in WHERE clause")
      s"($filterWhereClause) AND (${partition.whereClause})"
    } else {
      partition.whereClause
    }

  private val converters = DataTypeConverterFactory.create(schema)

  private var processedCount = 0

  val dataIterator: DataIterator = {
    val conn = ConnectionManager.getConnection(greenplumOptions, autoCommit = false)
    var service: GpfdistService = null
    try {
      val threadId          = Thread.currentThread().getId
      val executorId        = SparkEnv.get.executorId
      val externalTableName = GreenplumTableManager.generateExternalTableName(
        applicationId, greenplumOptions.dbTable, executorId, threadId, allColumns)
      val internalTable = GreenplumQualifiedName.forTable(
        greenplumOptions.dbSchema, greenplumOptions.dbTable)
      val externalTable = GreenplumQualifiedName.forTempTable(externalTableName)

      service = GpfdistServiceManager.getService(greenplumOptions.connectorOptions)

      val location         = connectorUtils.getLocation(greenplumOptions.connectorOptions, service.getPort)
      val pathPrefix       = connectorUtils.getLocationPathPrefix(applicationId, executorId, threadId)
      val locationWithPath = location.withPath(pathPrefix)
      val pathKey          = locationWithPath.path

      if (!Jdbc.externalTableExists(conn, externalTable)) {
        logger.debug(
          s"Temporary external table $externalTable not found, creating table with " +
          s"location='${locationWithPath.getUrl}' and columns=[${allColumns.mkString(",")}]")
        val distributionPolicy =
          if (greenplumOptions.connectorOptions.matchDistributionPolicy)
            Jdbc.determineDistributionPolicy(conn, internalTable)
          else "DISTRIBUTED RANDOMLY"
        Jdbc.createGpfdistWritableExternalTable(
          conn, internalTable, externalTable, locationWithPath, allColumns, distributionPolicy)
      }

      logger.debug(s"Using path key '$pathKey' for RDD partition with WHERE clause '${partition.whereClause}'")

      try {
        Jdbc.copyTableToExternal(conn, internalTable, externalTable, wherePredicate, allColumns)
        conn.commit()
      } catch {
        case p: PSQLException =>
          logger.error(s"There was a PSQL exception: ${p.getMessage}")
          service.getReceivedDataFor(pathKey) match {
            case Failure(error) =>
              logger.error(s"There was a failure in the HTTP handler: ${error.getMessage}")
              ErrorHandling.appendCauseToErrorChain(p, error)
            case Success(_) =>
          }
          throw p
      }

      service.getReceivedDataFor(pathKey) match {
        case Success(data) =>
          logger.debug(s"Reading table data for table $internalTable with path key $pathKey")
          new DataIterator(data)
        case Failure(e) =>
          logger.error("Table copy succeeded but there was an error in the HTTP handler; this should not happen")
          logger.error(e.getMessage)
          throw e
      }
    } finally {
      if (!conn.isClosed) {
        try { conn.rollback() }
        catch { case e: Throwable => logger.warn(s"Ignoring failure to rollback transaction: ${e.getMessage}") }
        try { conn.close() }
        catch { case e: Throwable => logger.warn(s"Ignoring failure to close transaction: ${e.getMessage}") }
      }
    }
  }

  override def hasNext: Boolean = dataIterator.hasNext

  override def next(): InternalRow = {
    val row    = new SpecificInternalRow(schema.fields.map(_.dataType).toSeq)
    val record = try { dataIterator.next() }
                 catch { case e: Exception => dataIterator.closeIfNeeded(); throw e }

    record.zipWithIndex.foreach { case (value, index) =>
      if (value != null) {
        if (index < converters.length) converters(index)(value, row)
      } else if (!schema.fields(index).nullable) {
        val columnName = schema.fields(index).name
        val tableName  = greenplumOptions.dbTable
        throw new RuntimeException(
          s"The $columnName column in $tableName table should not have null value.")
      }
    }

    processedCount += 1
    if (processedCount % 100000 == 0)
      logger.debug(s"partition.index = ${partition.index} processedCount = $processedCount")
    row
  }
}

object GreenplumRowIterator {
  def filterWherePredicate(filters: Array[Filter]): String =
    filters.flatMap(FilterCompiler.compileFilter)
           .map(p => s"($p)")
           .mkString(" AND ")
}
