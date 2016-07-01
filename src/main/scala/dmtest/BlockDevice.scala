package dmtest

import java.nio.ByteBuffer
import java.nio.file.{StandardOpenOption, Files, Path}

case class BlockDevice(path: Path) {
  def size: Sector = Sector(Shell(s"blockdev --getsize ${path}").toLong)
  def zeroFill(): Unit = Shell(s"dd if=/dev/zero of=${path} bs=512 count=${size}")
  def read(offset: Sector, len: Sector): ByteBuffer = {
    val buf = ByteBuffer.allocate(len.toB.toInt)

    val chan = Files.newByteChannel(path, StandardOpenOption.READ)
    chan.position(offset.toB)
    chan.read(buf)
    buf.flip()
    chan.close()

    buf
  }
  def write(offset: Sector, buf: ByteBuffer): Unit = {
    val expected = buf.remaining()
    assert(expected > 0)
    val chan = Files.newByteChannel(path, StandardOpenOption.WRITE)
    chan.position(offset.toB)
    assert(chan.write(buf) == expected)
    chan.close()
  }
}
