package io.pivotal.greenplum.spark.jdbc

import com.typesafe.scalalogging.LazyLogging
import io.pivotal.greenplum.spark.conf.{ConnectionPoolOptions, GreenplumOptions}

import java.sql.Connection
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

/** Thread-safe JDBC connection-pool manager (singleton per JVM). */
class ConnectionManager extends LazyLogging {

  private val pools = new ConcurrentHashMap[ConnectionKey, DataSource]()

  def getConnection(dbOpts: GreenplumOptions, autoCommit: Boolean = true): Connection = {
    val conn = getPooledConnection(dbOpts.url, dbOpts.user, dbOpts.password,
                                   dbOpts.driver, dbOpts.connectionPoolOptions)
    conn.setAutoCommit(autoCommit)
    conn
  }

  def getPooledConnection(jdbcUrl: String, userName: String, password: Option[String],
                           driver: String, options: ConnectionPoolOptions): Connection =
    getPooledDataSource(jdbcUrl, userName, password, driver, options).getConnection()

  def getPooledDataSource(jdbcUrl: String, userName: String, password: Option[String],
                           driver: String, options: ConnectionPoolOptions): DataSource = {
    val key = ConnectionKey(jdbcUrl, userName, password, options)
    if (!pools.containsKey(key)) {
      pools.synchronized {
        if (!pools.containsKey(key)) {
          val ds = HikariProvider.createDataSource(key, password, driver, options)
          pools.put(key, ds)
        }
      }
    }
    pools.get(key)
  }
}

object ConnectionManager {
  private val instance = new ConnectionManager()

  def getConnection(dbOpts: GreenplumOptions, autoCommit: Boolean = true): Connection =
    instance.getConnection(dbOpts, autoCommit)
}
