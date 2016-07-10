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
}
