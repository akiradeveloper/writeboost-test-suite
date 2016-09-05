package dmtest.fs

import dmtest.{Shell, Stack}

object XFS {
  def format(stack: Stack) = Shell(s"mkfs.xfs -f -m crc=1 ${stack.bdev.path}")
  case class Mount(stack: Stack) extends MountApply {
    override def mountOption: String = ""
    override def checkCmd: String = "xfs_repair -n"
  }
}
