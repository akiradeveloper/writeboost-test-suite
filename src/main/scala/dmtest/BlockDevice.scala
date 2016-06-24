package dmtest

import java.nio.file.Path

case class BlockDevice(path: Path) {
  def size: Sector = Sector(Shell(s"blockdev --getsize ${path}").toLong)
}
