package dmtest

import dmtest.stack._

class FlakeyTest extends DMTestSuite {
  test("flakey (read)") {
    Memory(Sector.M(16)) { a =>
      Flakey.Table(a, 0, 1).create { b =>
        intercept[Exception] {
          Shell(s"dd status=none if=${b.bdev.path} iflag=direct of=/dev/null")
        }
      }
    }
  }
}
