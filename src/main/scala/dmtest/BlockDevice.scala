package dmtest

import java.nio.ByteBuffer
import java.nio.file.{Paths, StandardOpenOption, Files, Path}
import scala.sys.process._

case class BlockDevice(path: Path) {
  def size: Sector = Sector(Shell(s"blockdev --getsize ${path}").toLong)
  def zeroFill(): Unit = Shell(s"dd status=none if=/dev/zero of=${path} bs=512 count=${size}")
  def read(offset: Sector, len: Sector): DataBuffer = {
      val tmpPath = Paths.get("/tmp/dmtest-tmp")
      Shell(s"dd status=none bs=512 if=${path} iflag=direct skip=${offset.unwrap} of=${tmpPath} count=${len.unwrap}")
      val buf = Array.ofDim[Byte](len.toB.toInt)
      val chan = Files.newByteChannel(tmpPath, StandardOpenOption.READ)
      try {
        chan.position(0)
        chan.read(ByteBuffer.wrap(buf))
      } finally {
        chan.close()
        Files.deleteIfExists(tmpPath)
      }
      DataBuffer(buf)
    }
  def write(offset: Sector, buf: DataBuffer): Unit = {
    val len = Sector(buf.size / 512)
    val tmpPath = Paths.get("/tmp/dmtest-tmp")
      val chan = Files.newByteChannel(tmpPath, StandardOpenOption.WRITE)
      try {
        chan.position(0)
        assert(chan.write(buf.refByteBuffer) == buf.size)
      } finally {
        chan.close()
        Files.deleteIfExists(tmpPath)
      }
      Shell(s"dd status=none bs=512 if=${tmpPath} of=${path} oflag=direct seek=${offset.unwrap} count=${len.unwrap}")
    }
}
