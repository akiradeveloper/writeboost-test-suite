package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.fs._

class WIP extends DMTestSuite {
  test("read: no caching") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val base = DataBuffer.random(Sector(8).toB.toInt)
          backing.bdev.write(Sector(0), base)

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
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val base = DataBuffer.random(Sector(8).toB.toInt)
          backing.bdev.write(Sector(0), base)

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
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val base = DataBuffer.random(Sector(8).toB.toInt)
          backing.bdev.write(Sector(0), base)

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
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val base = DataBuffer.random(Sector(8).toB.toInt)
          backing.bdev.write(Sector(0), base)

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
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val base = DataBuffer.random(Sector(8).toB.toInt)
          backing.bdev.write(Sector(0), base)

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
  test("write: compose: cached data + write data (really compose)") {
  }
}
