package dmtest.fs

import dmtest.{Shell, Stack}

object XFS {
  def format(stack: Stack) = Shell(s"mkfs.xfs -f ${stack.bdev.path}")
  case class Mount(stack: Stack) extends MountApply {
    override def option: String = ""
  }
}
