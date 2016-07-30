package dmtest.writeboost

import dmtest._
import dmtest.stack._

class REPRO_116 extends DMTestSuite {
  test("read should be failed when merging is needed and reading from caching device failed") {
    slowDevice(Sector.M(128)) { _slow => Linear.Table(_slow).create { slow =>
      fastDevice(Sector.M(32)) { _fast => Linear.Table(_fast).create { fast =>
        Writeboost.sweepCaches(fast)
        Writeboost.Table(slow, fast).create { s =>
          s.bdev.write(Sector(0), DataBuffer.random(Sector(1).toB.toInt))
          s.dropTransient()
          // the data is cached

          fast.reload(Flakey.Table(_fast, 0, 1))
          intercept[Exception] {
            s.bdev.read(Sector(0), Sector(3))
          }
        }
      }}
    }}
  }
  test("read should be failed when merging is needed and reading from backing device failed") {
    slowDevice(Sector.M(128)) { _slow => Linear.Table(_slow).create { slow =>
      fastDevice(Sector.M(32)) { _fast => Linear.Table(_fast).create { fast =>
        Writeboost.sweepCaches(fast)
        Writeboost.Table(slow, fast).create { s =>
          s.bdev.write(Sector(0), DataBuffer.random(Sector(1).toB.toInt))
          s.dropTransient()
          // the data is cached

          slow.reload(Flakey.Table(_slow, 0, 1))
          intercept[Exception] {
            s.bdev.read(Sector(0), Sector(3))
          }
        }
      }}
    }}
  }
}
