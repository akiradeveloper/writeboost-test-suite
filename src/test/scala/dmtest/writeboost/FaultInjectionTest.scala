package dmtest.writeboost

import dmtest.DMTestSuite

import dmtest._
import dmtest.stack._

class FaultInjectionTest extends DMTestSuite {
  test("read from backing") {
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

          slow.reload(Linear.Table(_slow))
          fast.reload(Linear.Table(_fast))
        } // can be removed (not blocked up)
      }}
    }}
  }
  test("partially overwriting a cached block leads to merging. write should fail immediately if the read fails") {
    slowDevice(Sector.M(128)) { _slow => Linear.Table(_slow).create { slow =>
      fastDevice(Sector.M(32)) { _fast => Linear.Table(_fast).create { fast =>
        Writeboost.sweepCaches(fast)
        Writeboost.Table(slow, fast).create { s =>
          s.bdev.write(Sector(0), DataBuffer.random(Sector(1).toB.toInt))
          s.dropTransient()
          // the data is cached (it's not in the rambuf)

          fast.reload(Flakey.Table(_fast, 0, 1))
          intercept[Exception] {
            s.bdev.write(Sector(1), DataBuffer.random(Sector(2).toB.toInt))
          }

          fast.reload(Linear.Table(_fast))
        }
      }}
    }}
  }
}
