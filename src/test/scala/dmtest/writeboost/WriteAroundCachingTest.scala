package dmtest.writeboost

import dmtest._
import dmtest.stack._

class WriteAroundCachingTest extends DMTestSuite {
  test("read caching work") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        val table = Writeboost.Table(backing, caching, Map("write_around_mode" -> 1, "read_cache_threshold" -> 1))

        import PatternedSeqIO._
        val pat = Seq(Read(Sector.K(4)), Skip(Sector.K(4)))
        val pio = new PatternedSeqIO(pat)
        pio.maxIOAmount = Sector.M(16)
        logger.debug("staging data")
        val st1 = table.create { s =>
          pio.run(s)
          Writeboost.Status.parse(s.dm.status())
        }
        logger.debug("and then cache hit")
        val st2 = table.create { s =>
          pio.run(s)
          Writeboost.Status.parse(s.dm.status())
        }
        val key = Writeboost.StatKey(false, true, false, true)
        assert(st2.stat(key) > st1.stat(key))
      }
    }
  }
  test("verify data") {
    slowDevice(Sector.M(1024)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        backing.bdev.zeroFill()
        val rp = new RandomPatternVerifier(backing, Sector.K(4))
        rp.stamp(2)

        Writeboost.sweepCaches(caching)
        val table = Writeboost.Table(backing, caching, Map("write_around_mode" -> 1, "read_cache_threshold" -> 127))
        table.create { s =>
          assert(rp.verify(withStack = s)) // stage all data
          Writeboost.Status.parse(s.dm.status()) // not used but for debugging
        }
        table.create { s =>
          val st1 = Writeboost.Status.parse(s.dm.status())
          assert(rp.verify(withStack = s)) // should read hit
          val st2 = Writeboost.Status.parse(s.dm.status())
          val key = Writeboost.StatKey(false, true, false, true) // fullsize read hit
          assert(st2.stat(key) > st1.stat(key))
        }
      }
    }
  }
  test("write won't cache") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)

        val table = Writeboost.Table(backing, caching, Map("write_around_mode" -> 1, "read_cache_threshold" -> 1))

        import PatternedSeqIO._
        val pat1 = Seq(Write(Sector.K(4)), Skip(Sector.K(4)))
        val pio1 = new PatternedSeqIO(pat1)
        pio1.maxIOAmount = Sector.M(16)
        table.create { s =>
          pio1.run(s)
          Writeboost.Status.parse(s.dm.status())
        }

        val pat2 = Seq(Read(Sector.K(4)), Skip(Sector.K(4)))
        val pio2 = new PatternedSeqIO(pat2)
        pio2.maxIOAmount = Sector.M(16)
        val st = table.create { s =>
          pio2.run(s)
          Writeboost.Status.parse(s.dm.status())
        }

        val key = Writeboost.StatKey(false, true, false, true)
        assert(st.stat(key) === 0)
      }
    }
  }
}

