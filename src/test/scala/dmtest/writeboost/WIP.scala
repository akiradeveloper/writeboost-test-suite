package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.fs._
import scala.util.Random

class WIP extends DMTestSuite {
  test("cancel cells") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(64)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching, Map("read_cache_threshold" -> 1, "write_around_mode" -> 1)).create { s =>
          import PatternedSeqIO._

          val maxIOAmount = Sector.K(4) * 2047 * 2

          val reader = new PatternedSeqIO(Seq(Read(Sector.K(4)), Skip(Sector.K(4))))
          reader.maxIOAmount = maxIOAmount

          val writer = new PatternedSeqIO(Seq(Write(Sector.K(4)), Skip(Sector.K(4))))
          writer.maxIOAmount = maxIOAmount

          reader.run(s) // stage all read date into the cells

          writer.run(s) // cancel all cells

          reader.run(s) // no read hits

          val st = s.status.stat
          assert(st(Writeboost.StatKey(false, true, false, true)) === 0)
        }
      }
    }
  }
}
