package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.fs._
import scala.util.Random

class WIP extends DMTestSuite {
  // perf
  test("git extract") {
    slowDevice(Sector.G(8)) { backing =>
      fastDevice(Sector.M(1024)) { caching =>
        Writeboost.sweepCaches(caching)
        val table = Writeboost.Table(backing, caching, Map("writeback_threshold" -> 70))
        table.create { s =>
          EXT4.format(s)
          EXT4.Mount(s) { mp =>
            val ge = reportTime("prepare") { GitExtract(mp) }
            for (tag <- GitExtract.TAGS) {
              Kernel.dropCaches
              s.dropTransient()
              s.dropCaches()
              reportTime(s"extract ${tag}") {
                ge.extract(tag)
              }
            }
          }
        }
      }
    }
  }
}
