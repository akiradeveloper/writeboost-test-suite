package dmtest.writeboost

import dmtest._
import dmtest.stack._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class REPRO_147 extends DMTestSuite {
  test("nr_cur_batched_writeback adaptively shrinks") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        val table = Writeboost.Table(backing, caching, Map("nr_max_batched_writeback" -> 16))
        table.create { s =>
          s.bdev.write(Sector(0), DataBuffer.random(Sector.M(32).toB.toInt))

          val writer = Future {
            val data = DataBuffer.random(Sector.M(128).toB.toInt)
            s.bdev.write(Sector(0), data)
          }

          s.dm.message("writeback_threshold 100")
          var b = false
          while (!writer.isCompleted && !b) {
            logger.debug(s"n=${s.status.tunables("nr_cur_batched_writeback")}")
            if (s.status.tunables("nr_cur_batched_writeback") == 1)
              b = true
          }
          assert(b)

          Await.ready(writer, Duration.Inf)
        }
      }
    }
  }
  // degradation check
  test("writeback should recover full throttle") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(128)) { caching =>
        Writeboost.sweepCaches(caching)
        val table = Writeboost.Table(backing, caching, Map("nr_max_batched_writeback" -> 8))
        table.create { s =>
          // fullfill the caching device
          s.bdev.write(Sector(0), DataBuffer.random(Sector.M(128).toB.toInt))
          s.dm.message("writeback_threshold 100")

          s.dropTransient()
          def complt: Boolean = {
            val st = s.status
            st.lastWritebackId == st.lastFlushedId
          }

          var b = false
          while (!complt && !b) {
            logger.debug(s"n=${s.status.tunables("nr_cur_batched_writeback")}")
            if (s.status.tunables("nr_cur_batched_writeback") == 8)
              b = true
          }

          assert(b)
        }
      }
    }
  }
  // degradation check
  test("writeback should be full throttle when there are many empty segs") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(64)) { caching =>
        Writeboost.sweepCaches(caching)
        val table = Writeboost.Table(backing, caching, Map("nr_max_batched_writeback" -> 8))
        table.create { s =>
          s.bdev.write(Sector(0), DataBuffer.random(Sector.M(32).toB.toInt))
          s.dm.message("writeback_threshold 100")

          s.dropTransient()
          def complt: Boolean = {
            val st = s.status
            st.lastWritebackId == st.lastFlushedId
          }

          var b = false
          while (!complt && !b) {
            logger.debug(s"n=${s.status.tunables("nr_cur_batched_writeback")}")
            if (s.status.tunables("nr_cur_batched_writeback") == 8)
              b = true
          }

          assert(b)
        }
      }
    }
  }
}
