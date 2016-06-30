package dmtest

import java.nio.ByteBuffer

import dmtest._
import dmtest.stack._

class PatternedSeqIOTest extends DMTestSuite {
  test("pattened IO") {
    Memory(Sector.M(128)) { s =>
      import PatternedSeqIO._
      val pat = Seq(
        Write(Sector.K(1)),
        Skip(Sector.K(2)),
        Read(Sector.K(3)),
        Skip(Sector.K(4))
      )
      val pio = new PatternedSeqIO(pat)
      pio.startingOffset = Sector.K(4)
      pio.maxIOAmount = Sector.M(64)

      s.bdev.zeroFill()
      pio.run(s)

      assert(!ByteBuffers.isZeroed(s.bdev.read(Sector.K(4), Sector.K(1))))
      assert(ByteBuffers.isZeroed(s.bdev.read(Sector.K(5), Sector.K(9))))
      assert(!ByteBuffers.isZeroed(s.bdev.read(Sector.K(14), Sector.K(1))))
    }
  }
}