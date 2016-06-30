package dmtest

import java.nio.file.Paths

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
  test("two rand buffers are not equal") {
    val a = ByteBuffers.mkRandomByteBuffer(1000)
    val b = ByteBuffers.mkRandomByteBuffer(1000)
    assert(!ByteBuffers.areTheSame(a, b))
  }
  test("rand buffer is not zeroed") {
    assert(!ByteBuffers.isZeroed(ByteBuffers.mkRandomByteBuffer(1000)))
  }
}
