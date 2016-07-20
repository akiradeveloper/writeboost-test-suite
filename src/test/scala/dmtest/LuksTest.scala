package dmtest

import dmtest.fs._
import dmtest.stack._

class LuksTest extends DMTestSuite {
  test("write then read") {
    Memory(Sector.M(16)) { s =>
      Luks.format(s)
      Luks(s) { s2 =>
        Shell(s"dd status=none if=/dev/urandom of=${s.bdev.path} bs=512 count=10") // wipe
        assert(s2.exists)
      }
      assert(s.exists)
    }
  }
  test("format before wrapping") {
    slowDevice(Sector.M(128)) { backing =>
      EXT4.format(backing)
      Luks.format(backing)
      Luks(backing) { s =>
        intercept[Exception] {
          EXT4.Mount(s) { mp => }
        }
      }
    }
  }
}
