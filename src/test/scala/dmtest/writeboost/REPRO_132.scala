package dmtest.writeboost

import dmtest._
import dmtest.stack._

class REPRO_132 extends DMTestSuite {
  test("shouldn't replay the logs when write-around mode") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        import PatternedSeqIO._

        val pat = Seq(Read(Sector.K(4)), Skip(Sector.K(4)))
        val pio = new PatternedSeqIO(pat)
        pio.maxIOAmount = Sector.M(32)

        val table = Writeboost.Table(backing, caching, Map("write_around_mode" -> 1, "read_cache_threshold" -> 1))
        // staging
        table.create { s =>
          pio.run(s)

        }
        // will not hit
        val st = table.create { s =>
          pio.run(s)
          Writeboost.Status.parse(s.dm.status())
        }
        assert(st.stat(Writeboost.StatKey(false, true, false, true)) === 0)
      }
    }
  }
}
