package dmtest

import dmtest.stack._
import org.scalatest._

class StackTest extends DMTestSuite {
  test("linear") {
    Memory(Sector.K(32)) { s =>
      stack.Linear.Table(s, Sector(0), Sector.K(10)).create { s2 =>
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
  test("suite pool allocate") {
    slowDevice(Sector.M(16 <> 4)) { s1 =>
      fastDevice(Sector.M(2 <> 1)) { s2 =>
        assert(s1.exists)
        assert(s2.exists)
      }
    }
  }
}
