package dmtest

import java.nio.file.{StandardOpenOption, Files}

import scala.util.Random

object PatternedSeqIO {
  trait Pattern { def len: Sector }
  case class Write(len: Sector) extends Pattern
  case class Read(len: Sector) extends Pattern
  case class Skip(len: Sector) extends Pattern
}
class PatternedSeqIO(pat: Seq[PatternedSeqIO.Pattern]) {
  import PatternedSeqIO._

  var startingOffset = Sector(0)
  var maxRuntime = 0L
  var maxIOAmount = Sector(0)

  val patLen: Sector = pat.map(_.len).foldLeft(Sector(0))(_ + _)

  def run(s: Stack): Unit = {
    val devSize = s.bdev.size
    var cursor = startingOffset
    def writtenAmount = cursor - startingOffset
    val deadline = System.currentTimeMillis() + maxRuntime
    def shouldQuit: Boolean = {
      if (maxRuntime > 0L && System.currentTimeMillis() > deadline) {
        logger.warn("runtime exceeded")
        return true
      }
      if (maxIOAmount > Sector(0) && writtenAmount + patLen > maxIOAmount) {
        logger.warn("max IO amount exceeded")
        return true
      }
      if (writtenAmount + patLen > devSize)
        return true

      return false
    }
    while (!shouldQuit) {
      pat.foreach { _ match {
        case Write(len) =>
          val buf = DataBuffer.random(len.toB.toInt)
          s.bdev.write(cursor, buf)
          cursor += len
        case Read(len) =>
          val buf = s.bdev.read(cursor, len)
          cursor += len
        case Skip(len) =>
          cursor += len
      }}
    }
  }
}
