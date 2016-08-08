package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.fs._
import scala.util.Random

class WIP extends DMTestSuite {
  // FIXME nr_cache_cells
  ignore("log rotates") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(2)) { caching => // superblock + 2 segments
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching, Map("nr_cache_cells" -> 127, "read_cache_threshold" -> 1, "write_around_mode" -> 1)).create { s =>
          import PatternedSeqIO._

          val U = Sector.K(4) * 127 * 2

          val reader = new PatternedSeqIO(Seq(Read(Sector.K(4)), Skip(Sector.K(4))))
          reader.startingOffset = Sector(0)
          reader.maxIOAmount = U * 3
          reader.run(s) // stage all read date into the cells
          Thread.sleep(10000) // wait for injection

          // caches = [U1, U2]

          reader.startingOffset = Sector(0)
          reader.maxIOAmount = U * 1
          reader.run(s)
          assert(s.status.stat(Writeboost.StatKey(false, true, false, true)) === 0)
          Thread.sleep(10000) // wait for injection

          // caches = [U2, U0]

          reader.startingOffset = U * 2
          reader.maxIOAmount = U * 1
          reader.run(s)
          assert(s.status.stat(Writeboost.StatKey(false, true, false, true)) === 127)
        }
      }
    }
  }
}
