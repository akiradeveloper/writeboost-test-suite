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
          slow.reload(Flakey.Table(_slow, 1, 1)) // should use _slow
          fast.reload(Flakey.Table(_fast, 1, 1))
          // error detected from backing device
          logger.info("reading")
          intercept[Exception] {
            while (true) {
              Shell(s"dd status=none if=${s.bdev.path} iflag=direct of=/dev/null")
            }
          }
          // no room in rambuf so timeout
          logger.info("writing")
          Shell.sync(s"timeout 10s dd if=/dev/urandom of=${s.bdev.path} oflag=direct bs=4k count=10000")
        } // can be removed (not blocked up)
      }}
    }}
  }
}
