package dmtest

import java.io.ByteArrayOutputStream
import java.nio.file.Path

import scala.sys.process._

object Shell {
  def sync(cmd: ProcessBuilder): Either[Int, String] = {
    logger.debug(s"sh> ${cmd}")
    val os = new ByteArrayOutputStream()
    val err = (cmd #> os).!
    if (err == 0) {
      Right(os.toString.trim)
    } else {
      logger.error(s"sh> err: ${err}")
      Left(err)
    }
  }
  def apply(cmd: ProcessBuilder): String = {
    sync(cmd) match {
      case Right(o) => o
    }
  }
  def at(cwd: Path)(cmd: String) = apply(Process(cmd, cwd.toFile))
}

