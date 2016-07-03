package dmtest

import java.nio.ByteBuffer
import java.nio.file.{StandardOpenOption, Files, Path}
import scala.sys.process._

// We use in-memory file as buffer and use dd command to submit IOs as we expect.
// SeekableByteChannel doesn't submit the device a partial IO because it does read-modify-write operation to submit small IOs.
// To test the DM device correctly the behavior isn't appropriate.

case class BlockDevice(path: Path) {
  def size: Sector = Sector(Shell(s"blockdev --getsize ${path}").toLong)
  def zeroFill(): Unit = Shell(s"dd status=none if=/dev/zero of=${path} bs=512 count=${size}")
  def read(offset: Sector, len: Sector): DataBuffer = {
    TempFile { tmp =>
      Shell(s"dd status=none bs=512 if=${path} iflag=direct skip=${offset.unwrap} of=${tmp} count=${len.unwrap}")
      val buf = Array.ofDim[Byte](len.toB.toInt)
      val chan = Files.newByteChannel(tmp, StandardOpenOption.READ)
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
    TempFile { tmp =>
      val len = Sector(buf.size / 512)
      val chan = Files.newByteChannel(tmp, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)
      try {
        chan.position(0)
        assert(chan.write(buf.refByteBuffer) == buf.size)
      } finally {
        chan.close()
      }
      Shell(s"dd status=none bs=512 if=${tmp} of=${path} oflag=direct seek=${offset.unwrap} count=${len.unwrap}")
    }
  }
}
