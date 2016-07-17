package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.fs._
import scala.util.Random

class WIP extends DMTestSuite {
  test("dbench") {
    slowDevice(Sector.G(2)) { backing =>
      fastDevice(Sector.M(64)) { caching =>
        Writeboost.sweepCaches(caching)
        val table = Writeboost.Table(backing, caching,
          Map("writeback_threshold" -> 70, "read_cache_threshold" -> 31)
        )
        def run(option: String): Unit = reportTime(option) {
          table.create { s =>
            EXT4.format(s)
            EXT4.Mount(s) { mp =>
              Shell.at(mp)(s"dbench ${option}")
              Shell("sync")
              Kernel.dropCaches
              s.dropCaches()
            }
          }
        }
        val t = 300 <> 1
        run(s"-t ${t} 1")
        run(s"-S -t ${t} 4") // directory operations are sync
        run(s"-s -t ${t} 4") // all operations are sync
      }
    }
  }
}
