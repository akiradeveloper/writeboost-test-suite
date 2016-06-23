package dmtest

import java.nio.file.Path

object FileSystem {
  case class XFS(stack: Stack) {
    def mkfs = s"mkfs.xfs ${stack.dev}"
    def mount: Path = ???
    def apply[A](f: Path => A): A = {
      val mp = mount
      val res = f(mp)
      res
    }
  }
}
