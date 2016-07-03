package dmtest

import java.util.concurrent.atomic.AtomicLong

object RandName {
  val PREFIX = "dmtest"
  val m = scala.collection.mutable.Set[String]()
  val i = new AtomicLong(0)
  def alloc: String = {
    val newName = s"${PREFIX}-${i.getAndIncrement()}"
    newName
  }
}
