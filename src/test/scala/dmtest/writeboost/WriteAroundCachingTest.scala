package dmtest.writeboost

import dmtest._
import dmtest.stack._

class WriteAroundCachingTest extends DMTestSuite {
  test("read caching work") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)

        import PatternedSeqIO._

        val table = Writeboost.Table(backing, caching, Map("write_around_mode" -> 1, "read_cache_threshold" -> 1))
        table.create { s =>
          val pat = Seq(Read(Sector.K(4)), Skip(Sector.K(4)))
          val pio = new PatternedSeqIO(pat)
          pio.maxIOAmount = Sector.M(16)

          logger.debug("staging data")
          pio.run(s)
          val st1 = Writeboost.Status.parse(s.dm.status())
          logger.debug("and then cache hit")
          pio.run(s)
          val st2 = Writeboost.Status.parse(s.dm.status())

          val key = Writeboost.StatKey(false, true, false, true)
          assert(st2.stat(key) > st1.stat(key))
        }
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
          val st1 = Writeboost.Status.parse(s.dm.status()) // not used but for debugging

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

        import PatternedSeqIO._

        val table = Writeboost.Table(backing, caching, Map("write_around_mode" -> 1, "read_cache_threshold" -> 1))
        table.create { s =>
          val pat1 = Seq(Write(Sector.K(4)), Skip(Sector.K(4)))
          val pio1 = new PatternedSeqIO(pat1)
          pio1.maxIOAmount = Sector.M(16)
          pio1.run(s)
          Writeboost.Status.parse(s.dm.status())

          val pat2 = Seq(Read(Sector.K(4)), Skip(Sector.K(4)))
          val pio2 = new PatternedSeqIO(pat2)
          pio2.maxIOAmount = Sector.M(16)
          pio2.run(s)
          val st = Writeboost.Status.parse(s.dm.status())
          assert(st.stat(Writeboost.StatKey(false, true, false, true)) === 0)
        }
      }
    }
  }
  test("write invalidates the read caches") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        import PatternedSeqIO._

        val table = Writeboost.Table(backing, caching, Map("write_around_mode" -> 1, "read_cache_threshold" -> 1))
        table.create { s =>
          val pat1 = Seq(Write(Sector.K(3)), Skip(Sector.K(5)))
          val pio1 = new PatternedSeqIO(pat1)

          val pat2 = Seq(Read(Sector.K(4)), Skip(Sector.K(4)))
          val pio2 = new PatternedSeqIO(pat2)

          // staging
          pio2.maxIOAmount = Sector.M(32)
          pio2.run(s)
          Writeboost.Status.parse(s.dm.status())

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

          writer.run(s) // cancel all cells (try remove this line)

          reader.startingOffset = maxIOAmount
          reader.run(s)
          Thread.sleep(5000) // wait for injection

          reader.startingOffset = Sector(0)
          reader.run(s) // no read hits

          val st = s.status.stat
          assert(st(Writeboost.StatKey(false, true, false, true)) === 0)
        }
      }
    }
  }
  test("stage and should hit all") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(64)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching, Map("read_cache_threshold" -> 1, "write_around_mode" -> 1)).create { s =>
          import PatternedSeqIO._

          val maxIOAmount = Sector.K(4) * 2048 * 2

          val reader = new PatternedSeqIO(Seq(Read(Sector.K(4)), Skip(Sector.K(4))))
          reader.maxIOAmount = maxIOAmount

          reader.run(s) // stage all read date into the cells
          Thread.sleep(10000) // wait for injection

          reader.run(s) // no read hits

          val st = s.status.stat
          val onCache = Writeboost.StatKey(false, true, false, true)
          val onBuffer = Writeboost.StatKey(false, true, true, true)
          assert(st(onBuffer) + st(onCache) === 2048)
        }
      }
    }
  }
}

