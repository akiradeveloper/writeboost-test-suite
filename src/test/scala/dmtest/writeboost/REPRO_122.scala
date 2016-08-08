package dmtest.writeboost

import dmtest._
import dmtest.stack._

class REPRO_122 extends DMTestSuite {
  // ignore because the param isn't implemented yet
  test("nr_read_cache_cells works") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        val m = Map("nr_read_cache_cells" -> 32, "read_cache_threshold" -> 1)
        import PatternedSeqIO._
        val pat = Seq(Read(Sector.K(4)), Skip(Sector.K(4)))
        val pio = new PatternedSeqIO(pat)

        Writeboost.Table(backing, caching, m).create { s =>
          pio.maxIOAmount = Sector.K(4) * 31 * 2
          pio.run(s)
          Thread.sleep(5000)

          s.dropTransient()
          pio.run(s)
          val st = s.status.stat
          assert(st(Writeboost.StatKey(false, true, false, true)) === 0)
        }

        Writeboost.Table(backing, caching, m).create { s =>
          pio.maxIOAmount = Sector.K(4) * 32 * 2
          pio.run(s)
          Thread.sleep(5000) // wait for injection

          s.dropTransient()
          pio.run(s)
          val st = s.status.stat
          assert(st(Writeboost.StatKey(false, true, false, true)) === 32)
        }

        Writeboost.Table(backing, caching, m).create { s =>
          pio.maxIOAmount = Sector.K(4) * 33 * 2
          pio.run(s)
          Thread.sleep(5000) // wait for injection

          s.dropTransient()
          pio.run(s)
          val st = s.status.stat
          assert(st(Writeboost.StatKey(false, true, false, true)) === 32)
        }
      }
    }
  }
}
