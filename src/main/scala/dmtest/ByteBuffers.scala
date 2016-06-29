package dmtest

import java.nio.ByteBuffer

import scala.util.Random

object ByteBuffers {
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
    a.array().zip(b.array()).forall { case (x, y) => x == y }
  }
  def isZeroed(a: ByteBuffer): Boolean = {
    val len: Int = a.limit() - a.position()
    for (i <- 0 until len) {
      val b = a.get(i)
      if (b != 0) return false
    }
    true
  }
}
