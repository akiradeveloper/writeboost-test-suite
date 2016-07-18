package dmtest.writeboost

import dmtest._
import dmtest.stack._

class REPRO_118 extends DMTestSuite {
  // TODO enable this once dm-flakey is fixed
  ignore("don't merge when the asking region entirely exists in the caching device") {
    slowDevice(Sector.M(128)) { _slow => Linear.Table(_slow).create { slow =>
      fastDevice(Sector.M(32)) { _fast => Linear.Table(_fast).create { fast =>
        Writeboost.sweepCaches(fast)
        Writeboost.Table(slow, fast).create { s =>
          val data = DataBuffer.random(Sector(4).toB.toInt)
          s.bdev.write(Sector(1), data)
          s.dropTransient()
          // the data is cached

          // we don't merge the backing data and the cached data
          // but instead directly read the cached data
          slow.reload(Flakey.Table(_slow, 0, 1))
          assert(s.bdev.read(Sector(1), Sector(4)) isSameAs data)
        }
      }}
    }}
  }
}
