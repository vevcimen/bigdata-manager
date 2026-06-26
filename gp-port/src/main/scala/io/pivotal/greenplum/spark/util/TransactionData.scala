package io.pivotal.greenplum.spark.util

import java.io.InputStream
import scala.util.Try

/**
 * Mutable buffer that accumulates bytes received from Greenplum
 * via the gpfdist protocol across one or more segment POST requests.
 */
class TransactionData {
  private val buffer = new LargeByteBuffer

  /** Reads `contentLen` bytes from `inputStream` and appends them to the buffer. */
  def write(segmentId: Int, inputStream: InputStream, contentLen: Int): Try[Unit] = Try {
    val chunk     = new Array[Byte](8192)
    var remaining = contentLen
    while (remaining > 0) {
      val n = inputStream.read(chunk, 0, math.min(chunk.length, remaining))
      if (n == -1) throw new java.io.IOException(
        s"Unexpected end of stream: expected $contentLen bytes but stream ended early")
      buffer.write(chunk, 0, n)
      remaining -= n
    }
  }

  /** Returns the accumulated data as a fresh InputStream. */
  def getInputStream: InputStream = buffer.toInputStream
}
