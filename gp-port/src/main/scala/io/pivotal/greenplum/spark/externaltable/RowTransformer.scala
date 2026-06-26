package io.pivotal.greenplum.spark.externaltable

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.Row

import scala.util.{Failure, Success, Try}

/** Builds a Row→Row function that reorders Spark columns to match Greenplum column order. */
object RowTransformer extends LazyLogging {

  val identityFunction: Row => Row = x => x

  def getFunction(sparkCols: Seq[String], gpdbCols: Seq[String]): Try[Row => Row] = {
    val lowercaseSparkCols = sparkCols.map(_.toLowerCase)
    val lowercaseGPDBCols  = gpdbCols.map(_.toLowerCase)

    if (lowercaseSparkCols == lowercaseGPDBCols) {
      logger.debug("RowTransformer.getfunction returning identity function...")
      Success(identityFunction)
    } else {
      val extraColumns   = formatColumnList(lowercaseSparkCols.diff(lowercaseGPDBCols))
      val missingColumns = formatColumnList(lowercaseGPDBCols.diff(lowercaseSparkCols))

      if (extraColumns.nonEmpty)
        logger.warn(
          s"Spark dataframe contains extra column[s] $extraColumns " +
          s"that will be ignored when writing to Greenplum Database table.")

      if (missingColumns.nonEmpty)
        return Failure(new RuntimeException(
          s"Spark DataFrame must include column[s] $missingColumns " +
          s"when writing to Greenplum Database table."))

      val sparkColIndexFromGpdbColIndex = lowercaseGPDBCols.map(lowercaseSparkCols.indexOf)

      val reorderColumns: Row => Row = sparkRow => {
        val reorderedValues = lowercaseGPDBCols.indices.map(i =>
          sparkRow.get(sparkColIndexFromGpdbColIndex(i)))
        Row.fromSeq(reorderedValues)
      }
      Success(reorderColumns)
    }
  }

  private def formatColumnList(columns: Seq[String]): String =
    columns.map(s => s""""$s"""").mkString(", ")
}
