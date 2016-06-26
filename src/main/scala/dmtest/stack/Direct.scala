package dmtest.stack

import java.nio.file.Path

import dmtest.Stack

object Direct {

}
case class Direct(path: Path) extends Stack {
  override def terminate: Unit = {} // this stack doesn't need to lock
}
