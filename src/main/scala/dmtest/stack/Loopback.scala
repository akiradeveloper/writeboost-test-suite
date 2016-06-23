package dmtest.stack

import java.nio.file.{Paths, Files, Path}

import dmtest._

object Loopback {
  private def attach(size: Sector): (Path, Path) = {
    val emptyLoopDevice = Paths.get(Shell("losetup -f"))
    val tmpFile = Paths.get(s"/tmp/${RandNameAllocator.alloc}")
    Shell(s"dd if=/dev/zero of=${tmpFile} bs=512 count=${size}")
    Shell(s"losetup ${emptyLoopDevice} ${tmpFile}")
    (emptyLoopDevice, tmpFile)
  }
  private def detach(path: Path) = {
    Shell(s"losetup -d ${path}")
  }
  def allocator(size: Sector): S = {
    new S(size)
  }
  case class S(size: Sector) extends Stack {
    val (path, filePath) = attach(size)
    override def terminate = {
      detach(path)
      Files.delete(filePath)
    }
  }
}
