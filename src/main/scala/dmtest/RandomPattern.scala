package dmtest

import java.nio.ByteBuffer
import java.nio.file.{StandardOpenOption, Files}

import scala.util.Random

object RandomPattern {
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
}
class RandomPattern(stack: Stack, blockSize: Sector) {
  import RandomPattern._
  case class DeltaBlock(offset: Sector) extends Ordered[DeltaBlock] {
    val data = mkRandomByteBuffer(blockSize.toB.toInt)
    def matchBytes(buf: ByteBuffer): Boolean = areTheSame(data, buf)
    override def compare(that: DeltaBlock): Int = (this.offset.unwrap - that.offset.unwrap).toInt
  }
  private val maxBlocks = stack.bdev.size.unwrap / blockSize.unwrap
  private val delta = scala.collection.mutable.SortedSet[DeltaBlock]()
  def stamp(percent: Int) = {
    val nblocks = maxBlocks * percent / 100 // TODO care the end?
    def mkBlock: DeltaBlock = {
      val offset: Sector = blockSize * Random.nextInt(nblocks.toInt)
      DeltaBlock(offset)
    }

    var count = nblocks
    while (count > 0) {
      val b = mkBlock
      if (!delta.contains(b)) {
        delta += b
        count -= 1
      }
    }
    writeBlocks(delta)
  }
  private def writeBlocks(blocks: Iterable[DeltaBlock]) = {
    val chan = Files.newByteChannel(stack.bdev.path, StandardOpenOption.WRITE)
    blocks.foreach { b =>
      chan.position(b.offset.toB)
      chan.write(b.data)
      b.data.rewind()
    }
    chan.close
  }
  def verify(): Boolean = {
    val chan = Files.newByteChannel(stack.bdev.path, StandardOpenOption.READ)
    var success = true
    delta.foreach { b =>
      val buf = ByteBuffer.allocate(blockSize.toB.toInt)
      chan.position(b.offset.toB)
      chan.read(buf)
      buf.flip()
      if (!b.matchBytes(buf)) {
        logger.error(s"offset ${b.offset} didn't match")
        success = false
      }
    }
    chan.close
    success
  }
}
