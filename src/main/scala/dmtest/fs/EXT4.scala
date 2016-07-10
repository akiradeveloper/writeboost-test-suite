package dmtest.fs

import dmtest.{Stack, Shell}

object EXT4 {
  def format(stack: Stack) = Shell(s"mkfs.ext4 ${stack.bdev.path}")
  case class Mount(stack: Stack) extends MountApply {
    override def mountOption: String = ""
    override def checkCmd: String = "fsck.ext4 -fn"
  }
}
