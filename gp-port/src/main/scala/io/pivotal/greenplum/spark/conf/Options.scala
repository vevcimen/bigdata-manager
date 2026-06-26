package io.pivotal.greenplum.spark.conf

import scala.util.{Failure, Success, Try}

/** ADT for specifying what happens when a config option is absent. */
sealed trait WhatIfMissing
case object ErrorIfMissing                    extends WhatIfMissing
case class  Default(value: String)            extends WhatIfMissing

/** Trait providing typed config-option lookup backed by a Map[String,String]. */
trait Options {
  def parameters: Map[String, String]

  /** Get required option (throws if missing). */
  def option(optionName: String, whatIfMissing: WhatIfMissing): String =
    (parameters.get(optionName), whatIfMissing) match {
      case (None, ErrorIfMissing)   =>
        throw new IllegalArgumentException(s"requirement failed: Option '$optionName' is required.")
      case (None, Default(v))       => v
      case (Some(v), _)             => v
    }

  /** Get option with conversion, returning a T (throws on missing/parse error). */
  def option[T](optionName: String, whatIfMissing: WhatIfMissing, convert: (String, String) => Try[T]): T =
    convert(option(optionName, whatIfMissing), optionName).get

  /** Get optional value as Option[String]. */
  def option(optionName: String): Option[String] =
    parameters.get(optionName)

  /** Get optional value with conversion. */
  def option[T](optionName: String, convert: (String, String) => Try[T]): Option[T] =
    option(optionName).map(v => convert(v, optionName).get)

  /** Boolean converter. */
  def bool: (String, String) => Try[Boolean] = (value, name) =>
    Try(value.toBoolean).recoverWith { case _ =>
      Failure(new IllegalArgumentException(s"'$name' should be true/false, but was '$value'"))
    }

  /** Natural (≥ 0) long converter. */
  def naturalLong: (String, String) => Try[Long] = (value, name) =>
    Try(value.toLong).flatMap { v =>
      if (v >= 0) Success(v)
      else Failure(new IllegalArgumentException(s"'$name' must be ≥ 0, but was $v"))
    }

  /** Int converter. */
  def int: (String, String) => Try[Int] = (value, name) =>
    Try(value.toInt).recoverWith { case _ =>
      Failure(new IllegalArgumentException(s"'$name' should be an int, but was '$value'"))
    }

  /** Positive int converter. */
  def positiveInt: (String, String) => Try[Int] = (value, name) =>
    int(value, name).flatMap { v =>
      if (v > 0) Success(v)
      else Failure(new IllegalArgumentException(s"'$name' must be > 0, but was $v"))
    }
}
