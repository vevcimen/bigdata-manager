package io.pivotal.greenplum.spark.jdbc

import com.typesafe.scalalogging.LazyLogging
import io.pivotal.greenplum.spark.GreenplumCSVFormat
import io.pivotal.greenplum.spark.externaltable.{GpfdistLocation, GreenplumQualifiedName, SqlObjectNameUtils}

import java.sql.{Array => SqlArray, _}
import scala.util.{Try, Using}
import org.apache.spark.sql.types._

/** SQL helpers for interacting with Greenplum. */
object Jdbc extends LazyLogging {

  // ── Schema resolution ────────────────────────────────────────────────────

  def resolveTable(conn: Connection, url: String, table: GreenplumQualifiedName.Table): StructType = {
    Using.resource(conn.prepareStatement(s"SELECT * FROM $table WHERE 1=0")) { ps =>
      val meta = ps.executeQuery().getMetaData
      val fields = (1 to meta.getColumnCount).map { i =>
        val name    = meta.getColumnName(i)
        val sqlType = meta.getColumnType(i)
        val prec    = meta.getPrecision(i)
        val scale   = meta.getScale(i)
        val nullable= meta.isNullable(i) != ResultSetMetaData.columnNoNulls
        StructField(name, jdbcTypeToSparkType(sqlType, prec, scale), nullable)
      }
      StructType(fields.toArray)
    }
  }

  private def jdbcTypeToSparkType(sqlType: Int, precision: Int, scale: Int): DataType =
    sqlType match {
      case Types.BIT | Types.BOOLEAN              => BooleanType
      case Types.TINYINT | Types.SMALLINT          => ShortType
      case Types.INTEGER                           => IntegerType
      case Types.BIGINT                            => LongType
      case Types.FLOAT | Types.REAL                => FloatType
      case Types.DOUBLE                            => DoubleType
      case Types.NUMERIC | Types.DECIMAL           =>
        if (precision > 0) DecimalType(precision, scale) else DecimalType(38, 18)
      case Types.CHAR | Types.VARCHAR | Types.LONGVARCHAR |
           Types.NCHAR | Types.NVARCHAR | Types.LONGNVARCHAR => StringType
      case Types.BINARY | Types.VARBINARY | Types.LONGVARBINARY => BinaryType
      case Types.DATE                              => DateType
      case Types.TIMESTAMP | Types.TIMESTAMP_WITH_TIMEZONE => TimestampType
      case _                                       => StringType
    }

  // ── Segment IDs ──────────────────────────────────────────────────────────

  def retrieveSegmentIds(conn: Connection): Array[Int] = {
    Using.resource(conn.createStatement()) { stmt =>
      val rs = stmt.executeQuery(
        "SELECT content FROM gp_segment_configuration WHERE role = 'p' AND content >= 0 ORDER BY content")
      val buf = scala.collection.mutable.ArrayBuffer.empty[Int]
      while (rs.next()) buf += rs.getInt(1)
      rs.close()
      buf.toArray
    }
  }

  // ── Partition column range ────────────────────────────────────────────────

  def computeColumnValueRange(conn: Connection, table: GreenplumQualifiedName.Table,
                               partitionColumn: String): ColumnValueRange = {
    val col = SqlObjectNameUtils.escape(partitionColumn)
    Using.resource(conn.createStatement()) { stmt =>
      val rs = stmt.executeQuery(s"SELECT MIN($col), MAX($col) FROM $table")
      if (rs.next()) ColumnValueRange(rs.getString(1), rs.getString(2))
      else throw new RuntimeException(s"Cannot compute range for column $partitionColumn in $table")
    }
  }

  // ── External table management ─────────────────────────────────────────────

  def externalTableExists(conn: Connection, table: GreenplumQualifiedName.TempTable): Boolean = {
    Using.resource(conn.prepareStatement(
      "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = ?)")) { ps =>
      ps.setString(1, table.name)
      val rs = ps.executeQuery()
      rs.next() && rs.getBoolean(1)
    }
  }

  def createGpfdistWritableExternalTable(conn: Connection,
                                          srcTable : GreenplumQualifiedName.Table,
                                          extTable : GreenplumQualifiedName.TempTable,
                                          location : GpfdistLocation,
                                          columns  : Seq[String],
                                          distributionPolicy: String): Unit = {
    val colDefs = getColumnsMetadata(conn, srcTable, columns).mkString(", ")
    val sql =
      s"""CREATE WRITABLE EXTERNAL TABLE $extTable ($colDefs)
         |LOCATION ('${location.getUrl}')
         |FORMAT 'CSV' (DELIMITER '${GreenplumCSVFormat.CHAR_DELIMITER}'
         |              NULL '${GreenplumCSVFormat.VALUE_OF_NULL}'
         |              QUOTE '${GreenplumCSVFormat.QUOTE_STRING}')
         |$distributionPolicy""".stripMargin
    logger.debug(s"Creating external table: $sql")
    Using.resource(conn.createStatement())(_.executeUpdate(sql))
  }

  def createGpfdistReadableExternalTable(conn: Connection,
                                          srcTable : GreenplumQualifiedName.Table,
                                          extTable : GreenplumQualifiedName.TempTable,
                                          location : GpfdistLocation,
                                          columns  : Seq[String]): Unit = {
    val colDefs = getColumnsMetadata(conn, srcTable, columns).mkString(", ")
    val sql =
      s"""CREATE READABLE EXTERNAL TABLE $extTable ($colDefs)
         |LOCATION ('${location.getUrl}')
         |FORMAT 'CSV' (DELIMITER '${GreenplumCSVFormat.CHAR_DELIMITER}'
         |              NULL '${GreenplumCSVFormat.VALUE_OF_NULL}'
         |              QUOTE '${GreenplumCSVFormat.QUOTE_STRING}')""".stripMargin
    Using.resource(conn.createStatement())(_.executeUpdate(sql))
  }

  def dropExternalTable(conn: Connection, table: GreenplumQualifiedName.TempTable): Unit = {
    Using.resource(conn.createStatement()) { stmt =>
      stmt.executeUpdate(s"DROP EXTERNAL TABLE IF EXISTS $table")
    }
  }

  def copyTableToExternal(conn: Connection,
                           srcTable   : GreenplumQualifiedName.Table,
                           extTable   : GreenplumQualifiedName.TempTable,
                           predicate  : String,
                           columns    : Seq[String]): Unit = {
    val colNames = if (columns.isEmpty) "1"
                   else columns.map(SqlObjectNameUtils.escape).mkString(",")
    val sql = s"INSERT INTO $extTable SELECT $colNames FROM $srcTable WHERE $predicate"
    Using.resource(conn.createStatement())(_.executeUpdate(sql))
  }

  // ── Column metadata ───────────────────────────────────────────────────────

  def getColumnsMetadata(conn: Connection, table: GreenplumQualifiedName.Table,
                          columnNames: Seq[String]): Seq[String] = {
    val sql = formatSqlQuery(
      s"""SELECT '"' || a.attname || '" ' ||
         |pg_catalog.format_type(a.atttypid, a.atttypmod) as column_metadata
         |FROM pg_catalog.pg_attribute a, pg_class b, pg_namespace n
         |WHERE n.nspname = ?
         |AND n.oid = b.relnamespace
         |AND a.attrelid = b.oid
         |AND b.relname = ?
         |AND a.attnum > 0 AND NOT a.attisdropped
         |AND a.attname = ANY (?)
         |ORDER BY strpos(E'\\'${columnNames.mkString("\\',\\'")}\\'' , E'\\'' || a.attname || E'\\'')""".stripMargin)
    retrieveResults(conn, sql,
      Array(table.schema, table.name, conn.createArrayOf("text", columnNames.toArray)),
      rs => rs.getString("column_metadata"))
  }

  // ── Transaction ID ────────────────────────────────────────────────────────

  def getDistributedTransactionId(conn: Connection): String = {
    Using.resource(conn.createStatement()) { stmt =>
      val rs = stmt.executeQuery("SELECT txid_current()::text")
      if (rs.next()) rs.getString(1) else ""
    }
  }

  // ── Distribution policy ───────────────────────────────────────────────────

  def determineDistributionPolicy(conn: Connection, table: GreenplumQualifiedName.Table): String =
    Try(getDistributionPolicy(conn, table))
      .recover { case _ => getDistributionPolicyGp5(conn, table) }
      .getOrElse("DISTRIBUTED RANDOMLY")

  def getDistributionPolicy(conn: Connection, table: GreenplumQualifiedName.Table): String = {
    val sql = formatSqlQuery(
      s"""SELECT pg_catalog.pg_get_table_distributedBy(b.oid) as distribution_policy
         |FROM pg_class b, pg_namespace n
         |WHERE n.nspname = ?
         |AND n.oid = b.relnamespace
         |AND b.relname = ?""".stripMargin)
    val results = retrieveResults(conn, sql, Array(table.schema, table.name),
                                  rs => rs.getString("distribution_policy"))
    if (results.isEmpty) "DISTRIBUTED RANDOMLY" else s"${results.head}"
  }

  def getDistributionPolicyGp5(conn: Connection, table: GreenplumQualifiedName.Table): String = {
    val sql = formatSqlQuery(
      s"""SELECT a.attname as distribution_cols
         |FROM (
         |    SELECT *, row_number() over () as order FROM (
         |        SELECT b.oid, unnest(d.attrnums) as key
         |        FROM pg_class b, pg_namespace n, gp_distribution_policy d
         |        WHERE n.nspname = ? AND n.oid = b.relnamespace AND b.relname = ? AND b.oid = d.localoid
         |    ) as info
         |) as dist_keys, pg_catalog.pg_attribute a
         |WHERE dist_keys.oid = a.attrelid AND a.attnum > 0 AND NOT a.attisdropped
         |AND a.attnum = dist_keys.key ORDER BY dist_keys.order""".stripMargin)
    val results = retrieveResults(conn, sql, Array(table.schema, table.name),
                                  rs => rs.getString("distribution_cols"))
    if (results.isEmpty) {
      logger.debug(s"No distribution columns for $table; assuming random distribution")
      "DISTRIBUTED RANDOMLY"
    } else s"DISTRIBUTED BY (${results.mkString(", ")})"
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private def formatSqlQuery(sql: String): String =
    sql.stripMargin.replaceAll("\\s+", " ").trim

  private def retrieveResults[T](conn: Connection, sql: String, args: Array[Any],
                                  extractor: ResultSet => T): Seq[T] = {
    Using.resource(conn.prepareStatement(sql)) { ps =>
      args.zipWithIndex.foreach { case (arg, i) =>
        arg match {
          case s: String             => ps.setString(i + 1, s)
          case a: SqlArray           => ps.setArray(i + 1, a)
          case o: AnyRef             => ps.setObject(i + 1, o)
        }
      }
      val rs  = ps.executeQuery()
      val buf = scala.collection.mutable.ArrayBuffer.empty[T]
      while (rs.next()) buf += extractor(rs)
      rs.close()
      buf.toSeq
    }
  }
}
