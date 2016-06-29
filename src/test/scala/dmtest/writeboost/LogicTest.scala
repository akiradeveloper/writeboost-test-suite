package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.DMTestSuite

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class LogicTest extends DMTestSuite {
  test("rambuf read fullsize") {
    slowDevice(Sector.M(16)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching, Map("writeback_threshold" -> 0)).create { s =>
          s.bdev.zeroFill()
          val stat1 = Writeboost.Status.parse(s.dm.status)
          val rp = new RandomPattern(s, Sector.K(31))
          rp.stamp(20)
          assert(rp.verify())
          val stat2 = Writeboost.Status.parse(s.dm.status)
          val key = Writeboost.StatKey(false, true, true, true)
          assert(stat2.stat(key) > stat2.stat(key))
        }
      }
    }
  }
  test("invalidate prev cache") {
    val backingSize = Sector.K(4) * 1 * (128 - 1)
    val cachingSize = Sector.M(1) + (Sector.K(4) * 3 * 128) // superblock (1M) + 3 segments
    slowDevice(backingSize) { backing =>
      fastDevice(cachingSize) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching, Map("writeback_threshold" -> 0, "nr_max_batched_writeback" -> 1)).create { s =>
          Shell(s"fio --name=test --time_based --runtime=${30 <> 3} --filename=${s.bdev.path} --rw=write:3584 --ioengine=libaio --direct=1 --bs=512")
        }
      }
    }
  }
  test("no read caching") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          def run() = Shell(s"fio --name=test filename=${s.bdev.path} --io_limit=16M --rw=read:4K --bs=4K --direct=1")
          run()
          val st1 = Writeboost.Status.parse(s.dm.status())
          run()
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
        val table = Writeboost.Table(backing, caching, Map("read_cache_threshold" -> 1))
        Writeboost.sweepCaches(caching)
        def run(s: Stack) = Shell(s"fio --name=test --filename=${s.bdev.path} --io_limit=16M --rw=read:4K --bs=4K --direct=1")
        val st1 = table.create { s =>
          run(s)
          Writeboost.Status.parse(s.dm.status())
        }
        val st2 = table.create { s =>
          run(s)
          Writeboost.Status.parse(s.dm.status())
        }
        val key = Writeboost.StatKey(false, true, false, true)
        assert(st1.stat(key) === st2.stat(key))
      }
    }
  }
  test("read cache threshold") {
    slowDevice(Sector.G(2)) { backing =>
      fastDevice(Sector.M(128)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching, Map("read_cache_threshold" -> 127)).create { s =>
          import scala.concurrent.ExecutionContext.Implicits.global

          val f1 = Future(Shell.sync(s"dd if=${s.bdev.path} iflag=direct of=/dev/null bs=1M count=1000"))
          val f2 = Future(Shell.sync(s"dd if=${s.bdev.path} iflag=direct of=/dev/null bs=1M skip=500 count=1000"))
          val fx = Future.sequence(Seq(f1, f2))
          val result = Await.result(fx, Duration.Inf)
          assert(result.forall(_.isRight))

          val st1 = Writeboost.Status.parse(s.dm.status())
          Shell(s"dd if=${s.bdev.path} iflag=direct of=/dev/null bs=1M count=1000")
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
        val rp = new RandomPattern(backing, Sector.K(4))
        rp.stamp(2)

        Writeboost.sweepCaches(caching)
        val table = Writeboost.Table(backing, caching, Map("read_cache_threshold" -> 127))
        table.create { s =>
          assert(rp.verify(withStack = s)) // stage all data
        }
        table.create { s =>
          val st1 = Writeboost.Status.parse(s.dm.status())
          assert(rp.verify(withStack = s))
          val st2 = Writeboost.Status.parse(s.dm.status())
          val key = Writeboost.StatKey(false, true, false, true)
          assert(st2.stat(key) > st1.stat(key))
        }
      }
    }
  }
}
