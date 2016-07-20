package dmtest

import dmtest.stack._

class FlakeyTest extends DMTestSuite {
  test("write error") {
    Memory(Sector.M(16)) { backing =>
      // uptime=3sec, downtime=1sec
      Flakey.Table(backing, 3, 1).create { s =>
        intercept[Exception] {
          while (true) {
            Shell(s"dd status=none if=/dev/urandom of=${s.bdev.path} oflag=direct bs=512 count=1")
          }
        }
      }
    }
  }
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
