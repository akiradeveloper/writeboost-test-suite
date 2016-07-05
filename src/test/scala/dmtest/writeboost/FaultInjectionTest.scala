package dmtest.writeboost

import dmtest.DMTestSuite

import dmtest._
import dmtest.stack._

class FaultInjectionTest extends DMTestSuite {
  test("pattern verifier with flakey backing and caching") {
    slowDevice(Sector.M(128)) { _slow => Linear.Table(_slow).create { slow =>
      fastDevice(Sector.M(32)) { _fast => Linear.Table(_fast).create { fast =>
        Writeboost.sweepCaches(fast)
        Writeboost.Table(slow, fast).create { s =>
          slow.reload(Flakey.Table(_slow, 0, 1)) // should use _slow
          fast.reload(Flakey.Table(_fast, 0, 1))
          // detect error in backing
          intercept[Exception] {
            while (true) {
              Shell(s"dd status=none if=${s.bdev.path} iflag=direct of=/dev/null")
            }
          }
          // no room in the rambuf. should timeout
          Shell.sync(s"timeout 10s dd if=/dev/urandom of=${s.bdev.path} oflag=direct bs=1m count=16")
        } // can be removed (not blocked up)
      }}
    }}
  }
}
