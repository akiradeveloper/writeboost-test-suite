package dmtest

import java.io.ByteArrayOutputStream

import scala.sys.process._

object Shell {
  private def runCommand(cmd: String): Either[Int, String] = {
    logger.debug("sh ${cmd}")
    val os = new ByteArrayOutputStream()
    val err = (cmd #> os).!
    if (err == 0) {
      Right(os.toString)
    } else {
      logger.error(s"cmd err=${err}")
      Left(err)
    }
  }
  def apply(cmd: String): String = {
    runCommand(cmd) match {
      case Right(o) => o
    }
  }
}
