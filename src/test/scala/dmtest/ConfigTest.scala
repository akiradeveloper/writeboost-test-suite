package dmtest

import java.nio.file.{Files, Paths}

class ConfigTest extends DMTestSuite {
  test("read config (no file)") {
    assert(Config.readConfig(Paths.get("/tmp/hogehogehogehogehogheo")) === None)
  }
  test("read config") {
    val f = TempFile.text {
      """
         slow_device = aaaa
         fast_device = bbbb
      """.stripMargin
    }
    val c = try {
      Config.readConfig(f).get
    } finally {
      Files.deleteIfExists(f)
    }
    assert(c.slowDevice === Paths.get("aaaa"))
    assert(c.fastDevice === Paths.get("bbbb"))
  }
}
