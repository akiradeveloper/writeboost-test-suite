package dmtest.stack

import dmtest._
import scala.collection.mutable

object Writeboost {
  type TunableKind = String
  private val TunableKinds = Seq(
    "writeback_threshold",
    "nr_max_batched_writeback",
    "update_sb_record_interval",
    "sync_data_interval",
    "read_cache_threshold",
    "write_through_mode"
  )
  private def validateTunableKind(x: TunableKind): Unit = {
    if (!TunableKinds.contains(x)) {
      logger.error(s"tunable key ${x} isn't defined")
      assert(false)
    }
  }
  def sweepCaches(cacheDev: Stack) = Shell(s"dd if=/dev/zero of=${cacheDev.bdev.path} oflag=direct bs=512 count=1")
  case class Table(backingDev: Stack, cacheDev: Stack, tunables: Map[TunableKind, Int] = Map.empty) extends DMTable[Writeboost] {
    override def f: (DMStack) => Writeboost = (a: DMStack) => Writeboost(a, this)
    override def line: String = {
      val nrTunables = tunables.size * 2
      val asList = tunables map { case (k, v) => validateTunableKind(k); s"${k} ${v}" } mkString " "
      val optionalArgs = if (nrTunables > 0) {
        " ${nrTunables} ${asList}"
      } else {
        ""
      }
      s"0 ${backingDev.bdev.size} writeboost ${backingDev.bdev.path} ${cacheDev.bdev.path}" + optionalArgs
    }
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
          for (i <- 0 until 16) {
            val k = StatKey.fromIndex(i)
            val v = q.dequeue.toInt
            result += k -> v
          }
          result.toMap
        },
        q.dequeue.toInt,
        {
          val result = mutable.Map[String, Int]()
          q.dequeue // discard nr_tunables
          while (!q.isEmpty) {
            val k = q.dequeue()
            validateTunableKind(k)
            val v = q.dequeue().toInt
            result += k -> v
          }
          result.toMap
        }
      )
    }
  }
  case class StatKey(write: Boolean, hit: Boolean, onBuffer: Boolean, fullSize: Boolean)
  object StatKey {
    def fromIndex(i: Int): StatKey = {
      def k(shift: Int): Boolean = (i & (1 << shift)) != 0
      StatKey(k(3), k(2), k(1), k(0))
    }
  }
  case class Status(
    cursorPos: Int,
    nrCacheBlocks: Int,
    nrSegments: Int,
    currentId: Int,
    lastFlushedId: Int,
    lastWritebackId: Int,
    nrDirtyCacheBlocks: Int,
    stat: Map[StatKey, Int],
    nrPartialFlushed: Int,
    tunables: Map[TunableKind, Int]
  )
}
case class Writeboost(delegate: DMStack, table: Writeboost.Table) extends DMStackDecorator[Writeboost] {
  override def subStacks = Seq(table.backingDev, table.cacheDev)
  def dropCaches(): Unit = dm.message("drop_caches")
  def clearStats(): Unit = dm.message("clear_stats")
}
