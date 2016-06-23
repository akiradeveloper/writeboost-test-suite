package dmtest.stack

import java.nio.file.Path

import dmtest.{Stack, BlockDevice}

object Direct {
  case class S(path: Path) extends Stack {
    override def terminate: Unit = {}
  }
}
