package dmtest

import dmtest.stack._

class FlakeyTest extends DMTestSuite {
  test("write error") {
    Memory(Sector.M(16)) { backing =>
      // uptime=3sec, downtime=1sec
      Flakey.Table(backing, 3, 1).create { s =>
        intercept[Exception] {
          while (true) {
            Shell(s"dd status=none if=/dev/urandom of=${s.bdev.path} oflag=direct bs=512 count=1", quiet=true)
          }
        }
      }
    }
  }
}
