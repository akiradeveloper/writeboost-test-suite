package dmtest

import java.io.ByteArrayOutputStream
import java.nio.file.Path

import scala.sys.process._

object Shell {
  def sync(cmd: ProcessBuilder, quiet: Boolean = false): Either[Int, String] = {
    if (!quiet)
      logger.debug(s"sh> ${cmd}")

    val os = new ByteArrayOutputStream()
    val err = (cmd #> os).!
    if (err == 0) {
      Right(os.toString.trim)
    } else {
      logger.error(s"sh> ${cmd} err=${err}")
      Left(err)
    }
  }
  def apply(cmd: ProcessBuilder, quiet: Boolean = false): String = {
    sync(cmd, quiet) match {
      case Right(o) => o
    }
  }
  def at(cwd: Path, quiet: Boolean = false)(cmd: String) = apply(Process(cmd, cwd.toFile), quiet)
}

