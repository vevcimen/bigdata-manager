package io.pivotal.greenplum.spark.jdbc

import com.typesafe.scalalogging.LazyLogging
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.pivotal.greenplum.spark.conf.ConnectionPoolOptions

import java.util.Properties
import javax.sql.DataSource

object HikariProvider extends LazyLogging {

  private val PROPERTY_PREFIX = "hikari"

  def createDataSource(key: ConnectionKey, password: Option[String],
                       driver: String, options: ConnectionPoolOptions): DataSource = {
    // Register the driver
    Class.forName(driver)

    val hikariConfig = getHikariConfig(options)
    hikariConfig.setJdbcUrl(key.jdbcUrl)
    hikariConfig.setUsername(key.userName)
    password match {
      case Some(p) => hikariConfig.setPassword(p)
      case None    => logger.debug("No password is used")
    }
    logger.debug(
      s"Creating connection pool with ${options.maximumPoolSize} max connections for ${key.jdbcUrl}, user=${key.userName}")
    new HikariDataSource(hikariConfig)
  }

  def getHikariConfig(options: ConnectionPoolOptions): HikariConfig = {
    val props = new Properties()
    // Pass through any extra hikari.* options
    val config = new HikariConfig(props)
    config.setMaximumPoolSize(options.maximumPoolSize)
    config.setIdleTimeout(options.idleTimeoutMs)
    config.setMinimumIdle(options.minimumIdle)
    config
  }
}
