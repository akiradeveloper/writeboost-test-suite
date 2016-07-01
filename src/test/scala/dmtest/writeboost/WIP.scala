package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.fs._

class WIP extends DMTestSuite {
  test("read: compose partial rambuf data + backing") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>

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
