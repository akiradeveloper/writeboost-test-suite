package dmtest.stack

import dmtest._
import scala.collection.mutable

object Writeboost {
  def sweepCaches(cacheDev: Stack) = Shell(s"dd status=none if=/dev/zero of=${cacheDev.bdev.path} oflag=direct bs=512 count=1")
  case class Table(backingDev: Stack, cacheDev: Stack, tunables: Map[String, Int] = Map.empty) extends DMTable[Writeboost] {
    private def validateOptionals(x: String): Unit = {
      val allowed = Seq(
        "writeback_threshold",
        "nr_max_batched_writeback",
        "update_sb_record_interval",
        "sync_data_interval",
        "read_cache_threshold",
        "write_around_mode", // static
        "nr_read_cache_cells" // static
      )
      if (!allowed.contains(x)) {
        logger.error(s"tunable key ${x} isn't defined")
        assert(false)
      }
    }
    override def f: (DMStack) => Writeboost = (a: DMStack) => Writeboost(a, this)
    override def line: String = {
      val nrTunables = tunables.size * 2
      val asList = tunables map { case (k, v) => validateOptionals(k); s"${k} ${v}" } mkString " "
      val optionalArgs: String = if (nrTunables > 0) {
        s" ${nrTunables} ${asList}"
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
      val stat = Status(
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
            val v = q.dequeue().toInt
            result += k -> v
          }
          result.toMap
        }
      )
      logger.debug(s"stat (parsed): ${stat}")
      stat
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
    tunables: Map[String, Int]
  )
}
case class Writeboost(delegate: DMStack, table: Writeboost.Table) extends DMStackDecorator[Writeboost] {
  override def onReloaded(): Unit = { clearStats() }

  // drop all transient data in the ram buffer to the caching device
  // suspend + resume is used dm-wide to resolve all remaining transient data.
  def dropTransient(): Unit = {
    dm.suspend()
    dm.resume()
  }
  // drop all dirty blocks from the caching device to the backing device
  // (ram buffer isn't concerned)
  def dropCaches(): Unit = dm.message("drop_caches")
  def clearStats(): Unit = dm.message("clear_stat")
  def status: Writeboost.Status = Writeboost.Status.parse(dm.status())
}
