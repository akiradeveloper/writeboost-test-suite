package dmtest

import stack._
import fs._

class WIP extends DMTestSuite {
  test("write-around can log rotate") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(10)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching, Map("write_around_mode" -> 1, "read_cache_threshold" -> 1)).create { s =>
          import PatternedSeqIO._

          val reader = new PatternedSeqIO(Seq(Read(Sector.K(4)), Skip(Sector.K(4))))
          reader.maxIOAmount = Sector.G(1)
          reader.run(s)

          Thread.sleep(5000)

          reader.maxIOAmount = Sector.G(1)
          reader.run(s)

          assert(s.status.currentId > s.status.nrSegments)
        }
      }
    }
  }
}

