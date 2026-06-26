package io.pivotal.greenplum.spark.externaltable

/**
 * Key for the per-executor gpfdist service map; keyed on the configured port list
 * so that services sharing the same ports are reused.
 */
case class ServiceKey(port: List[Int])
