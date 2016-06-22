package dmtest

import java.io.ByteArrayOutputStream

import scala.sys.process._

object Shell {
  private def runCommand(cmd: String): Either[Int, String] = {
    val os = new ByteArrayOutputStream()
    val err = (cmd #> os).!
    if (err == 0) {
      Right(os.toString)
    } else {
      // TODO (logging)
      Left(err)
    }
  }
  def run(cmd: String): String = {
    runCommand(cmd) match {
      case Right(o) => o
    }
  }
}
