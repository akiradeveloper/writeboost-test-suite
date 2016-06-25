package dmtest.stack

import dmtest._
import scala.collection.mutable

object Writeboost {
  object Table {
    def parse(gTable: DMState.Table): Table = ???
  }
  case class Table(backingDev: Stack, cacheDev: Stack) extends DMTable[Writeboost] {
    override def f: (DMStack) => Writeboost = ???
    override def line: String = ???
  }
  object Status {
    def parse(status: DMState.Status): Status = {
      val q = mutable.Queue[String]()
      q ++= status.args
      Status(
        q.dequeue.toInt,
        q.dequeue.toInt,
        q.dequeue.toInt,
        q.dequeue.toInt,
        q.dequeue.toInt,
        q.dequeue.toInt,
        q.dequeue.toInt,
        {
          val result = mutable.Map[StatKey, Int]()
          for (i <- 0 to 16) {
            val k = StatKey.fromIndex(i)
            val v = q.dequeue.toInt
            result += k -> v
          }
          result.toMap
        },
        {
          val result = mutable.Map[String, Int]()
          q.dequeue // discard nr_tunables
          while (!q.isEmpty) {
            val k = q.dequeue()
            val v = q.dequeue().toInt
            result += k -> v
          }
          result.toMap
        }
      )
    }
  }
  case class StatKey(write: Boolean, hit: Boolean, onBuffer: Boolean, fullSize: Boolean) {
    def i(b: Boolean, shift: Int) = (if (b) 1 else 0) << shift
    def toIndex = i(write, 3) + i(hit, 2) + i(onBuffer, 1) + i(fullSize, 0)
  }
  object StatKey {
    def fromIndex(i: Int): StatKey = {
      def k(shift: Int): Boolean = (i & (1 << shift)) != 0
      StatKey(k(3), k(2), k(1), k(0))
    }
  }
  case class Status(
    cursor_pos: Int,
    nr_cache_blocks: Int,
    nr_segments: Int,
    current_id: Int,
    last_flushed_id: Int,
    last_writeback_id: Int,
    nr_dirty_cache_blocks: Int,
    stat: Map[StatKey, Int],
    tunables: Map[String, Int]
  )
}
case class Writeboost(delegate: DMStack, table: Writeboost.Table) extends DMStackDecorator[Writeboost] {
  override def subsidiaries = Seq(table.backingDev, table.cacheDev)
}
