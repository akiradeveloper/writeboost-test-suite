package dmtest

import java.nio.file.{Files, Paths, Path}

object FileSystem {
  case class XFS(stack: Stack) {
    private def mkfs = s"mkfs.xfs -f ${stack.path}"
    private def mount: Path = {
      val mp = Paths.get(s"/tmp/${RandNameAllocator.alloc}")
      Files.deleteIfExists(mp)
      Files.createDirectory(mp)
      Shell(s"mount ${stack.path} ${mp}")
      mp
    }
    private def umount(path: Path) = {
      Files.deleteIfExists(path)
    }
    def apply[A](f: Path => A): A = {
      val mp = mount
      val res = f(mp)
      umount(mp)
      res
    }

    mkfs
  }
}
