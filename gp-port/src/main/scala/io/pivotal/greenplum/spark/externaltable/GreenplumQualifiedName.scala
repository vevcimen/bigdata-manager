package io.pivotal.greenplum.spark.externaltable

/** Represents qualified Greenplum object names. */
sealed trait GreenplumQualifiedName {
  def schema: String
  def name: String
  override def toString: String = s""""$schema"."$name""""
}

object GreenplumQualifiedName {

  case class Table(schema: String, name: String) extends GreenplumQualifiedName

  case class TempTable(name: String) extends GreenplumQualifiedName {
    val schema: String = "public"
    override def toString: String = s""""$name""""
  }

  def forTable(schema: String, name: String): Table      = Table(schema, name)
  def forTempTable(name: String)            : TempTable  = TempTable(name)
}
