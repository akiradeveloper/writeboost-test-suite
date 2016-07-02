package dmtest

import java.nio.ByteBuffer
import java.nio.file.{StandardOpenOption, Files, Path}
import scala.sys.process._

case class BlockDevice(path: Path) {
  def size: Sector = Sector(Shell(s"blockdev --getsize ${path}").toLong)
  def zeroFill(): Unit = Shell(s"dd if=/dev/zero of=${path} bs=512 count=${size}")
  def read(offset: Sector, len: Sector): DataBuffer = {
    stack.Memory(len) { s =>
      Shell(s"dd status=none bs=512 if=${path} iflag=direct skip=${offset.unwrap} of=${s.bdev.path} count=${len.unwrap}")
      val buf = Array.ofDim[Byte](len.toB.toInt)
      val chan = Files.newByteChannel(s.bdev.path, StandardOpenOption.READ)
      try {
        chan.position(0)
        chan.read(ByteBuffer.wrap(buf))
      } finally {
        chan.close()
      }
      DataBuffer(buf)
    }
  }
  def write(offset: Sector, buf: DataBuffer): Unit = {
    val len = Sector(buf.size / 512)
    stack.Memory(len) { s =>
      val chan = Files.newByteChannel(s.bdev.path, StandardOpenOption.WRITE)
      try {
        chan.position(0)
        assert(chan.write(buf.refByteBuffer) == buf.size)
      } finally {
        chan.close()
      }
      Shell(s"dd status=none bs=512 if=${s.bdev.path} of=${path} oflag=direct seek=${offset.unwrap} count=${len.unwrap}")
    }
  }
}
