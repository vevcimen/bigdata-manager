package io.pivotal.greenplum.spark.externaltable

import java.util.NoSuchElementException

/**
 * Abstract iterator that supports a "close on finish" pattern.
 * Subclasses implement getNext() and close(); hasNext/next are handled here.
 */
abstract class NextIterator[U] extends Iterator[U] {

  private var gotNext   = false
  private var nextValue : U = _  // uninitialized
  private var closed    = false
  protected var finished = false

  def getNext(): U
  def close(): Unit

  def closeIfNeeded(): Unit =
    if (!closed) { closed = true; close() }

  override def hasNext: Boolean = {
    if (!finished && !gotNext) {
      nextValue = getNext()
      if (finished) closeIfNeeded()
      gotNext = true
    }
    !finished
  }

  override def next(): U = {
    if (!hasNext) throw new NoSuchElementException("End of stream")
    gotNext = false
    nextValue
  }
}
