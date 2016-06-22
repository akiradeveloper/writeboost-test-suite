package dmtest

object RandNameAllocator {
  val PREFIX = "dmtest"
  val m = scala.collection.mutable.Set[String]()
  var i = 0
  def alloc: String = {
    val newName = s"${PREFIX}-${i}"
    i += 1
    newName
  }
}
