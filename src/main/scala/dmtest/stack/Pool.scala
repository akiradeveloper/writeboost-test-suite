package dmtest.stack

import dmtest._

object Pool {
  case class S(pool: Pool, size: Sector) extends Stack {
    private val linear: Linear.S = pool.alloc(size)
    override def terminate: Unit = {
      pool.free(linear)
    }
    override def path = linear.path
  }
  case class Range(start: Sector, len: Sector) extends Ordered[Range] {
    override def compare(that: Range): Int = (this.start.unwrap - that.start.unwrap).toInt
    def take(size: Sector): (Range, Range) = {
      require(len >= size)
      (Range(start, size), Range(start + size, len - size))
    }
  }
  class FreeArea(initSize: Sector) {
    val m = scala.collection.mutable.SortedSet[Range](Range(Sector(0), initSize))
    def getFreeSpace(size: Sector): Range = {
      val target = m.find(_.len >= size).get
      m -= target
      val (res, rem) = target.take(size)
      if (rem.len > Sector(0)) m += rem
      res
    }
    def merge(): Boolean = {
      val a = m.toList
      val b = m.toList.drop(1)
      a.zip(b).find { case (x, y) =>
        x.start + x.len == y.start
      } match {
        case Some((x, y)) =>
          m -= x
          m -= y
          m += Range(x.start, x.len + y.len)
          true
        case None => false
      }
    }
    def release(range: Range) = {
      m += range
      while (merge) {}
    }
  }
}

class Pool(pool: Stack) {
  import Pool._
  val device = BlockDevice(pool.path)
  val freeArea = new FreeArea(device.size)
  def alloc(size: Sector): Linear.S = {
    val space = freeArea.getFreeSpace(size)
    EmptyStack().reload(Linear.T(pool, space.start, space.len))
  }
  def free(linearS: Linear.S): Unit = {
    linearS.purge()
    val space = Range(linearS.start, linearS.len)
    freeArea.release(space)
  }
}
