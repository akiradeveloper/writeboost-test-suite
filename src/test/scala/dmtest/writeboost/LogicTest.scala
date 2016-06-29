package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.DMTestSuite

class LogicTest extends DMTestSuite {
  test("rambuf read fullsize") {
    slowDevice(Sector.M(16)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        EmptyStack().reload(Writeboost.Table(backing, caching, Map("writeback_threshold" -> 0))) { s =>
          s.bdev.zeroFill()
          val stat1 = Writeboost.Status.parse(s.dm.status)
          val rp = new RandomPattern(s, Sector.K(31))
          rp.stamp(20)
          assert(rp.verify)
          val stat2 = Writeboost.Status.parse(s.dm.status)
          val key = Writeboost.StatKey(false, true, true, true)
          assert(stat2.stat(key) > stat2.stat(key))
        }
      }
    }
  }
}
