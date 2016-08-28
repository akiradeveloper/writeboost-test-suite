package dmtest.writeboost

import dmtest._
import dmtest.stack._

class REPRO_144 extends DMTestSuite {
  test("log rotated and superblock record is enabled") {
    slowDevice(Sector.M(128)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          s.bdev.write(Sector(0), DataBuffer.random(Sector.M(64).toB.toInt))
          s.dropTransient()
          s.dropCaches()
          assert(s.status.lastFlushedId === s.status.lastWritebackId)
          s.dm.message("update_sb_record_interval 1")
          Thread.sleep(5000) // wait for updating the sb record
        }
        // this should not cause kernel panic
        Writeboost.Table(backing, caching).create { s =>
        }
      }
    }
  }
}
