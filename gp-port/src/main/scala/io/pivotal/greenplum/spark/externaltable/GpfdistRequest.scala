package io.pivotal.greenplum.spark.externaltable

import com.typesafe.scalalogging.LazyLogging
import jakarta.servlet.http.HttpServletRequest

import java.util.NoSuchElementException
import scala.util.{Failure, Success, Try}

/**
 * Parsed representation of an HTTP request from a Greenplum segment to gpfdist.
 */
case class GpfdistRequest(
    transactionId: String,
    segmentId    : Int,
    path         : String,
    request      : HttpServletRequest) {

  val identifier: String  = s"seg$segmentId,$transactionId,$path"
  def requestType: String = request.getMethod
  def contentLength: Int  = request.getContentLength
  def inputStream         = request.getInputStream
}

object GpfdistRequest extends LazyLogging {

  val DISTRIBUTED_TRANSACTION_ID_HEADER = "X-GP-XID"
  val SEGMENT_ID_HEADER                 = "X-GP-SEGMENT-ID"
  val SEGMENT_COUNT_HEADER              = "X-GP-SEGMENT-COUNT"

  def parse(request: HttpServletRequest, path: String): Try[GpfdistRequest] = Try {
    val txId = Option(request.getHeader(DISTRIBUTED_TRANSACTION_ID_HEADER)).getOrElse {
      val msg = s"$DISTRIBUTED_TRANSACTION_ID_HEADER header is required for a request"
      logger.debug(s"Incomplete headers in request: $msg")
      throw new WebException(400, msg)
    }

    val segId = Option(request.getHeader(SEGMENT_ID_HEADER))
      .map(s => Try(s.toInt))
      .getOrElse(Failure(new NoSuchElementException(s"$SEGMENT_ID_HEADER header is required for a request")))
      .recover { case e =>
        logger.warn(s"Unable to parse $SEGMENT_ID_HEADER request: ${e.getMessage}")
        throw new WebException(400, e.getMessage)
      }.get

    GpfdistRequest(txId, segId, path, request)
  }
}
