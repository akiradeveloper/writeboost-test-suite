package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.fs._
import scala.util.Random

class WIP extends DMTestSuite {
  test("write-around can log rotate") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(10)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching, Map("write_around_mode" -> 1, "read_cache_threshold" -> 1)).create { s =>
          import PatternedSeqIO._

          val reader = new PatternedSeqIO(Seq(Read(Sector.K(4)), Skip(Sector.K(4))))
          reader.maxIOAmount = Sector.M(30)

          reader.startingOffset = Sector(0)
          reader.run(s)

          Thread.sleep(5000)

          reader.startingOffset = Sector.M(30)
          reader.run(s)

          assert(s.status.currentId > s.status.nrSegments)
        }
      }
    }
  }
}
