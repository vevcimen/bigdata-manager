package io.pivotal.greenplum.spark.externaltable

import com.typesafe.scalalogging.LazyLogging
import io.pivotal.greenplum.spark.conf.ConnectorOptions
import io.pivotal.greenplum.spark.util.TransactionData
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.thread.{QueuedThreadPool, ScheduledExecutorScheduler}

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._
import scala.util.Try

/**
 * Singleton that manages per-executor GpfdistService instances,
 * keyed by the configured port list (ServiceKey).
 */
object GpfdistServiceManager extends LazyLogging {

  private val bufferMap   : ConcurrentHashMap[String, Try[TransactionData]] = new ConcurrentHashMap()
  private val sendBufferMap: ConcurrentHashMap[String, PartitionData]        = new ConcurrentHashMap()
  private val servicesMap  : ConcurrentHashMap[ServiceKey, GpfdistService]   = new ConcurrentHashMap()

  def getService(connectorOptions: ConnectorOptions): GpfdistService = {
    val ports = connectorOptions.port
    val key   = ServiceKey(ports)

    if (logger.underlying.isTraceEnabled) {
      servicesMap.entrySet().asScala.foreach { e =>
        logger.trace("{}: [key: {}; value: {}]", hashCode(), e.getKey, e.getValue)
      }
    }

    if (!servicesMap.containsKey(key)) {
      servicesMap.synchronized {
        if (!servicesMap.containsKey(key)) {
          val handler   = new GpfdistHandler(bufferMap, sendBufferMap)
          val pool      = new QueuedThreadPool()
          pool.setName(s"Gpfdist${handler.hashCode()}")
          pool.setDaemon(true)
          val server    = new Server(pool)
          val scheduler = new ScheduledExecutorScheduler("Gpfdist-JettyScheduler", true)
          server.addBean(scheduler)
          server.setHandler(handler)
          val serverHost = connectorOptions.getServerHost
          val service    = new GpfdistService(
            serverHost, ports, bufferMap, sendBufferMap,
            new ServerWrapper(server), connectorOptions.timeoutInMillis)
          logger.debug(s"Service for $ports is being started....")
          service.start()
          servicesMap.put(key, service)
        }
      }
    }

    val service = servicesMap.get(key)
    if (service.timeoutInMillis < connectorOptions.timeoutInMillis)
      logger.warn(
        s"Unable to change the GpfdistService timeout after the service has started. " +
        s"GpfdistService started with timeout ${service.timeoutInMillis} but " +
        s"a higher timeout (${connectorOptions.timeoutInMillis}) is configured. " +
        s"Ensure that the desired GpfdistService timeout is set for the first " +
        s"operation that accesses Greenplum.")
    service
  }

  def stopAndRemove(key: ServiceKey): Unit = {
    val service = servicesMap.remove(key)
    if (service != null) service.stop()
    else logger.warn(s"Unable to find service for $key")
  }
}
