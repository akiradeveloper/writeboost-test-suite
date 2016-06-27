package dmtest

import dmtest.stack.Memory

class RandomPatternTest extends DMTestSuite {
  test("make random byte buffer") {
    val a = RandomPattern.mkRandomByteBuffer(1024)
    assert(RandomPattern.areTheSame(a, a))
    val b = RandomPattern.mkRandomByteBuffer(1024)
    assert(!RandomPattern.areTheSame(a, b))
  }
  test("stamp and verify") {
    Memory(Sector.M(16)) { s =>
      val ps = new RandomPattern(s, Sector.K(4))
      ps.stamp(5)
      ps.verify()
    }
  }
}
