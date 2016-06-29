package dmtest

import dmtest.stack.{Loopback, Memory}

class RandomPatternTest extends DMTestSuite {
  test("make random byte buffer") {
    val a = ByteBuffers.mkRandomByteBuffer(1024)
    assert(ByteBuffers.areTheSame(a, a))
    val b = ByteBuffers.mkRandomByteBuffer(1024)
    assert(!ByteBuffers.areTheSame(a, b))
  }
  test("stamp and verify") {
    Memory(Sector.M(16)) { s =>
      val ps = new RandomPattern(s, Sector.K(4))
      ps.stamp(5)
      assert(ps.verify())
    }
  }
  test("verify with") {
    val sz = Sector.M(128)
    Memory(sz) { s1 =>
      val rp = new RandomPattern(s1, Sector.K(4))
      rp.stamp(5)
      assert(rp.verify())
      Loopback(sz) { s2 =>
        Shell(s"dd if=${s1.bdev.path} of=${s2.bdev.path} bs=512 count=${sz.unwrap}")
        assert(rp.verify(withStack = s2))
      }
    }
  }
}
