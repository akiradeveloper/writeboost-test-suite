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
      val data = DataBuffer.random(sz.toB.toInt)
      s.bdev.write(offset, data)
      val read = s.bdev.read(offset, sz)
      assert(read.isSameAs(data))
    }
  }
}
