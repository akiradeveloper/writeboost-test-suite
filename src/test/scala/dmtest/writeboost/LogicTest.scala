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
        val st1 = table.create { s =>
          // run(s)
          pio.run(s)
          Writeboost.Status.parse(s.dm.status())
        }
        val st2 = table.create { s =>
          // run(s)
          pio.run(s)
          Writeboost.Status.parse(s.dm.status())
        }
        val key = Writeboost.StatKey(false, true, false, true)
        assert(st2.stat(key) === st1.stat(key))
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
  test("read: no writeboost") {
    slowDevice(Sector.G(1)) { backing =>
      val base = DataBuffer.random(Sector(8).toB.toInt)
      backing.bdev.write(Sector(0), base)
      assert(backing.bdev.read(Sector(0), Sector(8)) isSameAs base)
    }
  }
  test("read: no caching") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val st1 = s.status
          assert(s.bdev.read(Sector(0), Sector(8)) isSameAs base)
          val st2 = s.status
          val key = Writeboost.StatKey(false, false, false, true)
          assert(st2.stat(key) > st1.stat(key))
        }
      }
    }
  }
  test("read: compose: rambuf data + backing") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val D1 = DataBuffer.random(Sector(1).toB.toInt)
          s.bdev.write(Sector(1), D1)

          // bb 11 bb bb bb bb bb bb
          val shouldRead: DataBuffer = base.overwrite(Sector(1).toB.toInt, D1)
          val st1 = s.status
          assert(s.bdev.read(Sector(0), Sector(8)) isSameAs shouldRead)
          val st2 = s.status
          val key = Writeboost.StatKey(false, true, true, true)
          assert(st2.stat(key) > st1.stat(key))
        }
      }
    }
  }
  test("read: compose: cached data + backing") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val D1 = DataBuffer.random(Sector(1).toB.toInt)
          s.bdev.write(Sector(1), D1) // rambuf

          s.dropTransient()

          // bb 11 bb bb bb bb bb bb
          val shouldRead: DataBuffer = base.overwrite(Sector(1).toB.toInt, D1)
          val st1 = s.status
          assert(s.bdev.read(Sector(0), Sector(8)) isSameAs shouldRead)
          val st2 = s.status
          val key = Writeboost.StatKey(false, true, false, true)
          assert(st2.stat(key) > st1.stat(key))
        }
      }
    }
  }
  test("read: compose: rambuf overwrite cached data + backing"){
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val D1 = DataBuffer.random(Sector(1).toB.toInt)
          s.bdev.write(offset = Sector(1), D1)

          s.dropTransient()

          val D2 = DataBuffer.random(Sector(2).toB.toInt)
          s.bdev.write(offset = Sector(0), D2)

          // 22 22 bb bb bb bb bb bb
          val shouldRead: DataBuffer = base
            .overwrite(Sector(1).toB.toInt, D1)
            .overwrite(Sector(0).toB.toInt, D2)
          val st1 = s.status
          assert(s.bdev.read(Sector(0), Sector(8)) isSameAs shouldRead)
          val st2 = s.status
          val key = Writeboost.StatKey(false, true, true, true)
          assert(st2.stat(key) > st1.stat(key))
        }
      }
    }
  }
  test("read: compose: rambuf not overwrite cached data + backing"){
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val D1 = DataBuffer.random(Sector(1).toB.toInt)
          s.bdev.write(offset = Sector(1), D1)

          s.dropTransient()

          val D2 = DataBuffer.random(Sector(2).toB.toInt)
          s.bdev.write(offset = Sector(2), D2)

          // bb 11 22 22 bb bb bb bb
          val shouldRead: DataBuffer = base
            .overwrite(Sector(1).toB.toInt, D1)
            .overwrite(Sector(2).toB.toInt, D2)
          val st1 = s.status
          assert(s.bdev.read(Sector(0), Sector(8)) isSameAs shouldRead)
          val st2 = s.status
          val key = Writeboost.StatKey(false, true, true, true)
          assert(st2.stat(key) > st1.stat(key))
        }
      }
    }
  }
}
