package dmtest

import java.nio.ByteBuffer
import java.nio.file.{StandardOpenOption, Files, Path}

case class BlockDevice(path: Path) {
  def size: Sector = Sector(Shell(s"blockdev --getsize ${path}").toLong)
  def zeroFill(): Unit = Shell(s"dd if=/dev/zero of=${path} bs=512 count=${size}")
  def read(offset: Sector, len: Sector): DataBuffer = {
    val buf = Array.ofDim[Byte](len.toB.toInt)

    val chan = Files.newByteChannel(path, StandardOpenOption.READ)
    try {
      chan.position(offset.toB)
      chan.read(ByteBuffer.wrap(buf))
    } finally {
      chan.close()
    }

    DataBuffer(buf)
  }
  def write(offset: Sector, buf: DataBuffer): Unit = {
    val chan = Files.newByteChannel(path, StandardOpenOption.WRITE)
    try {
      chan.position(offset.toB)
      assert(chan.write(buf.refByteBuffer) == buf.size)
    } finally {
      chan.close()
    }
  }
}
