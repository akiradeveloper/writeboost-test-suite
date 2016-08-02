package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.fs._
import scala.util.Random

class WIP extends DMTestSuite {
  test("write invalidates the read caches") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)

        val table = Writeboost.Table(backing, caching, Map("write_around_mode" -> 1, "read_cache_threshold" -> 1))

        import PatternedSeqIO._
        val pat1 = Seq(Write(Sector.K(3)), Skip(Sector.K(5)))
        val pio1 = new PatternedSeqIO(pat1)

        val pat2 = Seq(Read(Sector.K(4)), Skip(Sector.K(4)))
        val pio2 = new PatternedSeqIO(pat2)

        // staging
        table.create { s =>
          pio2.maxIOAmount = Sector.M(32)
          pio2.run(s)
          Writeboost.Status.parse(s.dm.status())
        }

        table.create { s =>
          // invalidating (by partial writes)
          pio1.maxIOAmount = Sector.M(32)
          pio1.run(s)
          val st1 = Writeboost.Status.parse(s.dm.status())
          assert(st1.stat(Writeboost.StatKey(true, true, false, false)) > 0)

          // won't hit
          pio2.maxIOAmount = Sector.M(32)
          pio2.run(s)
          val st2 = Writeboost.Status.parse(s.dm.status())
          assert(st2.stat(Writeboost.StatKey(false, true, false, true)) === 0)
        }
      }
    }
  }
}
