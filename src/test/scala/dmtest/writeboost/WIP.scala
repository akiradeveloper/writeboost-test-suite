package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.fs._
import scala.util.Random

class WIP extends DMTestSuite {
  test("partial IO works") {
    slowDevice(Sector.M(128)) { backing =>
      fastDevice(Sector.M(4)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val st1 = s.status
          s.bdev.write(Sector(0), DataBuffer.random(Sector(1).toB.toInt))
          val st2 = s.status
          val key1 = Writeboost.StatKey(true, false, false, false)
          assert(st2.stat(key1) > st1.stat(key1))

          s.bdev.read(Sector(8), Sector(1))
          val st3 = s.status
          val key2 = Writeboost.StatKey(false, false, false, false)
          assert(st3.stat(key2) > st2.stat(key2))
        }
      }
    }
  }
}
