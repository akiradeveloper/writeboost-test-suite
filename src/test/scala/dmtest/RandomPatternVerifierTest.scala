package dmtest

import dmtest.stack.Memory

class RandomPatternVerifierTest extends DMTestSuite {
  test("stamp and verify (5%)") {
    Memory(Sector.M(128)) { s =>
      val ps = new RandomPatternVerifier(s, Sector.K(4))
      ps.stamp(5)
      assert(ps.verify())
    }
  }
  test("stamp and verify (20%)") {
    Memory(Sector.M(128)) { s =>
      val ps = new RandomPatternVerifier(s, Sector.K(4))
      ps.stamp(20)
      assert(ps.verify())
    }
  }
  test("verify with") {
    val sz = Sector.M(128)
    Memory(sz) { s1 =>
      val rp = new RandomPatternVerifier(s1, Sector.K(4))
      rp.stamp(5)
      assert(rp.verify())
      Memory(sz) { s2 =>
        Shell(s"dd status=none if=${s1.bdev.path} of=${s2.bdev.path} bs=512 count=${sz.unwrap}")
        assert(rp.verify(withStack = s2))
      }
    }
  }
}
