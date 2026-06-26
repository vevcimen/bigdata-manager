package io.pivotal.greenplum.spark.externaltable

import com.typesafe.scalalogging.LazyLogging
import io.pivotal.greenplum.spark.ErrorHandling
import io.pivotal.greenplum.spark.GreenplumCSVFormat
import io.pivotal.greenplum.spark.SqlExecutor
import io.pivotal.greenplum.spark.conf.GreenplumOptions
import io.pivotal.greenplum.spark.jdbc.ConnectionManager
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SaveMode}
import org.apache.spark.sql.types._
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils

import java.sql.{Connection, ResultSet}
import scala.util.{Failure, Success, Try, Using}

/**
 * DDL operations on Greenplum tables.  One instance per connection.
 *
 * The companion object holds purely functional helpers (type mapping, name
 * generation) and the top-level write() entry point.
 */
class GreenplumTableManager(sqlExecutor: SqlExecutor) extends LazyLogging {

  // ── Table existence ──────────────────────────────────────────────────────

  def tableExists(table: GreenplumQualifiedName): Try[Boolean] = {
    val (sql, args) = table match {
      case GreenplumQualifiedName.Table(schema, name) =>
        ("SELECT EXISTS (SELECT 1 FROM information_schema.tables " +
          "WHERE table_schema = ? AND table_name = ?);",
          Array[Any](schema, name))
      case GreenplumQualifiedName.TempTable(name) =>
        ("SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = ?);",
          Array[Any](name))
    }
    sqlExecutor.executeQuery(sql, args, (rs: ResultSet) => { rs.next(); rs.getBoolean(1) })
      .recoverWith(ErrorHandling.wrapErrorMessage(s"Cannot determine if table $table exists"))
  }

  // ── Write preparation ─────────────────────────────────────────────────────

  def prepareTableForWrite(table: GreenplumQualifiedName, sparkSchema: StructType,
                            options: GreenplumOptions,
                            mode: SaveMode = SaveMode.ErrorIfExists): Try[Boolean] =
    tableExists(table).flatMap { exists =>
      if (exists) {
        mode match {
          case SaveMode.ErrorIfExists =>
            Failure(new RuntimeException(s"Table $table exists, and SaveMode.ErrorIfExists was specified"))
          case SaveMode.Overwrite =>
            overwriteTable(table, sparkSchema, options).map(_ => true)
          case _ =>
            Success(false)
        }
      } else {
        createTable(table, sparkSchema, options).map(_ => true)
      }
    }

  def overwriteTable(table: GreenplumQualifiedName, sparkSchema: StructType,
                      options: GreenplumOptions): Try[Unit] =
    if (options.truncateTable) truncateTable(table)
    else dropTable(table).flatMap(_ => createTable(table, sparkSchema, options))

  private def createTable(table: GreenplumQualifiedName, sparkSchema: StructType,
                           options: GreenplumOptions): Try[Unit] =
    GreenplumTableManager.createTableStatement(table, sparkSchema, options)
      .flatMap { sql => sqlExecutor.execute(sql).map { _ =>
        logger.debug(s"Table $table was successfully created")
      }}
      .recoverWith(ErrorHandling.wrapErrorMessage(s"Exception while creating table $table"))

  // ── Column info ───────────────────────────────────────────────────────────

  def getColumnNames(table: GreenplumQualifiedName): Try[Seq[String]] =
    sqlExecutor.executeQuery(s"SELECT * FROM $table LIMIT 0", (rs: ResultSet) => {
      val meta = rs.getMetaData
      (1 to meta.getColumnCount).map(meta.getColumnName)
    })

  // ── External table helpers ────────────────────────────────────────────────

  def createReadableExternalTableIfNotExists(internalTable: GreenplumQualifiedName,
                                              externalTable: GreenplumQualifiedName.TempTable,
                                              gpfdistLocation: GpfdistLocation): Try[Unit] =
    tableExists(externalTable).flatMap {
      case true  => Success(())
      case false => createReadableExternalTable(internalTable, externalTable, gpfdistLocation)
    }

  private def createReadableExternalTable(internalTable: GreenplumQualifiedName,
                                           externalTable: GreenplumQualifiedName.TempTable,
                                           gpfdistLocation: GpfdistLocation): Try[Unit] = {
    val sql = createReadableExternalTableSql(internalTable, externalTable, gpfdistLocation)
    sqlExecutor.execute(sql) match {
      case s @ Success(_) =>
        logger.debug(s"External table $externalTable not found, created table with location='${gpfdistLocation.getUrl}'")
        s
      case Failure(e) =>
        logger.error(s"Exception while creating external table $externalTable with location='${gpfdistLocation.getUrl}'", e)
        Failure(e)
    }
  }

  private def createReadableExternalTableSql(internalTable: GreenplumQualifiedName,
                                              externalTable: GreenplumQualifiedName.TempTable,
                                              gpfdistLocation: GpfdistLocation): String = {
    val url = gpfdistLocation.getUrl
    val sql =
      s"""CREATE READABLE EXTERNAL TEMP TABLE
         |$externalTable (LIKE $internalTable)
         |LOCATION ('$url')
         |FORMAT 'CSV'
         |(DELIMITER AS '${GreenplumCSVFormat.CHAR_DELIMITER}'
         | NULL AS '${GreenplumCSVFormat.VALUE_OF_NULL}')
         |ENCODING '${GreenplumCSVFormat.DEFAULT_ENCODING}'""".stripMargin
        .replaceAll("\\s+", " ").trim
    logger.debug(s"Create readable external table query: $sql")
    sql
  }

  def copyTableFromExternal(extTable: GreenplumQualifiedName,
                             target: GreenplumQualifiedName): Try[Int] = {
    val sql = s"INSERT INTO $target SELECT * FROM $extTable"
    sqlExecutor.executeUpdate(sql)
  }

  // ── Transaction ID ────────────────────────────────────────────────────────

  def getDistributedTransactionId(): Try[String] =
    sqlExecutor.executeQuery("SELECT txid_current()::text", (rs: ResultSet) => {
      if (rs.next()) rs.getString(1) else ""
    }).recoverWith(ErrorHandling.wrapErrorMessage("cannot get distributed id"))

  // ── DDL helpers ───────────────────────────────────────────────────────────

  def truncateTable(table: GreenplumQualifiedName): Try[Unit] =
    sqlExecutor.execute(s"TRUNCATE TABLE $table")
      .recoverWith(ErrorHandling.wrapErrorMessage(s"Exception while truncating table $table"))

  def dropTable(table: GreenplumQualifiedName): Try[Unit] =
    sqlExecutor.execute(s"DROP TABLE $table")
      .recoverWith(ErrorHandling.wrapErrorMessage(s"Exception while dropping table $table"))
}

// ── Companion ─────────────────────────────────────────────────────────────────

object GreenplumTableManager extends LazyLogging {

  // ── Type mapping ─────────────────────────────────────────────────────────

  def gpdbColumnType(dt: DataType): Option[String] = dt match {
    case StringType                  => Some("TEXT")
    case BinaryType                  => Some("BYTEA")
    case BooleanType                 => Some("BOOLEAN")
    case FloatType                   => Some("FLOAT4")
    case DoubleType                  => Some("FLOAT8")
    case ShortType                   => Some("SMALLINT")
    case d: DecimalType              => Some(s"NUMERIC(${d.precision},${d.scale})")
    case IntegerType                 => Some("INTEGER")
    case LongType                    => Some("BIGINT")
    case TimestampType               => Some("TIMESTAMP")
    case DateType                    => Some("DATE")
    case _                           => None
  }

  def getSupportedSparkDataTypes: Seq[DataType] = Seq(
    StringType, BinaryType, BooleanType, FloatType, DoubleType,
    ShortType, DecimalType(38, 18), IntegerType, LongType, TimestampType, DateType)

  def createTableColumnList(sparkSchema: StructType): Try[String] = Try {
    sparkSchema.fields.map { field =>
      val nullableStr = if (field.nullable) "" else " NOT NULL"
      val typ = gpdbColumnType(field.dataType).getOrElse {
        throw new IllegalArgumentException(
          s"Column ${field.name} has unsupported type ${field.dataType}. " +
          s"Please use one of the supported Spark data types: " +
          s"${getSupportedSparkDataTypes.map(_.toString).mkString(", ")}")
      }
      s"""${SqlObjectNameUtils.escape(field.name)} $typ$nullableStr"""
    }.mkString(", ")
  }

  def createTableStatement(tableName: GreenplumQualifiedName, schema: StructType,
                            options: GreenplumOptions): Try[String] = {
    val distribution = options.distributedBy match {
      case Some(cols) => s" DISTRIBUTED BY ($cols)"
      case None       => " DISTRIBUTED RANDOMLY"
    }
    createTableColumnList(schema).map(cols =>
      s"CREATE TABLE $tableName ($cols)$distribution;")
  }

  // ── External table name generation ───────────────────────────────────────

  def generateExternalTableNamePrefix(applicationId: String, tableName: String): String = {
    val hash = foldedMd5Hex(applicationId + tableName)
    s"spark_$hash"
  }

  def generateExternalTableNameColumnPrefix(applicationId: String, table: String,
                                             columns: Seq[String]): String = {
    val prefix      = generateExternalTableNamePrefix(applicationId, table)
    val columnsHash = foldedMd5Hex(columns.mkString(","))
    s"${prefix}_$columnsHash"
  }

  def generateExternalTableName(applicationId: String, table: String, executorId: String,
                                 threadId: Long,
                                 columns: Seq[String] = Nil): String = {
    val prefix = generateExternalTableNameColumnPrefix(applicationId, table, columns)
    s"${prefix}_${executorId}_$threadId"
  }

  private def foldedMd5Hex(s: String): String = {
    val md5In16 = DigestUtils.md5(s)
    val md5In8  = (0 to 7).map(i => (md5In16(i) ^ md5In16(i + 8)).toByte).toArray
    Hex.encodeHexString(md5In8)
  }

  // ── Write entry point ─────────────────────────────────────────────────────

  def write(conn: Connection, table: GreenplumQualifiedName.Table, data: DataFrame,
             options: GreenplumOptions, mode: SaveMode, sparkContext: SparkContext): Unit = {
    conn.setAutoCommit(false)
    val sqlExecutor  = new SqlExecutor(conn)
    val tableManager = new GreenplumTableManager(sqlExecutor)

    tableManager.prepareTableForWrite(table, data.schema, options, mode).get
    conn.commit()

    val gpdbColNames  = tableManager.getColumnNames(table).get
    val sparkColNames = data.schema.fieldNames.toSeq
    val rowTransformer = RowTransformer.getFunction(sparkColNames, gpdbColNames).get

    val writer = new PartitionWriter(sparkContext.applicationId, options, rowTransformer)
    data.rdd.mapPartitionsWithIndex(writer.getClosure()).count()
  }
}
