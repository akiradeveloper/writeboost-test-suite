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
          val st1 = Writeboost.Status.parse(s.dm.status)
          val rp = new RandomPatternVerifier(s, Sector.K(31))
          rp.stamp(20)
          assert(rp.verify())
          val st2 = Writeboost.Status.parse(s.dm.status)
          val key = Writeboost.StatKey(false, true, true, true)
          assert(st2.stat(key) > st1.stat(key))
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
          // Shell(s"fio --name=test --time_based --runtime=${30 <> 3} --filename=${s.bdev.path} --rw=write:3584 --ioengine=libaio --direct=1 --bs=512")
          val pat = Seq(PatternedSeqIO.Write(Sector(1)), PatternedSeqIO.Skip(Sector(7)))
          val pio = new PatternedSeqIO(pat)
          pio.maxRuntime = (30 <> 3) * 1000 // ms
          pio.run(s)
        }
      }
    }
  }
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
}
