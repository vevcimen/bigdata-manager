package io.pivotal.greenplum.spark.externaltable

class WebException(val code: Int, msg: String) extends RuntimeException(msg) {
  def this(code: Int) = this(code, "")
}

object WebException {
  def apply(code: Int, msg: String): WebException = new WebException(code, msg)
  def apply(code: Int): WebException              = new WebException(code)
}
