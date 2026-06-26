package io.pivotal.greenplum.spark.externaltable

import org.eclipse.jetty.server.{Connector, Server}

/** Thin wrapper around a Jetty Server to allow mocking in tests. */
class ServerWrapper(val server: Server) {

  def start(): Unit = server.start()

  def stop(): Unit = server.stop()

  def getState: String = server.getState

  def getConnectors: Array[Connector] = server.getConnectors

  def setConnectors(connectors: Array[Connector]): Unit = server.setConnectors(connectors)
}
