package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.fs._

class WIP extends DMTestSuite {
  test("read: compose partial rambuf data + backing (overlap)") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val base = DataBuffer.random(Sector(8).toB.toInt)
          backing.bdev.write(Sector(0), base)

          val partial = DataBuffer.random(Sector(1).toB.toInt)
          s.bdev.write(Sector(1), partial) // rambuf

          // b = backing
          // x = rambuf
          // bb xx bb bb bb bb bb bb
          val shouldRead = base.overwrite(Sector(1).toB.toInt, partial)
          assert(s.bdev.read(Sector(0), Sector(8)) === shouldRead)
        }
      }
    }
  }
  test("read: compose partial cachee data + backing") {
  }
  test("write: compose partial cached data + write data (really compose)") {
  }
  test("write: compose partial cached data + write data (newer data wins all)") {
  }
}
