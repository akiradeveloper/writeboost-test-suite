package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.fs._

class REPRO_115 extends DMTestSuite {
  test("endless loop when try to use active caching device") {
    Memory(Sector.M(8)) { caching =>
      Memory(Sector.M(128)) { backing1 =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing1, caching).create { s =>
          Shell(s"dd if=/dev/urandom of=${s.bdev.path} bs=1m") // write
        }
      }
      Memory(Sector.M(64)) { backing2 =>
        Writeboost.Table(backing2, caching).create { s =>
          Shell(s"dd if=${s.bdev.path} of=/dev/null") // read
        }
      }
    }
  }
}
