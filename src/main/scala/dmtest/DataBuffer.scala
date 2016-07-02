package dmtest

import java.nio.ByteBuffer

import scala.util.Random

object DataBuffer {
  def allocate(n: Int): DataBuffer = {
    val d = Array.ofDim[Byte](n)
    DataBuffer(d)
  }
  def zeroed(n: Int): DataBuffer = DataBuffer(Array.fill[Byte](n){0})
  def random(n: Int): DataBuffer = {
    val d = Array.ofDim[Byte](n)
    Random.nextBytes(d)
    DataBuffer(d)
  }
}
// immutable data buffer
case class DataBuffer(unwrap: Array[Byte]) {
  def isSameAs(that: DataBuffer): Boolean = {
    if (unwrap.length != that.unwrap.length) {
      logger.debug(s"buffers not the same length (${unwrap.length} != ${that.unwrap.length})")
      return false
    }
    unwrap.zip(that.unwrap).zipWithIndex.foreach { case ((a, b), i) =>
        if (a != b) {
          logger.debug(s"buffers not the same first at index=${i} (${a} != ${b})")
          return false
        }
    }
    true
  }
  def size = unwrap.length
  def isZeroed: Boolean = isSameAs(DataBuffer.zeroed(unwrap.length))
  def overwrite(offset: Int, src: DataBuffer): DataBuffer = {
    val ret = DataBuffer(unwrap.clone())
    for (i <- 0 until src.size) {
      assert(offset + i < this.size)
      ret.unwrap.update(offset + i, src.unwrap(i))
    }
    ret
  }
  def slice(offset: Int, len: Int): DataBuffer = {
    val b = Array.ofDim[Byte](len)
    for (i <- 0 until len) {
      b.update(i, unwrap(offset + i))
    }
    DataBuffer(b)
  }
  def refByteBuffer: ByteBuffer = ByteBuffer.wrap(unwrap)
  def refArray: Array[Byte] = unwrap
}
