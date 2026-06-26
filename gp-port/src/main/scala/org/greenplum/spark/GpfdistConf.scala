package org.greenplum.spark

import org.apache.spark.SparkConf

/** Spark conf keys for gpfdist configuration (per executor). */
class GpfdistConf(sparkConf: SparkConf) extends Serializable {

  import GpfdistConf._

  def host: Option[String] = sparkConf.getOption(HOST_KEY)

  def listenPort: Int = getInt(LISTEN_PORT_KEY, LISTEN_PORT_DEFAULT_VALUE)

  def locationPort: Option[Int] = getOptionInt(LOCATION_PORT_KEY, None)

  def isSSL: Boolean = getBoolean(IS_SSL_KEY, IS_SSL_DEFAULT_VALUE)

  private def getInt(key: String, default: Int): Int =
    sparkConf.getOption(key).map(v => toType(v, _.trim.toInt, key, "int")).getOrElse(default)

  private def getBoolean(key: String, default: Boolean): Boolean =
    sparkConf.getOption(key).map(v => toType(v, _.trim.toBoolean, key, "boolean")).getOrElse(default)

  private def getOptionInt(key: String, default: Option[Int]): Option[Int] =
    sparkConf.getOption(key).map(v => toType(v, _.trim.toInt, key, "int")).orElse(default)

  private def toType[T](s: String, converter: String => T, key: String, configType: String): T =
    try converter(s.trim)
    catch {
      case _: NumberFormatException | _: IllegalArgumentException =>
        throw new IllegalArgumentException(s"$key should be $configType, but was $s")
    }
}

object GpfdistConf {
  val LISTEN_PORT_KEY            = "spark.greenplum.gpfdist.listenPort"
  private val HOST_KEY           = "spark.greenplum.gpfdist.host"
  private val LOCATION_PORT_KEY  = "spark.greenplum.gpfdist.locationPort"
  private val IS_SSL_KEY         = "spark.greenplum.gpfdist.ssl"

  private val LISTEN_PORT_DEFAULT_VALUE = 0
  private val IS_SSL_DEFAULT_VALUE      = false
}
