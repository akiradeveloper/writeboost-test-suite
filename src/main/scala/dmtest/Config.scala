package dmtest

import java.nio.file.{Files, Paths, Path}

object Config {
  val ROOT_CONFIG_PATH = Paths.get(System.getProperty("user.home")).resolve(".dmtest.json")
  def rootConfig: Option[Config] = {
    if (Files.exists(ROOT_CONFIG_PATH)) {
      None // tmp
    } else {
      None
    }
  }
  case class Config(slowDevice: Path, fastDevice: Path)
}
