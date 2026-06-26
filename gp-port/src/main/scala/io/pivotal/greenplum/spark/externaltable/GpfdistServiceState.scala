package io.pivotal.greenplum.spark.externaltable

/** Mirrors the state names returned by Jetty's server.getState() (capitalized). */
object GpfdistServiceState extends Enumeration {
  val Stopped, Failed, Starting, Started, Stopping, Running = Value
}
