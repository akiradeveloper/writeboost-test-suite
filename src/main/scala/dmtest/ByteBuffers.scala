package dmtest

import java.nio.ByteBuffer

import scala.util.Random

object ByteBuffers {
  def mkZeroedByteBuffer(len: Int): ByteBuffer = {
    val d = Array.fill[Byte](len){0}

    val data = ByteBuffer.allocate(len)
    data.put(d)
    data.flip()
    data
  }
  def mkRandomByteBuffer(len: Int): ByteBuffer = {
    val d = Array.ofDim[Byte](len)
    Random.nextBytes(d)

    val data = ByteBuffer.allocate(len)
    data.put(d)
    data.flip()
    data
  }
  // byte by byte comparison
  def areTheSame(a: ByteBuffer, b: ByteBuffer): Boolean = {
    assert(a.remaining() == b.remaining())
    val len: Int = a.remaining()
    for (i <- 0 until len) {
      if (a.get(i) != b.get(i)) return false
    }
    return true
  }
  def isZeroed(a: ByteBuffer): Boolean = {
    val len: Int = a.limit() - a.position()
    areTheSame(a, mkZeroedByteBuffer(len))
  }
}
