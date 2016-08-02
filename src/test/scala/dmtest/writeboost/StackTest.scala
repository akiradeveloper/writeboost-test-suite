package dmtest.writeboost

import dmtest.DMTestSuite
import dmtest.stack.Writeboost

import dmtest._
import dmtest.stack._

class StackTest extends DMTestSuite {
  test("just stack") {
    slowDevice(Sector.M(16)) { backing =>
      fastDevice(Sector.M(4)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          assert(s.exists)
        }
      }
    }
  }
  test("discard isn't supported") {
    slowDevice(Sector.M(16)) { backing =>
      fastDevice(Sector.M(4)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val res = Shell.sync(s"blkdiscard --offset=0 --length=4096 ${s.bdev.path}")
          assert(res.isLeft)
        }
      }
    }
  }
  test("tunable is changed through message") {
    slowDevice(Sector.M(16)) { backing =>
      fastDevice(Sector.M(4)) { caching =>
        Writeboost.sweepCaches(caching)
        val key = "read_cache_threshold"
        Writeboost.Table(backing, caching, Map(key -> 4)).create { s =>
          assert(s.status.tunables(key) === 4)
          s.dm.message(s"${key} 32")
          assert(s.status.tunables(key) === 32)
        }
      }
    }
  }
}
