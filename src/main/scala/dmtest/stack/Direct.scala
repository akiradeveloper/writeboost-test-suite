package dmtest.stack

import java.nio.file.Path

import dmtest.Stack

object Direct {
  case class S(path: Path) extends Stack {
    override def terminate: Unit = {}
  }
}
