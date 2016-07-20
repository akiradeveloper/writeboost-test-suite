package dmtest.writeboost

import dmtest._
import dmtest.fs.EXT4
import dmtest.stack._

class REPRO_111 extends DMTestSuite {
  // not reproduced yet
  ignore("luks on top of writeboost") {
    slowDevice(Sector.G(10)) { backing =>
      fastDevice(Sector.G(1)) { caching =>
        Writeboost.sweepCaches(caching)
        val options = Map(
          "read_cache_threshold" -> 1
        )
        Writeboost.Table(backing, caching, options).create { wb =>
          Luks.format(wb)
          Luks(wb) { s =>
            EXT4.format(s)
            Shell(s"fsck.ext4 -fn ${s.bdev.path}")
            Shell(s"fsck.ext4 -fn ${s.bdev.path}")
//            EXT4.Mount(s) { mp =>
//            }
//            EXT4.Mount(s) { mp =>
//            }
          }
        }
      }
    }
  }
  ignore("pattern verifier") {
    slowDevice(Sector.M(128)) { backing =>
      fastDevice(Sector.M(16)) { caching =>
        Writeboost.sweepCaches(caching)
        val options = Map(
          "read_cache_threshold" -> 1
        )
        Writeboost.Table(backing, caching, options).create  { wb =>
          Luks.format(wb)
          Luks(wb) { s =>
            val ps = new RandomPatternVerifier(s, Sector.K(4))
            ps.stamp(5)
            assert(ps.verify())
            assert(ps.verify())
          }
        }
      }
    }
  }
  test("encrypted backing") {
    slowDevice(Sector.G(1)) { backing =>
      Luks.format(backing)
      Luks(backing) { luks => // open
        EXT4.format(luks)
        EXT4.Mount(luks) { mp =>
          val crf = new CreateRandomFiles(mp)
          crf.numDirectories = 100
          crf.numFiles = 300
          crf.fileSize = 1
          crf.create()
        }
        Shell(s"fsck.ext4 -fn ${luks.bdev.path}")
      } // close
      fastDevice(Sector.G(1)) { caching =>
        Writeboost.sweepCaches(caching)
        val options = Map(
          "read_cache_threshold" -> 127
        )
        Writeboost.Table(backing, caching, options).create { wb =>
          Luks(wb) { s => // open
            wb.clearStats()

            val k11 = Writeboost.StatKey(false, false, false, false)
            val k12 = Writeboost.StatKey(false, false, false, true)

            val readBackingBefore = wb.status.stat(k11) + wb.status.stat(k12)
            Shell(s"fsck.ext4 -fn ${s.bdev.path}")
            val readBackingAfter = wb.status.stat(k11) + wb.status.stat(k12)
            assert(readBackingAfter > readBackingBefore)
            assert(wb.status.stat(Writeboost.StatKey(false, true, false, true)) === 0)

            val k21 = Writeboost.StatKey(false, true, false, false)
            val k22 = Writeboost.StatKey(false, true, false, true)

            val readCachedBefore = wb.status.stat(k21) + wb.status.stat(k22)
            Shell(s"fsck.ext4 -fn ${s.bdev.path}")
            val readCachedAfter = wb.status.stat(k21) + wb.status.stat(k22)
            assert(readCachedAfter > readCachedBefore)
          } // close
        }
      }
    }
  }
}
