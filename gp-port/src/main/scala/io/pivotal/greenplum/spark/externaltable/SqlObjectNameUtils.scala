package io.pivotal.greenplum.spark.externaltable

object SqlObjectNameUtils {
  /** Double-quote an identifier to preserve case and handle special characters. */
  def escape(name: String): String = s""""${name.replace("\"", "\"\"")}""""
}
