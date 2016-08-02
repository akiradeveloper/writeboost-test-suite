package dmtest.writeboost

import dmtest._
import dmtest.stack._

class REPRO_122 extends DMTestSuite {
  // ignore because the param isn't implemented yet
  ignore("nr_read_cache_cells works") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        val m = Map("nr_read_cache_cells" -> 32, "read_cache_threshold" -> 1)
        import PatternedSeqIO._
        val pat = Seq(Read(Sector.K(4)), Skip(Sector.K(4)))
        val pio = new PatternedSeqIO(pat)

        pio.maxIOAmount = Sector.K(4) * 31 * 2
        val st1 = Writeboost.Table(backing, caching, m).create { s =>
          pio.run(s)
          Thread.sleep(5000)
          pio.run(s)
          s.status
        }
        assert(st1.stat(Writeboost.StatKey(false, true, false, true)) === 0)

        pio.maxIOAmount = Sector.K(4) * 33 * 2
        val st2 = Writeboost.Table(backing, caching, m).create { s =>
          pio.run(s)
          Thread.sleep(5000) // wait for injection
          pio.run(s)
          s.status
        }
        assert(st2.stat(Writeboost.StatKey(false, true, false, true)) === 32)
      }
    }
  }
}
