package dmtest.writeboost

import dmtest.DMTestSuite

import dmtest._
import dmtest.stack._

class FaultInjectionTest extends DMTestSuite {
  test("pattern verifier with flakey backing and caching") {
    slowDevice(Sector.G(1)) { _slow => Linear.Table(_slow).create { slow =>
      fastDevice(Sector.M(32)) { _fast => Linear.Table(_fast).create { fast =>
        Writeboost.sweepCaches(fast)
        Writeboost.Table(slow, fast).create { s =>
          slow.reload(Flakey.Table(_slow, 1, 1)) // should use _slow
          fast.reload(Flakey.Table(_fast, 1, 1))
          Shell(s"fio --name=test --filename=${s.bdev.path} --rw=randrw --bs=4k --runtime=30s")
        } // can be removed (not blocked up)
      }}
    }}
  }
}
