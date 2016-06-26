package dmtest.stack

import java.nio.file.{Paths, Path}

import dmtest.{Sector, Shell, RandName, Stack}

object Memory {

}
case class Memory(size: Sector) extends Stack {
  private val name = RandName.alloc
  private val dirPath = s"/tmp/${name}"
  private val filePath = s"${dirPath}/inmem"

  Shell(s"mkdir -p ${dirPath}")
  Shell(s"mount -t tmpfs size=${512 * size.unwrap} ${name} ${dirPath}")

  Shell(s"dd if=/dev/zero of=${filePath} bs=512 count=${size}")
  private val loopDevice = Paths.get(Shell("losetup -f"))
  Shell(s"losetup ${loopDevice} ${filePath}")

  override protected def path: Path = loopDevice
  override protected def terminate(): Unit = {
    Shell(s"losetup -d ${loopDevice}")
    Shell(s"rm ${filePath}")
    Shell(s"umount ${dirPath}")
    Shell(s"rm -rf ${dirPath}")
  }
}
