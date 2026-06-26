package io.pivotal.greenplum.spark.externaltable

import com.typesafe.scalalogging.LazyLogging
import io.pivotal.greenplum.spark.conf.ConnectorOptions
import io.pivotal.greenplum.spark.util.TransactionData
import org.eclipse.jetty.server.{ServerConnector, Connector}
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler

import java.io.{ByteArrayInputStream, IOException, InputStream}
import java.net.BindException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import scala.util.{Failure, Success, Try}

/**
 * Manages a single Jetty HTTP server that implements the gpfdist protocol
 * for a set of candidate ports. Shared across tasks on the same executor.
 */
class GpfdistService(
    serverConnectorHost: String,
    availablePorts     : List[Int],
    bufferMap          : ConcurrentHashMap[String, Try[TransactionData]],
    sendBufferMap      : ConcurrentHashMap[String, PartitionData],
    server             : ServerWrapper,
    val timeoutInMillis: Long)
    extends LazyLogging {

  require(server != null, "server is null")

  private val started = new AtomicBoolean(false)

  def this(
      availablePorts: List[Int],
      bufferMap     : ConcurrentHashMap[String, Try[TransactionData]],
      sendBufferMap : ConcurrentHashMap[String, PartitionData],
      server        : ServerWrapper,
      timeoutInMillis: Long) =
    this(ConnectorOptions.GPDB_DEFAULT_SERVER_HOST, availablePorts, bufferMap, sendBufferMap, server, timeoutInMillis)

  def start(): Unit = synchronized {
    if (!started.get()) {
      val portsIter = availablePorts.iterator
      while (portsIter.hasNext && !started.get()) {
        val currentPort = portsIter.next()
        try {
          val connector = newConnector(server.server)
          connector.setHost(serverConnectorHost)
          connector.setPort(currentPort)
          connector.setIdleTimeout(timeoutInMillis)
          server.setConnectors(Array[Connector](connector))
          server.start()
          started.set(true)
          val actualPort = if (currentPort != 0) currentPort else getPort
          logger.info(s"Successfully started Gpfdist service on $serverConnectorHost:$actualPort")
        } catch {
          case _: BindException => logger.warn(s"Unable to bind port $currentPort")
          case e: IOException   => logger.warn(s"Error when starting GpfdistService: ${e.getMessage}")
        }
      }
      if (!started.get())
        throw new RuntimeException(s"Unable to start GpfdistService on any of ports=${availablePorts.mkString(", ")}")
    }
  }

  def stop(): Unit = synchronized {
    if (started.compareAndSet(true, false)) server.stop()
  }

  def getPort: Int =
    server.getConnectors.head.asInstanceOf[ServerConnector].getLocalPort

  def state: GpfdistServiceState.Value =
    GpfdistServiceState.withName(server.getState.toLowerCase.capitalize)

  def getReceivedDataFor(transactionId: String): Try[InputStream] = {
    val entry = bufferMap.remove(transactionId)
    if (entry == null) Success(new ByteArrayInputStream(Array.emptyByteArray))
    else entry.map(_.getInputStream)
  }

  def setPartitionDataFor(key: String, partitionData: PartitionData): Try[GpfdistService] = {
    if (sendBufferMap.containsKey(key))
      Failure(new IllegalStateException(s"Send buffer already exists for the given path = $key"))
    else {
      sendBufferMap.put(key, partitionData)
      Success(this)
    }
  }

  def removePartitionDataFor(key: String): Option[PartitionData] = {
    val result = Option(sendBufferMap.remove(key))
    result match {
      case Some(p) if !p.handled.get() =>
        logger.warn(s"Data has not been marked as processed for path $key and partition ${p.partitionIndex}")
      case None =>
        logger.warn(s"No partition exists for path $key")
      case _ =>
    }
    result
  }

  private def newConnector(server: org.eclipse.jetty.server.Server): ServerConnector = {
    val scheduler = new ScheduledExecutorScheduler("Gpfdist-JettyScheduler", true)
    new ServerConnector(server, null, scheduler, null, -1, -1, new HttpConnectionFactory())
  }
}
