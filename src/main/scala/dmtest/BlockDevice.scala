package dmtest

import java.nio.ByteBuffer
import java.nio.file.{Files, Path}

case class BlockDevice(path: Path) {
  def size: Sector = Sector(Shell(s"blockdev --getsize ${path}").toLong)
  def zeroFill(): Unit = Shell(s"dd if=/dev/zero of=${path} bs=512 count=${size}")
  def read(offset: Sector, len: Sector): ByteBuffer = {
    val buf = ByteBuffer.allocate(len.toB.toInt)

    val chan = Files.newByteChannel(path)
    chan.position(offset.toB)
    chan.read(buf)
    buf.flip()
    chan.close()

    buf
  }
}
