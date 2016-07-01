package dmtest

import java.nio.file.Paths
import dmtest._
import dmtest.stack._

class MiscTest extends DMTestSuite {
  test("<> op") {
    val res = 100 <> 10
    if (isDebugMode)
      assert(res === 10)
    else
      assert(res === 100)
  }
  test("chdir and run cmd") {
    val output = Shell.at(Paths.get("/tmp")) {
      "ls"
    }
    logger.info(output)
  }
  test("block device write then read") {
    Memory(Sector.M(10)) { s =>
      val sz = Sector(7)
      val offset = Sector(102)
      val data = ByteBuffers.mkRandomByteBuffer(sz.toB.toInt)
      s.bdev.write(offset, data)
      data.rewind()
      val read = s.bdev.read(offset, sz)
      assert(ByteBuffers.areTheSame(read, data))
    }
  }
  test("make random byte buffer") {
    val a = ByteBuffers.mkRandomByteBuffer(1024)
    assert(ByteBuffers.areTheSame(a, a))
    a.rewind()
    val b = ByteBuffers.mkRandomByteBuffer(1024)
    assert(!ByteBuffers.areTheSame(a, b))
  }
  test("two rand buffers are not equal") {
    val a = ByteBuffers.mkRandomByteBuffer(1000)
    val b = ByteBuffers.mkRandomByteBuffer(1000)
    assert(!ByteBuffers.areTheSame(a, b))
  }
  test("rand buffer is not zeroed") {
    assert(!ByteBuffers.isZeroed(ByteBuffers.mkRandomByteBuffer(1000)))
  }
}
