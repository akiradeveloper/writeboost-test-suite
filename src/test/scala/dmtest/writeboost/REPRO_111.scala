package dmtest.writeboost

import dmtest._
import dmtest.fs.EXT4
import dmtest.stack._

class REPRO_111 extends DMTestSuite {
  test("luks on top of writeboost") {
    slowDevice(Sector.G(12)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        val options = Map(
          "writeback_threshold" -> 70,
          "sync_data_interval" -> 3600,
          "read_cache_threshold" -> 2
        )
        Writeboost.Table(backing, caching, options).create { wb =>
          Luks(wb) { s =>
            EXT4.format(s)
            EXT4.Mount(s) { mp =>
              Shell(s"dd if=/dev/zero of=${mp.resolve("a")} oflag=direct bs=512 count=10")
            }
          }
        }
      }
    }
  }
}
