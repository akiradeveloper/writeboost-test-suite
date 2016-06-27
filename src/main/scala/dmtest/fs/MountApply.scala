package dmtest.fs

import java.nio.file.{Files, Path, Paths}

import dmtest.{RandName, Shell, Stack}

trait MountApply {
  def stack: Stack
  def option: String
  private def mount: Path = {
    val mp = Paths.get(s"/tmp/${RandName.alloc}")
    Files.deleteIfExists(mp)
    Files.createDirectory(mp)
    Shell(s"mount ${option} ${stack.bdev.path} ${mp}")
    mp
  }
  private def umount(path: Path) = {
    Shell(s"umount -l ${path}")
    Files.deleteIfExists(path)
  }
  def apply[A](f: Path => A): A = {
    val mp = mount
    try {
      f(mp)
    } finally {
      umount(mp)
    }
  }
}
