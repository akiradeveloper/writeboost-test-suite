package dmtest.writeboost

import dmtest._
import dmtest.stack._

// dm-flakey is fixed at kernel 4.8
// so we can't put these test in the framework regression test
class FlakeyTest extends DMTestSuite {
  test("read error") {
    Memory(Sector.M(16)) { a =>
      Flakey.Table(a, 0, 1).create { b =>
        intercept[Exception] {
          Shell(s"dd status=none if=${b.bdev.path} iflag=direct of=/dev/null")
        }
      }
    }
  }
  test("linear on top of flakey reloaded") {
    slowDevice(Sector.M(16)) { a => Linear.Table(a).create { b =>
      Linear.Table(b).create { c =>
        b.reload(Flakey.Table(a, 0, 1))
        intercept[Exception] {
          Shell(s"dd status=none if=${c.bdev.path} iflag=direct of=/dev/null")
        }
      }
    }}
  }
}
