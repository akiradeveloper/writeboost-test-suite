package dmtest

import java.nio.file.{StandardOpenOption, Files}
import java.util.concurrent.atomic.AtomicBoolean

import scala.util.Random

class RandomPatternVerifier(stack: Stack, blockSize: Sector) {
  case class DeltaBlock(offset: Sector) extends Ordered[DeltaBlock] {
    val data = DataBuffer.random(blockSize.toB.toInt)
    def matchBytes(buf: DataBuffer): Boolean = data.isSameAs(buf)
    override def compare(that: DeltaBlock): Int = (this.offset.unwrap - that.offset.unwrap).toInt
  }
  private val maxBlocks = stack.bdev.size.unwrap / blockSize.unwrap
  private val delta = scala.collection.mutable.ArrayBuffer[DeltaBlock]()
  def stamp(percent: Int) = {
    val nblocks = maxBlocks * percent / 100 // TODO care the end?
    assert(nblocks < maxBlocks)
    def mkBlock: DeltaBlock = {
      val offset: Sector = blockSize * Random.nextInt(maxBlocks.toInt)
      DeltaBlock(offset)
    }

    logger.debug(s"stamp #${nblocks} delta blocks")
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
    blocks.par.foreach { b =>
      stack.bdev.write(b.offset, b.data)
    }
  }
  def verify(withStack: Stack = this.stack): Boolean = {
    val success = new AtomicBoolean(true)

    logger.debug(s"verify #${delta.size} delta blocks")

    delta.par.foreach { b =>
      val buf = withStack.bdev.read(b.offset, blockSize)
      if (!b.matchBytes(buf)) {
        logger.error(s"offset ${b.offset} didn't match")
        success.set(false)
      }
    }
    success.get()
  }
}
