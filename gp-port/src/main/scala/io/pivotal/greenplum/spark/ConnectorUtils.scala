package io.pivotal.greenplum.spark

import io.pivotal.greenplum.spark.conf.ConnectorOptions
import io.pivotal.greenplum.spark.externaltable.GpfdistLocation
import io.pivotal.greenplum.spark.util.NetworkOperations

import java.net.{Inet4Address, InetAddress}
import scala.util.Try

class ConnectorUtils(networkOperations: NetworkOperations = new NetworkOperations())
    extends Serializable {

  def getLocation(connectorOptions: ConnectorOptions, localGpfdistPort: Int): GpfdistLocation =
    GpfdistLocation(getLocationHost(connectorOptions), getLocationPort(connectorOptions, localGpfdistPort))

  def getLocationPathPrefix(applicationId: String, executorId: String, threadId: Long): String =
    s"/$applicationId/exec/$executorId/$threadId"

  def getHostAddressByNetworkInterfaceByName(name: String): Try[String] =
    networkOperations.getInet4AddressByNetworkInterfaceName(name).map(_.getHostAddress)

  private def getLocationHost(opts: ConnectorOptions): String = {
    if (opts.useLocalHostname)
      InetAddress.getLocalHost.getHostName
    else opts.serverAddressFromEnvironment
      .orElse(opts.networkInterfaceName.flatMap { n =>
        networkOperations.getInet4AddressByNetworkInterfaceName(n).toOption.map(_.getHostAddress)
      })
      .getOrElse(InetAddress.getLocalHost.getHostAddress)
  }

  private def getLocationPort(opts: ConnectorOptions, localGpfdistPort: Int): Int =
    localGpfdistPort
}
