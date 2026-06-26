package io.pivotal.greenplum.spark

import scala.util.{Failure, Try}

object ErrorHandling {

  /** Appends `cause` to the end of the exception chain of `original`. */
  def appendCauseToErrorChain(original: Throwable, cause: Throwable): Unit = {
    var last    = original
    var counter = 100
    while (last.getCause != null && counter > 0) {
      last    = last.getCause
      counter -= 1
    }
    if (counter > 0) last.initCause(cause)
  }

  /** Returns a PartialFunction for use with `recoverWith` that wraps the throwable with `message`. */
  def wrapErrorMessage[U](message: String): PartialFunction[Throwable, Try[U]] = {
    case e: Throwable => Failure(new RuntimeException(message, e))
  }
}
