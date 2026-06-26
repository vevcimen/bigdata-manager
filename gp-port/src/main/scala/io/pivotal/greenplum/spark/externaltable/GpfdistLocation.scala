package io.pivotal.greenplum.spark.externaltable

/** Location URL for a gpfdist external table. */
case class GpfdistLocation(protocol: String, host: String, port: Int, path: String) {

  def withPath(newPath: String): GpfdistLocation = copy(path = newPath)

  def getUrl: String = s"$protocol://$host:$port$path"

  override def toString: String = getUrl
}

object GpfdistLocation {
  def apply(host: String, port: Int, ssl: Boolean): GpfdistLocation =
    GpfdistLocation(if (ssl) "gpfdists" else "gpfdist", host, port, "")

  def apply(host: String, port: Int): GpfdistLocation =
    apply(host, port, ssl = false)
}
