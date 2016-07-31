package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.fs._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ReadCachingTest extends DMTestSuite {
  test("no read caching") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          import PatternedSeqIO._
          val pat = Seq(Read(Sector.K(4)), Skip(Sector.K(4)))
          val pio = new PatternedSeqIO(pat)
          pio.maxIOAmount = Sector.M(16)
          // def run() = Shell(s"fio --name=test filename=${s.bdev.path} --io_limit=16M --rw=read:4K --bs=4K --direct=1")
          pio.run(s)
          val st1 = Writeboost.Status.parse(s.dm.status())
          pio.run(s)
          val st2 = Writeboost.Status.parse(s.dm.status())
          val key = Writeboost.StatKey(false, true, false, true)
          assert(st2.stat(key) === st1.stat(key))
        }
      }
    }
  }
  test("read cache") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        val table = Writeboost.Table(backing, caching, Map("read_cache_threshold" -> 1))
        // def run(s: Stack) = Shell(s"fio --name=test --filename=${s.bdev.path} --io_limit=16M --rw=read:4K --bs=4K --direct=1")
        import PatternedSeqIO._
        val pat = Seq(Read(Sector.K(4)), Skip(Sector.K(4)))
        val pio = new PatternedSeqIO(pat)
        pio.maxIOAmount = Sector.M(16)
        logger.debug("staging data")
        val st1 = table.create { s =>
          // run(s)
          pio.run(s)
          Writeboost.Status.parse(s.dm.status())
        }
        logger.debug("and then cache hit")
        val st2 = table.create { s =>
          // run(s)
          pio.run(s)
          Writeboost.Status.parse(s.dm.status())
        }
        val key = Writeboost.StatKey(false, true, false, true)
        assert(st2.stat(key) > st1.stat(key))
      }
    }
  }
  test("read cache threshold") {
    slowDevice(Sector.G(2)) { backing =>
      fastDevice(Sector.M(128)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching, Map("read_cache_threshold" -> 127)).create { s =>
          import scala.concurrent.ExecutionContext.Implicits.global

          val f1 = Future(Shell.sync(s"dd status=none if=${s.bdev.path} iflag=direct of=/dev/null bs=1M count=1000"))
          val f2 = Future(Shell.sync(s"dd status=none if=${s.bdev.path} iflag=direct of=/dev/null bs=1M skip=500 count=1000"))
          val fx = Future.sequence(Seq(f1, f2))
          val result = Await.result(fx, Duration.Inf)
          assert(result.forall(_.isRight))

          val st1 = Writeboost.Status.parse(s.dm.status())
          Shell(s"dd status=none if=${s.bdev.path} iflag=direct of=/dev/null bs=1M count=1000")
          val st2 = Writeboost.Status.parse(s.dm.status())
          val key = Writeboost.StatKey(false, true, false, true)
          assert(st2.stat(key) === st1.stat(key))
        }
      }
    }
  }
  test("read cache verify data") {
    slowDevice(Sector.M(1024)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        backing.bdev.zeroFill()
        val rp = new RandomPatternVerifier(backing, Sector.K(4))
        rp.stamp(2)

        Writeboost.sweepCaches(caching)
        val table = Writeboost.Table(backing, caching, Map("read_cache_threshold" -> 127))
        table.create { s =>
          assert(rp.verify(withStack = s)) // stage all data
          Writeboost.Status.parse(s.dm.status()) // not used but for debugging
          s.dropTransient()
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
  test("doesn't cache because of the threshold") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        val table = Writeboost.Table(backing, caching, Map("read_cache_threshold" -> 1))

        import PatternedSeqIO._
        val pat = Seq(Read(Sector.K(8)), Skip(Sector.K(4)))
        val pio = new PatternedSeqIO(pat)
        pio.maxIOAmount = Sector.M(16)
        table.create { s =>
          pio.run(s)
          Writeboost.Status.parse(s.dm.status())
        }
        val st = table.create { s =>
          pio.run(s)
          Writeboost.Status.parse(s.dm.status())
        }
        assert(st.stat(Writeboost.StatKey(false, true, false, true)) === 0)
      }
    }
  }
  test("doesn't cache because the read isn't fullsized") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        val table = Writeboost.Table(backing, caching, Map("read_cache_threshold" -> 127))

        import PatternedSeqIO._
        val pat = Seq(Read(Sector.K(3)), Skip(Sector.K(5)))
        val pio = new PatternedSeqIO(pat)
        pio.maxIOAmount = Sector.M(16)
        table.create { s =>
          pio.run(s)
          Writeboost.Status.parse(s.dm.status())
        }
        val st = table.create { s =>
          pio.run(s)
          Writeboost.Status.parse(s.dm.status())
        }
        assert(st.stat(Writeboost.StatKey(false, true, false, true)) === 0) // full read hit
        assert(st.stat(Writeboost.StatKey(false, true, false, false)) === 0) // partial read hit
      }
    }
  }
}
