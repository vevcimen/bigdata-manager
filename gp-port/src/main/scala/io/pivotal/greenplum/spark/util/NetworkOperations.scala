package io.pivotal.greenplum.spark.util

import java.net.{Inet4Address, InetAddress, NetworkInterface}
import scala.jdk.CollectionConverters._
import scala.util.Try

class NetworkOperations extends Serializable {

  def getInet4AddressByNetworkInterfaceName(name: String): Try[Inet4Address] = Try {
    val iface = NetworkInterface.getByName(name)
    if (iface == null) throw new IllegalArgumentException(s"Network interface '$name' not found")
    iface.getInetAddresses.asScala
      .collect { case a: Inet4Address => a }
      .nextOption()
      .getOrElse(throw new IllegalArgumentException(s"No IPv4 address on interface '$name'"))
  }
}
