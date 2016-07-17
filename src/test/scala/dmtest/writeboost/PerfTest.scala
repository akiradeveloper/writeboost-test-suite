package dmtest.writeboost

import dmtest.DMTestSuite

import dmtest._
import dmtest.stack._
import dmtest.fs._

class PerfTest extends DMTestSuite {
  test("fio read overhead") {
    slowDevice(Sector.G(4)) { backing =>
      fastDevice(Sector.M(128)) { caching =>
        Seq(1, 2, 4, 8, 16, 32, 64, 128).foreach { iosize =>
          reportTime(s"iosize=${iosize}k") {
            Writeboost.sweepCaches(caching)
            Writeboost.Table(backing, caching).create { s =>
              Shell(s"fio --name=test --filename=${s.bdev.path} --rw=randread --ioengine=libaio --direct=1 --size=${128 <> 1}m --ba=${iosize}k --bs=${iosize}k --iodepth=32")
            }
          }
        }
      }
    }
  }
  test("split overhead") {
    slowDevice(Sector.G(2)) { backing =>
      fastDevice(Sector.M(64)) { caching =>
        Seq("4K", "32K", "256K").foreach { iosize =>
          Writeboost.sweepCaches(caching)
          Writeboost.Table(backing, caching).create { s =>
            reportTime(s"iosize=${iosize}") {
              Shell(s"dd status=none if=${s.bdev.path} iflag=direct of=/dev/null bs=${iosize}")
            }
          }
        }
      }
    }
  }
  test("caching seq write (baseline for randwrite)") {
    fastDevice(Sector.M(500)) { s =>
      reportTime("") {
        Shell(s"fio --name=test --filename=${s.bdev.path} --rw=write --ioengine=libaio --size=500m --bs=256k --iodepth=32")
      }
    }
  }
  test("rand write") {
    val amount = 500 <> 1
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(amount + 100)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          reportTime("") {
            Shell(s"fio --name=test --filename=${s.bdev.path} --rw=randwrite --ioengine=lib--direct=1 --size=${amount}m --ba=4k --bs=4k --iodepth=32")
          }
        }
      }
    }
  }
  test("writeback sorting effect") {
    val amount = 128 <> 1
    slowDevice(Sector.G(2)) { backing =>
      fastDevice(Sector.M(129)) { caching =>
        Seq(4, 32, 128, 256).foreach { batchSize =>
          Writeboost.sweepCaches(caching)
          Writeboost.Table(backing, caching, Map("nr_max_batched_writeback" -> batchSize)).create { s =>
            XFS.format(s)
            XFS.Mount(s) { mp =>
              reportTime(s"batch size = ${batchSize}") {
                Shell.at(mp)(s"fio --name=test --rw=randwrite --ioengine=libaio --direct=1 --size=${amount}m --ba=4k --bs=4k --iodepth=32")
                Shell("sync")
                Kernel.dropCaches
                s.dropTransient()
                s.dropCaches()
              }
            }
          }
        }
      }
    }
  }
  test("wipe") {
    slowDevice(Sector.M(1024)) { backing =>
      fastDevice(Sector.M(1800)) { caching =>
        Seq(Sector(1), Sector.K(4), Sector.M(1)).foreach { bs =>
          Writeboost.sweepCaches(caching)
          Writeboost.Table(backing, caching, Map("writeback_threshold" -> 70)).create { s =>
            reportTime(s"bs=${bs.toB}") {
              Shell(s"dd if=/dev/zero of=${s.bdev.path} oflag=direct bs=${bs.toB} count=${s.bdev.size.toB / bs.toB}")
            }
          }
        }
      }
    }
  }
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
