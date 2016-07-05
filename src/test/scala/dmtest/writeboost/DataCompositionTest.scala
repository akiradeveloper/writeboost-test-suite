package dmtest.writeboost

import dmtest.stack.Writeboost
import dmtest.{DataBuffer, Sector, DMTestSuite}
import scala.util.Random

class DataCompositionTest extends DMTestSuite {
  test("read: no writeboost") {
    slowDevice(Sector.G(1)) { backing =>
      val base = DataBuffer.random(Sector(8).toB.toInt)
      backing.bdev.write(Sector(0), base)
      assert(backing.bdev.read(Sector(0), Sector(8)) isSameAs base)
    }
  }
  test("partial IO works") {
    slowDevice(Sector.M(128)) { backing =>
      fastDevice(Sector.M(4)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val st1 = s.status
          s.bdev.write(Sector(0), DataBuffer.random(Sector(1).toB.toInt))
          val st2 = s.status
          val key1 = Writeboost.StatKey(true, false, false, false)
          assert(st2.stat(key1) > st1.stat(key1))

          s.bdev.read(Sector(8), Sector(1))
          val st3 = s.status
          val key2 = Writeboost.StatKey(false, false, false, false)
          assert(st3.stat(key2) > st2.stat(key2))
        }
      }
    }
  }
  test("read: no caching") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val st1 = s.status
          assert(s.bdev.read(Sector(0), Sector(8)) isSameAs base)
          val st2 = s.status
          val key = Writeboost.StatKey(false, false, false, true)
          assert(st2.stat(key) > st1.stat(key))
        }
      }
    }
  }
  test("read: rambuf data + backing") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val D1 = DataBuffer.random(Sector(1).toB.toInt)
          s.bdev.write(Sector(1), D1)

          // bb 11 bb bb bb bb bb bb
          val shouldRead: DataBuffer = base.overwrite(Sector(1).toB.toInt, D1)
          val st1 = s.status
          assert(s.bdev.read(Sector(0), Sector(8)) isSameAs shouldRead)
          val st2 = s.status
          val key = Writeboost.StatKey(false, true, true, true)
          assert(st2.stat(key) > st1.stat(key))
        }
      }
    }
  }
  test("read: cached data + backing") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val D1 = DataBuffer.random(Sector(1).toB.toInt)
          s.bdev.write(Sector(1), D1) // rambuf

          s.dropTransient()

          // bb 11 bb bb bb bb bb bb
          val shouldRead: DataBuffer = base.overwrite(Sector(1).toB.toInt, D1)
          val st1 = s.status
          assert(s.bdev.read(Sector(0), Sector(8)) isSameAs shouldRead)
          val st2 = s.status
          val key = Writeboost.StatKey(false, true, false, true)
          assert(st2.stat(key) > st1.stat(key))
        }
      }
    }
  }
  test("read: rambuf fully overwrite cached data + backing"){
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val D1 = DataBuffer.random(Sector(1).toB.toInt)
          s.bdev.write(offset = Sector(1), D1)

          s.dropTransient()

          val D2 = DataBuffer.random(Sector(2).toB.toInt)
          s.bdev.write(offset = Sector(0), D2)

          // 22 22 bb bb bb bb bb bb
          val shouldRead: DataBuffer = base
            .overwrite(Sector(1).toB.toInt, D1)
            .overwrite(Sector(0).toB.toInt, D2)
          val st1 = s.status
          assert(s.bdev.read(Sector(0), Sector(8)) isSameAs shouldRead)
          val st2 = s.status
          val key = Writeboost.StatKey(false, true, true, true)
          assert(st2.stat(key) > st1.stat(key))
        }
      }
    }
  }
  test("read: rambuf partially overwrites cached data + backing") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val D1 = DataBuffer.random(Sector(2).toB.toInt)
          s.bdev.write(offset = Sector(1), D1)

          s.dropTransient()

          val D2 = DataBuffer.random(Sector(1).toB.toInt)
          s.bdev.write(offset = Sector(1), D2)

          // bb 22 11 bb bb bb bb bb
          val shouldRead: DataBuffer = base
            .overwrite(Sector(1).toB.toInt, D1)
            .overwrite(Sector(1).toB.toInt, D2)
          val st1 = s.status
          assert(s.bdev.read(Sector(0), Sector(8)) isSameAs shouldRead)
          val st2 = s.status
          val key = Writeboost.StatKey(false, true, true, true)
          assert(st2.stat(key) > st1.stat(key))
        }
      }
    }
  }
  test("read: rambuf not overwrite cached data + backing") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val D1 = DataBuffer.random(Sector(1).toB.toInt)
          s.bdev.write(offset = Sector(1), D1)

          s.dropTransient()

          val D2 = DataBuffer.random(Sector(2).toB.toInt)
          s.bdev.write(offset = Sector(2), D2)

          // bb 11 22 22 bb bb bb bb
          val shouldRead: DataBuffer = base
            .overwrite(Sector(1).toB.toInt, D1)
            .overwrite(Sector(2).toB.toInt, D2)
          val st1 = s.status
          assert(s.bdev.read(Sector(0), Sector(8)) isSameAs shouldRead)
          val st2 = s.status
          val key = Writeboost.StatKey(false, true, true, true)
          assert(st2.stat(key) > st1.stat(key))
        }
      }
    }
  }
  test("read: random write and occasionally dropping rambuf") {
    trait Input
    case class Write(offset: Sector, data: DataBuffer) extends Input
    case class DropTransient() extends Input
    def compose(acc: DataBuffer, x: Input): DataBuffer = {
      x match {
        case Write(offset: Sector, data: DataBuffer) =>
          acc.overwrite(offset.toB.toInt, data)
        case DropTransient() =>
          acc
      }
    }
    val base = DataBuffer.random(Sector(8).toB.toInt)
    val inputs = (0 until 100).map { _ =>
      Random.nextInt(3) match {
        case 3 =>
          DropTransient()
        case _ =>
          val offset = Random.nextInt(8)
          val len = Random.nextInt(8 - offset) + 1
          val data = DataBuffer.random(Sector(len).toB.toInt)
          Write(Sector(offset), data)
      }
    }
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          var acc = base
          inputs.foreach { input =>
            input match {
              case Write(offset, data) =>
                s.bdev.write(offset, data)
              case DropTransient() =>
                s.dropTransient()
            }
            val read = s.bdev.read(Sector(0), Sector(8))

            acc = compose(acc, input)
            assert(read isSameAs acc)
          }
        }
      }
    }
  }
  test("write: cached data + write data (partial overwrite)") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val D1 = DataBuffer.random(Sector(3).toB.toInt)
          s.bdev.write(Sector(1), D1)
          s.dropTransient()

          val D2 = DataBuffer.random(Sector(1).toB.toInt)
          val st1 = s.status
          s.bdev.write(Sector(2), D2)
          val st2 = s.status
          val key = Writeboost.StatKey(true, true, false, false)
          assert(st2.stat(key) > st1.stat(key))

          val expected = base
            .overwrite(Sector(1).toB.toInt, D1)
            .overwrite(Sector(2).toB.toInt, D2)

          // bb 11 22 11 bb bb bb bb
          assert(s.bdev.read(Sector(0), Sector(8)) isSameAs expected)
        }
      }
    }
  }
  test("write: cached data + write data (entirely overwrite)") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          val D1 = DataBuffer.random(Sector(1).toB.toInt)
          s.bdev.write(Sector(2), D1)
          s.dropTransient()

          val D2 = DataBuffer.random(Sector(3).toB.toInt)
          val st1 = s.status
          s.bdev.write(Sector(1), D2)
          val st2 = s.status
          val key = Writeboost.StatKey(true, true, false, false)
          assert(st2.stat(key) > st1.stat(key))

          val expected = base
            .overwrite(Sector(2).toB.toInt, D1)
            .overwrite(Sector(1).toB.toInt, D2)

          // bb 22 22 22 bb bb bb bb
          assert(s.bdev.read(Sector(0), Sector(8)) isSameAs expected)
        }
      }
    }
  }
  test("write: random write on rambuffer") {
    slowDevice(Sector.G(1)) { backing =>
      fastDevice(Sector.M(32)) { caching =>
        val base = DataBuffer.random(Sector(8).toB.toInt)
        backing.bdev.write(Sector(0), base)

        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          var expected = base

          val startId = s.status.currentId
          for (_ <- 0 until 100) {
            val offset = Random.nextInt(8)
            val len = Random.nextInt(8 - offset) + 1
            val data = DataBuffer.random(Sector(len).toB.toInt)

            s.bdev.write(Sector(offset), data)
            expected = expected.overwrite(Sector(offset).toB.toInt, data)
            assert(s.bdev.read(Sector(0), Sector(8)) isSameAs expected)
          }
          assert(s.status.currentId === startId + 1)
        }
      }
    }
  }
}
