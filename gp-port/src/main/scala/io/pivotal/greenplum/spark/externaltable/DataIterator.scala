package io.pivotal.greenplum.spark.externaltable

import com.typesafe.scalalogging.LazyLogging
import com.univocity.parsers.csv.CsvParser
import io.pivotal.greenplum.spark.GreenplumCSVFormat

import java.io.InputStream

/**
 * Reads CSV rows from an InputStream using the univocity parser.
 * Extends NextIterator so that the stream is closed when iteration finishes.
 */
class DataIterator(val inputStream: InputStream) extends NextIterator[Array[String]] with LazyLogging {

  private lazy val csvIterator =
    new CsvParser(GreenplumCSVFormat.DEFAULT)
      .iterate(inputStream, GreenplumCSVFormat.DEFAULT_ENCODING)
      .iterator()

  override def getNext(): Array[String] =
    if (csvIterator.hasNext) csvIterator.next()
    else { finished = true; null }

  override def close(): Unit = inputStream.close()
}
