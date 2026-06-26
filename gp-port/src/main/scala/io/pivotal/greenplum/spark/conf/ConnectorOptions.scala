package io.pivotal.greenplum.spark.conf

import com.typesafe.scalalogging.LazyLogging
import io.pivotal.greenplum.spark.ConnectorUtils
import org.apache.commons.lang3.StringUtils
import org.apache.spark.SparkEnv
import org.greenplum.spark.GpfdistConf

import java.time.Duration
import scala.util.Try

/**
 * Connector (gpfdist / network) options parsed from the Spark DataSource parameters map.
 */
class ConnectorOptions(
    override val parameters: Map[String, String],
    gpfdistConf            : GpfdistConf,
    val env                : Map[String, String],
    val connectorUtils     : ConnectorUtils
) extends Serializable with Options with LazyLogging {

  import ConnectorOptions._

  val port: List[Int] = option(GPDB_NETWORK_PORT)
    .map { s =>
      val portString =
        if (StringUtils.startsWith(s, GPDB_ENV_VAR_PREFIX))
          env.getOrElse(s.substring(GPDB_ENV_VAR_PREFIX.length), "0")
        else s
      parsePortString(portString)
    }
    .getOrElse(List(gpfdistConf.listenPort))

  val useLocalHostname: Boolean =
    option(GPDB_NETWORK_HOSTNAME, Default("false"), bool)

  val serverAddressFromEnvironment: Option[String] =
    option(GPDB_NETWORK_ADDRESS)
      .filter(!_.startsWith(GPDB_ENV_VAR_PREFIX))
      .flatMap(key => env.get(key.stripPrefix(GPDB_ENV_VAR_PREFIX)))

  val networkInterfaceName: Option[String] =
    option(GPDB_NETWORK_INTERFACE).flatMap { v =>
      if (v.startsWith(GPDB_ENV_VAR_PREFIX)) env.get(v.stripPrefix(GPDB_ENV_VAR_PREFIX))
      else Some(v)
    }

  private val MAX_TIMEOUT_MILLIS: Long = Duration.ofHours(2).toMillis

  val timeoutInMillis: Long =
    option(GPDB_NETWORK_TIMEOUT, naturalLong).getOrElse(Duration.ofMinutes(5).toMillis)

  require(
    timeoutInMillis <= MAX_TIMEOUT_MILLIS,
    s"Option '$GPDB_NETWORK_TIMEOUT' has a value of '$timeoutInMillis' that is greater than the maximum allowed value of $MAX_TIMEOUT_MILLIS milliseconds."
  )

  val matchDistributionPolicy: Boolean =
    option(MATCH_DISTRIBUTION_POLICY, Default("false"), bool)

  def getServerHost: String = ConnectorOptions.GPDB_DEFAULT_SERVER_HOST
}

object ConnectorOptions {
  val GPDB_ENV_VAR_PREFIX        = "env:"
  val GPDB_NETWORK_PORT          = "server.port"
  val GPDB_NETWORK_HOSTNAME      = "server.useHostname"
  val GPDB_NETWORK_ADDRESS       = "server.address"
  val GPDB_NETWORK_INTERFACE     = "server.networkInterface"
  val GPDB_NETWORK_TIMEOUT       = "server.timeout"
  val GPDB_DEFAULT_SERVER_HOST   = "0.0.0.0"
  val GPDB_DEFAULT_NETWORK_INTERFACE = "eth0"
  val MATCH_DISTRIBUTION_POLICY  = "matchDistributionPolicy"

  def apply(): ConnectorOptions = new ConnectorOptions(
    Map.empty,
    new GpfdistConf(SparkEnv.get.conf),
    Map.empty,
    new ConnectorUtils()
  )

  def apply(params: Map[String, String]): ConnectorOptions =
    new ConnectorOptions(params, new GpfdistConf(SparkEnv.get.conf), Map.empty, new ConnectorUtils())

  def parsePortString(portStr: String): List[Int] =
    portStr.split(',')
      .flatMap { s =>
        parsePortOrRange(s).recoverWith {
          case e: Exception =>
            scala.util.Failure(new IllegalArgumentException(
              s"failed to parse port string '$portStr': ${e.getMessage}", e))
        }.get
      }
      .distinct
      .sorted
      .toList

  private def parsePortOrRange(str: String): Try[List[Int]] = Try {
    val parts = str.split('-').map(s => Try(s.trim.toInt).flatMap { p =>
      if (p < 0 || p > 65535)
        scala.util.Failure(new IllegalArgumentException(
          s"$p is not a valid port number. Specify a value between 0 and 65535"))
      else scala.util.Success(p)
    }.get)
    parts.length match {
      case 1 => List(parts(0))
      case 2 => (parts(0) to parts(1)).toList
      case _ => throw new IllegalArgumentException("invalid port range")
    }
  }
}
