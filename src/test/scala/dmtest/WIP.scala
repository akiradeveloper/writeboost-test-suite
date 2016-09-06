package dmtest

import stack._
import fs._

class WIP extends DMTestSuite {
  test("write-around can log rotate") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(10)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching, Map("write_around_mode" -> 1, "read_cache_threshold" -> 127)).create { s =>

        }
      }
    }
  }
}

