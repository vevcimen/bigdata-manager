package io.pivotal.greenplum.spark

import org.apache.spark.sql.sources._

/**
 * Compiles Spark DataSource Filter objects into PostgreSQL/Greenplum WHERE clause predicates.
 * Replaces the removed dependency on JDBCRDD.compileFilter.
 */
object FilterCompiler {

  def compileFilter(f: Filter): Option[String] = f match {
    case EqualTo(col, v)               => Some(s"${quote(col)} = ${lit(v)}")
    case EqualNullSafe(col, v) if v == null => Some(s"${quote(col)} IS NULL")
    case EqualNullSafe(col, v)         =>
      Some(s"(${quote(col)} IS NOT NULL AND ${quote(col)} = ${lit(v)})")
    case GreaterThan(col, v)           => Some(s"${quote(col)} > ${lit(v)}")
    case GreaterThanOrEqual(col, v)    => Some(s"${quote(col)} >= ${lit(v)}")
    case LessThan(col, v)              => Some(s"${quote(col)} < ${lit(v)}")
    case LessThanOrEqual(col, v)       => Some(s"${quote(col)} <= ${lit(v)}")
    case In(col, values)               =>
      val literals = values.map(lit).mkString(", ")
      Some(s"${quote(col)} IN ($literals)")
    case IsNull(col)                   => Some(s"${quote(col)} IS NULL")
    case IsNotNull(col)                => Some(s"${quote(col)} IS NOT NULL")
    case And(left, right)              =>
      for (l <- compileFilter(left); r <- compileFilter(right)) yield s"($l) AND ($r)"
    case Or(left, right)               =>
      for (l <- compileFilter(left); r <- compileFilter(right)) yield s"($l) OR ($r)"
    case Not(child)                    =>
      compileFilter(child).map(c => s"NOT ($c)")
    case StringStartsWith(col, v)      => Some(s"${quote(col)} LIKE '${escapeLike(v)}%'")
    case StringEndsWith(col, v)        => Some(s"${quote(col)} LIKE '%${escapeLike(v)}'")
    case StringContains(col, v)        => Some(s"${quote(col)} LIKE '%${escapeLike(v)}%'")
    case _                             => None
  }

  def filterWherePredicate(filters: Array[Filter]): String =
    filters.flatMap(compileFilter).map(p => s"($p)").mkString(" AND ")

  private def quote(col: String): String = s""""${col.replace("\"", "\"\"")}""""

  private def lit(v: Any): String = v match {
    case null        => "NULL"
    case s: String   => s"'${s.replace("'", "''")}'"
    case b: Boolean  => b.toString
    case n: Number   => n.toString
    case _           => s"'${v.toString.replace("'", "''")}'"
  }

  private def escapeLike(s: String): String =
    s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
}
