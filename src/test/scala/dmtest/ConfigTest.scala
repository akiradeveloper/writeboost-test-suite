package dmtest

import java.nio.file.Paths

class ConfigTest extends DMTestSuite {
  test("read config (no file)") {
    assert(Config.readConfig(Paths.get("/tmp/hogehogehogehogehogheo")) === None)
  }
  test("read config") {
    val f = TempFile {
      """
         slow_device = aaaa
         fast_device = bbbb
      """.stripMargin
    }
    val c = Config.readConfig(f).get
    assert(c.slowDevice === Paths.get("aaaa"))
    assert(c.fastDevice === Paths.get("bbbb"))
  }
}
