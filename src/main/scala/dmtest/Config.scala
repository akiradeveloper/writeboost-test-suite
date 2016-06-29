package dmtest

import java.nio.file.{Files, Paths, Path}

object Config {
  def readConfig(path: Path): Option[Config] = {
    if (Files.exists(path)) {
      val prop = new java.util.Properties()
      prop.load(Files.newInputStream(path))
      val slowDevice = prop.getProperty("slow_device")
      val fastDevice = prop.getProperty("fast_device")
      Some(Config(
        slowDevice = Paths.get(slowDevice),
        fastDevice = Paths.get(fastDevice)
      ))
    } else {
      None
    }
  }
  val ROOT_CONFIG_PATH = Paths.get(System.getProperty("user.home")).resolve(".dmtest.config")
  def rootConfig: Option[Config] = readConfig(ROOT_CONFIG_PATH)
  case class Config(slowDevice: Path, fastDevice: Path)
}
