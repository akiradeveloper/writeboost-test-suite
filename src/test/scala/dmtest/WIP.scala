package dmtest

import stack._
import fs._

class WIP extends DMTestSuite {
  test("flakey (read)") {
    slowDevice(Sector.M(16)) { a =>
      Flakey.Table(a, 0, 1).create { b =>
        // read
//        intercept[Exception] {
//          while (true) {
//            Shell(s"dd status=none if=${b.bdev.path} iflag=direct of=/dev/null bs=512 count=1")
//          }
//        }
      }
    }
  }
  test("flakey reloaded") {
    slowDevice(Sector.M(16)) { a => Linear.Table(a).create { b =>
        Linear.Table(b).create { c =>
          b.reload(Flakey.Table(a, 0, 1))
          // write
          intercept[Exception] {
            while (true) {
              Shell(s"dd status=none if=/dev/urandom of=${c.bdev.path} oflag=direct bs=512 count=1")
            }
          }
          // read
//          intercept[Exception] {
//            while (true) {
//              Shell(s"dd status=none if=${c.bdev.path} iflag=direct of=/dev/null bs=512 count=1")
//            }
//          }
        }
      }
    }
  }
}

