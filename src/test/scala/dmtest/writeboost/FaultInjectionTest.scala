package dmtest.writeboost

import dmtest.DMTestSuite

import dmtest._
import dmtest.stack._

class FaultInjectionTest extends DMTestSuite {
  // TODO enable this once dm-flakey is fixed
  ignore("pattern verifier with flakey backing and caching") {
    slowDevice(Sector.M(128)) { _slow => Linear.Table(_slow).create { slow =>
      fastDevice(Sector.M(32)) { _fast => Linear.Table(_fast).create { fast =>
        Writeboost.sweepCaches(fast)
        Writeboost.Table(slow, fast).create { s =>
          slow.reload(Flakey.Table(_slow, 0, 1)) // should use _slow
          fast.reload(Flakey.Table(_fast, 0, 1))

          // error detected from backing device
          logger.info("reading")
          val st1 = s.status
          intercept[Exception] {
            s.bdev.read(Sector(0), Sector(7))
          }
          val st2 = s.status
          val key = Writeboost.StatKey(false, false, false, false) // partial read from backing
          assert(st2.stat(key) > st1.stat(key))

          // no room in rambuf so timeout
          logger.info("writing")
          Shell.sync(s"timeout 10s dd if=/dev/urandom of=${s.bdev.path} oflag=direct bs=4k count=10000")
        } // can be removed (not blocked up)
      }}
    }}
  }

  // TODO enable this once dm-flakey is fixed
  ignore("partially overwriting a cached block leads to merging. write should fail immediately if the read fails") {
    slowDevice(Sector.M(128)) { _slow => Linear.Table(_slow).create { slow =>
      fastDevice(Sector.M(32)) { _fast => Linear.Table(_fast).create { fast =>
        Writeboost.sweepCaches(fast)
        Writeboost.Table(slow, fast).create { s =>
          s.bdev.write(Sector(0), DataBuffer.random(Sector(1).toB.toInt))
          s.dropTransient()
          // the data is cached

          fast.reload(Flakey.Table(_fast, 0, 1))
          intercept[Exception] {
            s.bdev.write(Sector(1), DataBuffer.random(Sector(2).toB.toInt))
          }
        }
      }}
    }}
  }
}
