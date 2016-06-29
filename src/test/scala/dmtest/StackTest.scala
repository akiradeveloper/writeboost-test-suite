package dmtest

import dmtest.stack.{Writeboost, Memory}
import org.scalatest._

class StackTest extends DMTestSuite {
  test("loopback") {
    stack.Loopback(Sector.K(16)) { stack =>
      assert(stack.bdev.size === Sector.K(16))
    }
  }
  test("linear") {
    stack.Loopback(Sector.K(32)) { s =>
      stack.Linear.Table(s, Sector(0), Sector.K(10)).create { s2 =>
        assert(s2.exists)
      }
      assert(s.exists)
    }
  }
  test("luks") {
    stack.Loopback(Sector.M(16)) { s =>
      stack.Luks(s) { s2 =>
        Shell(s"dd if=/dev/urandom of=${s.bdev.path} bs=512 count=10") // wipe
        assert(s2.exists)
      }
      assert(s.exists)
    }
  }
  test("memory") {
    Memory(Sector.M(1)) { s =>
      assert(s.exists)
    }
  }
  test("memory (nesting)") {
    Memory(Sector.M(1)) { s1 =>
      Memory(Sector.K(16)) { s2 =>
        assert(s1.exists)
        assert(s2.exists)
        assert(s1.bdev.path != s2.bdev.path)
      }
      assert(s1.exists)
    }
  }
  test("self nesting") {
    Memory(Sector.K(1)) { s1 =>
      s1 { s2 =>
        assert(s2.exists)
      }
      assert(s1.exists)
    }
  }
// FIXME (fails to umount at around 500th)
//  test("stack (not leaking)") {
//    (0 until 100000).foreach { _ => // 100GB in total
//      Memory(Sector.M(1)) { _ => }
//    }
//  }
  test("suite pool allocate") {
    slowDevice(Sector.M(16 <> 4)) { s1 =>
      fastDevice(Sector.M(2 <> 1)) { s2 =>
        assert(s1.exists)
        assert(s2.exists)
      }
    }
  }
}
