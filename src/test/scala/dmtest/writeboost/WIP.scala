package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.fs._

class WIP extends DMTestSuite {
  test("write: compose: cached data + write data (partial overwrite)") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val D1 = DataBuffer.random(Sector(3).toB.toInt)
          s.bdev.write(Sector(1), D1)
          s.dropTransient()

          val D2 = DataBuffer.random(Sector(1).toB.toInt)
          s.bdev.write(Sector(2), D2)

          val expected = base
            .overwrite(Sector(1).toB.toInt, D1)
            .overwrite(Sector(2).toB.toInt, D2)

          // bb 11 22 11 bb bb bb bb
          val st1 = s.status
          assert(s.bdev.read(Sector(0), Sector(8)) isSameAs expected)
          val st2 = s.status
          val key = Writeboost.StatKey(true, true, false, true)
          assert(st2.stat(key) > st1.stat(key))
        }
      }
    }
  }
  test("write: compose: cached data + write data (entirely overwrite)") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val D1 = DataBuffer.random(Sector(1).toB.toInt)
          s.bdev.write(Sector(2), D1)
          s.dropTransient()

          val D2 = DataBuffer.random(Sector(3).toB.toInt)
          s.bdev.write(Sector(1), D2)

          val expected = base
            .overwrite(Sector(2).toB.toInt, D1)
            .overwrite(Sector(1).toB.toInt, D2)

          // bb 22 22 22 bb bb bb bb
          val st1 = s.status
          assert(s.bdev.read(Sector(0), Sector(8)) isSameAs expected)
          val st2 = s.status
          val key = Writeboost.StatKey(true, true, false, true)
          assert(st2.stat(key) > st1.stat(key))
        }
      }
    }
  }
}
