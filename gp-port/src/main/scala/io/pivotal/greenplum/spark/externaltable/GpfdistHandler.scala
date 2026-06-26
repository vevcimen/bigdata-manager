package io.pivotal.greenplum.spark.externaltable

import com.typesafe.scalalogging.LazyLogging
import io.pivotal.greenplum.spark.GreenplumCSVFormat
import io.pivotal.greenplum.spark.util.TransactionData
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.apache.spark.sql.Row
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler

import java.util.concurrent.ConcurrentHashMap
import scala.util.{Failure, Success, Try}

/**
 * Jetty AbstractHandler that implements the gpfdist HTTP protocol.
 *
 *  GET  → serves CSV rows from the send buffer (Spark→GP direction)
 *  POST → accumulates raw CSV bytes into the receive buffer (GP→Spark direction)
 */
class GpfdistHandler(
    bufferMap    : ConcurrentHashMap[String, Try[TransactionData]],
    sendBufferMap: ConcurrentHashMap[String, PartitionData])
    extends AbstractHandler with LazyLogging {

  def this() = this(new ConcurrentHashMap, new ConcurrentHashMap)

  override def handle(
      target     : String,
      baseRequest: Request,
      request    : HttpServletRequest,
      response   : HttpServletResponse): Unit = {

    val result = GpfdistRequest.parse(request, target).flatMap { gpReq =>
      handleValidGpfdistRequest(gpReq, response)
    }

    result match {
      case Success(gpReq) =>
        response.setStatus(200)
        logger.debug(s"[${gpReq.identifier}] Successfully handled ${gpReq.requestType} request for $target")

      case Failure(e) =>
        val code = e match {
          case we: WebException => we.code
          case _                => 500
        }
        if (logger.underlying.isDebugEnabled) e.printStackTrace()
        processError(target, request, response, code, e)
    }

    baseRequest.setHandled(true)
  }

  def handleValidGpfdistRequest(gpReq: GpfdistRequest, response: HttpServletResponse): Try[GpfdistRequest] = {
    logger.debug(s"[${gpReq.identifier}] Received ${gpReq.requestType} request")
    gpReq.requestType match {
      case "GET"  => handleGET(gpReq, response).map(_ => gpReq)
      case "POST" => Try(handlePOST(gpReq)).map(_ => gpReq)
      case other  => Failure(new WebException(405, s"Method $other is not supported"))
    }
  }

  private def handleGET(gpReq: GpfdistRequest, response: HttpServletResponse): Try[Unit] = {
    logger.debug(s"[${gpReq.identifier}] Processing GET request for ${gpReq.path}")
    processPartitionData(response, gpReq.path)
  }

  private def processPartitionData(response: HttpServletResponse, transactionId: String): Try[Unit] =
    Option(sendBufferMap.get(transactionId)) match {
      case None =>
        Failure(new WebException(400, s"no data available for $transactionId"))
      case Some(partitionData) =>
        if (partitionData.handled.compareAndSet(false, true)) serveData(response, partitionData)
        else Success(())
    }

  private def serveData(response: HttpServletResponse, partitionData: PartitionData): Try[Unit] = Try {
    response.setContentType("text/plain")
    response.setCharacterEncoding(GreenplumCSVFormat.DEFAULT_ENCODING)
    val writer  = response.getWriter
    val reorder = partitionData.rowTransformer

    if (partitionData.rowIterator != null) {
      partitionData.rowIterator.foreach(row => writer.println(rowToCSVString(reorder(row))))
    } else {
      partitionData.rows.map(reorder).foreach(row => writer.println(rowToCSVString(row)))
      partitionData.rows = Nil
    }
    writer.flush()
  }

  private def handlePOST(gpReq: GpfdistRequest): Unit = {
    logger.debug(s"[${gpReq.identifier}] Processing POST request for ${gpReq.path}")
    val contentLen = gpReq.contentLength
    if (contentLen < 0) throw new WebException(413, "Size of request is too large")
    if (contentLen == 0) return

    val requestInputStream = Option(gpReq.inputStream).getOrElse {
      logger.warn(s"[${gpReq.identifier}] Received POST request with non-zero content length ($contentLen) but unable to retrieve the body of the request")
      throw new WebException(400)
    }

    bufferMap.putIfAbsent(gpReq.path, Success(new TransactionData()))
    bufferMap.get(gpReq.path) match {
      case Failure(e) =>
        logger.warn(s"[${gpReq.identifier}] Received additional request data for a transaction that previously errored: ${e.getMessage}")
        throw new WebException(500)
      case Success(txData) =>
        txData.write(gpReq.segmentId, requestInputStream, contentLen) match {
          case Failure(e) =>
            bufferMap.put(gpReq.path, Failure(e))
            logger.warn(s"[${gpReq.identifier}] Attempted to copy data from the request but failed: ${e.getMessage}")
            throw new WebException(400, e.getMessage)
          case Success(_) =>
            logger.debug(s"[${gpReq.identifier}] Copied $contentLen additional bytes of data from the request")
        }
    }
  }

  private def rowToCSVString(row: Row): String =
    row.toSeq.map {
      case null      => GreenplumCSVFormat.VALUE_OF_NULL
      case s: String =>
        s"${GreenplumCSVFormat.QUOTE}${s.replace(GreenplumCSVFormat.QUOTE_STRING, GreenplumCSVFormat.ESCAPED_QUOTE)}${GreenplumCSVFormat.QUOTE}"
      case other => other
    }.mkString(GreenplumCSVFormat.CHAR_DELIMITER.toString)

  private def processError(
      path    : String,
      request : HttpServletRequest,
      response: HttpServletResponse,
      code    : Int,
      e       : Throwable): Unit = {
    logger.error(s"Failed to handle ${request.getMethod} request for $path : ${e.toString}")
    response.setStatus(code)
    if (e != null && e.getMessage != null) response.getWriter.write(e.getMessage)
  }
}
