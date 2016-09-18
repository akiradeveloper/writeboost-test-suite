package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.fs._

class REPRO_115 extends DMTestSuite {
  test("making another device while in use (backing)") {
    slowDevice(Sector.M(64)) { backing =>
      fastDevice(Sector.M(8)) { caching1 =>
        Writeboost.sweepCaches(caching1)
        Writeboost.Table(backing, caching1).create { s1 =>
          fastDevice(Sector.M(8)) { caching2 =>
            Writeboost.sweepCaches(caching2)
            intercept[Exception] {
              Writeboost.Table(backing, caching2).create { s2 =>
              }
            }
          }
        }
      }
    }
  }
  test("making another device while in use (caching)") {
    fastDevice(Sector.M(8)) { caching =>
      slowDevice(Sector.M(64)) { backing1 =>
        slowDevice(Sector.M(64)) { backing2 =>
          Writeboost.sweepCaches(caching)
          Writeboost.Table(backing1, caching).create { s =>
            intercept[Exception] {
              Writeboost.Table(backing2, caching).create { s =>
              }
            }
          }
        }
      }
    }
  }
  ignore("endless loop when try to use active caching device") {
    Memory(Sector.M(8)) { caching =>
      Memory(Sector.M(128)) { backing1 =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing1, caching).create { s =>
          Shell(s"dd if=/dev/urandom of=${s.bdev.path} bs=1M count=128") // write
        }
      }
      Memory(Sector.M(64)) { backing2 =>
        Writeboost.Table(backing2, caching).create { s =>
          Shell(s"dd if=${s.bdev.path} of=/dev/null bs=1M count=128") // read
        }
      }
    }
  }
}
