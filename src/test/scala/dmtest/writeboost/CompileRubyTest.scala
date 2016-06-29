package dmtest.writeboost

import dmtest._
import dmtest.stack._
import dmtest.fs._

class CompileRubyTest extends DMTestSuite {
  test("compile ruby") {
    slowDevice(Sector.G(4)) { backing =>
      fastDevice(Sector.M(128)) { caching =>
        Writeboost.sweepCaches(caching)
        EmptyStack().reload(Writeboost.Table(backing, caching)) { s =>
          XFS.format(s)
          XFS.Mount(s) { mp =>
            val rc = new CompileRuby(mp)
            rc.downloadArchive
            rc.unarchive
          }
        }
        EmptyStack().reload(Writeboost.Table(backing, caching,
          Map("writeback_threshold" -> 70, "read_cache_threshold" -> 31))) { s =>
          s.dropCaches()
          XFS.Mount(s) { mp =>
            val rc = new CompileRuby(mp)
            rc.compile
            rc.check
          }
        }
      }
    }
  }
}
