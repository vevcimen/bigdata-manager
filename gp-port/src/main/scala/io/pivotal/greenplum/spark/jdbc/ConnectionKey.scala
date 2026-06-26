package io.pivotal.greenplum.spark.jdbc

import io.pivotal.greenplum.spark.conf.ConnectionPoolOptions

case class ConnectionKey(
    jdbcUrl                 : String,
    userName                : String,
    hashedPassword          : String,
    connectionPoolOptionsHash: Int
)

object ConnectionKey {
  def apply(jdbcUrl: String, userName: String, password: Option[String],
            poolOpts: ConnectionPoolOptions): ConnectionKey =
    ConnectionKey(
      jdbcUrl,
      userName,
      password.map(p => java.security.MessageDigest.getInstance("MD5")
        .digest(p.getBytes("UTF-8"))
        .map("%02x".format(_)).mkString).getOrElse(""),
      poolOpts.hashCode()
    )
}
