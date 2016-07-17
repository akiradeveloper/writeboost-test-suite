package dmtest.writeboost

import dmtest.DMTestSuite

import dmtest._
import dmtest.stack._

class FaultInjectionTest extends DMTestSuite {
  // TODO enable this one dm-flakey is fixed
  ignore("pattern verifier with flakey backing and caching") {
    slowDevice(Sector.M(128)) { _slow => Linear.Table(_slow).create { slow =>
      fastDevice(Sector.M(32)) { _fast => Linear.Table(_fast).create { fast =>
        Writeboost.sweepCaches(fast)
        Writeboost.Table(slow, fast).create { s =>
          slow.reload(Flakey.Table(_slow, 1, 1)) // should use _slow
          fast.reload(Flakey.Table(_fast, 1, 1))
          logger.info(s"s.table=${s.table}")
          logger.info(s"slow.table=${slow.dm.table}")
          logger.info(s"fast.table=${fast.dm.table}")
          // error detected from backing device
          logger.info("reading")
          intercept[Exception] {
            while (true) {
              // val st1 = s.status
              Shell(s"dd status=none if=${s.bdev.path} iflag=direct of=/dev/null count=1")
              // val st2 = s.status
              // val key = Writeboost.StatKey(false, false, false, false) // read from backing
              // assert(st2.stat(key) > st1.stat(key))
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
