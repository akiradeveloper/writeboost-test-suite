package dmtest.writeboost

import dmtest._
import dmtest.stack._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class REPRO_147 extends DMTestSuite {
  test("nr_cur_batched_writeback adaptively shrinks") {
    slowDevice(Sector.G(1)) { backing =>
      Memory(Sector.M(32)) { caching =>
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
          while (!writer.isCompleted) {
            logger.debug(s"n=${s.status.tunables("nr_cur_batched_writeback")}")
            if (s.status.tunables("nr_cur_batched_writeback") == 1)
              b = true
          }
          assert(b)
          Thread.sleep(1)
        }
      }
    }
  }
}
