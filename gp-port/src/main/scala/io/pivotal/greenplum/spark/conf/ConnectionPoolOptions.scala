package io.pivotal.greenplum.spark.conf

/** HikariCP connection-pool tuning options extracted from the connector parameters map. */
class ConnectionPoolOptions(rawParams: Map[String, String] = Map.empty)
    extends Serializable with Options {

  override val parameters: Map[String, String] =
    rawParams.map { case (k, v) => k.toLowerCase -> v }

  private val prefix = "pool"

  val minimumIdle    : Int = option("pool.minIdle",   Default("0"),     int)
  val maximumPoolSize: Int = option("pool.maxSize",   Default("1"),     positiveInt)
  val idleTimeoutMs  : Int = option("pool.timeoutMs", Default("30000"), positiveInt)
}

object ConnectionPoolOptions {
  def apply(): ConnectionPoolOptions = new ConnectionPoolOptions()
  def apply(params: Map[String, String]): ConnectionPoolOptions = new ConnectionPoolOptions(params)
}
