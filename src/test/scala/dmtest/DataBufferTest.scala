package dmtest

class DataBufferTest extends DMTestSuite {
  test("make random byte buffer") {
    val a = DataBuffer.random(1024)
    assert(a isSameAs a)
    val b = DataBuffer.random(1024)
    assert(!a.isSameAs(b))
  }
  test("two rand buffers are not equal") {
    val a = DataBuffer.random(1000)
    val b = DataBuffer.random(1000)
    assert(!a.isSameAs(b))
  }
  test("rand buffer is not zeroed") {
    assert(!DataBuffer.random(1000).isZeroed)
  }
  test("overwrite") {
    val a = DataBuffer.random(1000)
    val b = DataBuffer.random(100)
    val c = a.overwrite(30, b)
    assert(!a.isSameAs(c))
  }
}
