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
}
