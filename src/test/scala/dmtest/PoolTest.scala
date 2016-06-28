package dmtest

class PoolTest extends DMTestSuite {
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
      d2.purge()
      val d4 = stack.Pool.S(pool, Sector.K(8))
      assert(d4.exists)
      val d5 = stack.Pool.S(pool, Sector.K(1))
      assert(d5.exists)

      // should purge the linear devices
      // otherwise the backing loopback device can't be detached because of reference count remained.
      d1.purge()
      d3.purge()
      d4.purge()
      d5.purge()
    }
  }
}
