package dmtest

import org.scalatest._

class StackTest extends FunSuite {
  test("loopback") {
    stack.Loopback.S(Sector.K(16)) withDev { dev =>
      assert(dev.size === Sector.K(16))
    }
  }
  test("pool") {
    val loopback = stack.Loopback.S(Sector.K(32)) withDev { dev1 =>
      val pool = new stack.Pool(stack.Direct.S(dev1.path))
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
}
