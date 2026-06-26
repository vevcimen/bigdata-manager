package io.pivotal.greenplum.spark.util

import java.io.{ByteArrayInputStream, InputStream}

/**
 * Accumulates bytes from an HTTP request body into an in-memory buffer.
 * Used to buffer data arriving from Greenplum via the gpfdist protocol.
 */
class LargeByteBuffer {
  private val buf = new java.io.ByteArrayOutputStream(64 * 1024)

  def write(bytes: Array[Byte], offset: Int, length: Int): Unit =
    buf.write(bytes, offset, length)

  def write(bytes: Array[Byte]): Unit = buf.write(bytes)

  def toInputStream: InputStream = new ByteArrayInputStream(buf.toByteArray)

  def size: Int = buf.size()
}
