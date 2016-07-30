package dmtest.stack

import java.nio.file.{Paths, Path}

import dmtest._

object Memory {

}
case class Memory(size: Sector) extends Stack {
  private val filePath = TempFile.alloc()

  Shell(s"dd status=none if=/dev/zero of=${filePath} bs=512 count=${size}", quiet=true)
  private val loopDevice = Paths.get(Shell("losetup -f"))
  Shell(s"losetup ${loopDevice} ${filePath}", quiet=true)

  override protected def path: Path = loopDevice
  override protected def terminate(): Unit = {
    Shell(s"losetup -d ${loopDevice}", quiet=true)
    Shell(s"rm ${filePath}", quiet=true)
  }
}
