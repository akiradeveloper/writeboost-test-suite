package dmtest.stack

import java.nio.file.{Files, Path}

import dmtest._

object Loopback {
  // TODO should check /dev/loopN is available (use losetup -f)
  private def attach(size: Sector): (String, Path) = ???
  private def detach(path: String) = ???
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
