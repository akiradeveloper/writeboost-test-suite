package dmtest.fs

import java.nio.file.{Files, Path, Paths}

import dmtest.{RandName, Shell, Stack}

trait MountApply {
  def stack: Stack
  def mountOption: String
  def checkCmd: String
  private def mount: Path = {
    val mp = Paths.get(s"/tmp/${RandName.alloc}")
    Files.deleteIfExists(mp)
    Files.createDirectory(mp)
    Shell(s"mount ${mountOption} ${stack.bdev.path} ${mp}")
    mp
  }
  private def umount(path: Path) = {
    Shell(s"umount ${path}")
    Files.deleteIfExists(path)
    Shell(s"${checkCmd} ${stack.bdev.path}")
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
