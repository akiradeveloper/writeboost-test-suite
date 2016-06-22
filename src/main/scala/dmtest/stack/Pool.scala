package dmtest.stack

import dmtest._

object Pool {
  case class S(pool: Pool, size: Sector) extends Stack {
    private val linear: Linear.S = pool.alloc(size)
    override def terminate: Unit = {
      linear.purge()
      pool.free(linear)
    }
    override def path: String = linear.path
  }
  case class Range(start: Sector, len: Sector) extends Ordered[Range] {
    override def compare(that: Range): Int = (this.start.unwrap - that.start.unwrap).toInt
  }
  class FreeArea(initSize: Sector) {
    val m = scala.collection.mutable.SortedSet[Range](Range(Sector(0), initSize))
    def +(range: Range): this.type = ???
    def -(range: Range): this.type = ???
  }
}

class Pool(pool: Direct.S) {
  import Pool._
  val device = BlockDevice(pool.path)
  private def getFreeSpace(size: Sector): Range = ???
  var freeArea = new FreeArea(device.size)
  def alloc(size: Sector): Linear.S = {
    val space = getFreeSpace(size)
    freeArea += space
    EmptyStack().reload(Linear.T(pool, space.start, space.len))
  }
  def free(linearS: Linear.S): Unit = {
    val space = Range(linearS.start, linearS.len)
    freeArea -= space
  }
}
