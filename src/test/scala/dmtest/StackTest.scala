package dmtest

import dmtest.stack.Memory
import org.scalatest._

class StackTest extends DMTestSuite {
  test("loopback") {
    stack.Loopback(Sector.K(16)) { stack =>
      assert(stack.bdev.size === Sector.K(16))
    }
  }
  test("pool (algorithm)") {
    val fa = new stack.Pool.FreeArea(Sector(8))
    val r0 = fa.getFreeSpace(Sector(2))
    val r1 = fa.getFreeSpace(Sector(3))
    fa.release(r1) // merge (3+3)
    fa.getFreeSpace(Sector(6))
    fa.release(r0)
    fa.getFreeSpace(Sector(1))
  }
  test("pool") {
    stack.Loopback(Sector.K(32)) { s =>
      val pool = new stack.Pool(s)
      val d1 = stack.Pool.S(pool, Sector.K(18))
      assert(d1.exists)
      val d2 = stack.Pool.S(pool, Sector.K(9))
      assert(d2.exists)
      val d3 = stack.Pool.S(pool, Sector.K(5))
      assert(d3.exists)
      d2.purge
      val d4 = stack.Pool.S(pool, Sector.K(8))
      assert(d4.exists)
      val d5 = stack.Pool.S(pool, Sector.K(1))
      assert(d5.exists)
    }
  }
  test("linear") {
    stack.Loopback(Sector.K(32)) { s =>
      EmptyStack().reload(stack.Linear.Table(s, Sector(0), Sector.K(10))) { s2 =>
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
    }
  }
  test("memory") {
    Memory(Sector.M(1)) { s =>
      assert(s.exists)
    }
  }
  test("memory (nesting)") {
    Memory(Sector.M(1)) { s1 =>
      Memory(Sector.K(16)) {s2 =>
        assert(s1.exists)
        assert(s2.exists)
        assert(s1.bdev.path != s2.bdev.path)
      }
    }
  }
  test("test <> op") {
    val res = 100 <> 10
    if (isDebugMode)
      assert(res === 10)
    else
      assert(res === 100)
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
