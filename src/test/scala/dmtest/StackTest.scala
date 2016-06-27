package dmtest

import dmtest.stack.{Writeboost, Memory}
import org.scalatest._

class StackTest extends DMTestSuite {
  test("loopback") {
    stack.Loopback(Sector.K(16)) { stack =>
      assert(stack.bdev.size === Sector.K(16))
    }
  }
  test("pool (algorithm)") {
    val fa = new stack.Pool.FreeArea(Sector(8))
    val r0 = fa.getFreeSpace(Sector(2))
    val r1 = fa.getFreeSpace(Sector(3))
    fa.release(r1) // merge (3+3)
    fa.getFreeSpace(Sector(6))
    fa.release(r0)
    fa.getFreeSpace(Sector(1))
  }
  test("pool") {
    stack.Loopback(Sector.K(32)) { s =>
      val pool = new stack.Pool(s)
      val d1 = stack.Pool.S(pool, Sector.K(18))
      assert(d1.exists)
      val d2 = stack.Pool.S(pool, Sector.K(9))
      assert(d2.exists)
      val d3 = stack.Pool.S(pool, Sector.K(5))
      assert(d3.exists)
      d2.purge
      val d4 = stack.Pool.S(pool, Sector.K(8))
      assert(d4.exists)
      val d5 = stack.Pool.S(pool, Sector.K(1))
      assert(d5.exists)
    }
  }
  test("linear") {
    stack.Loopback(Sector.K(32)) { s =>
      EmptyStack().reload(stack.Linear.Table(s, Sector(0), Sector.K(10))) { s2 =>
        assert(s2.exists)
      }
      assert(s.exists)
    }
  }
  test("luks") {
    stack.Loopback(Sector.M(16)) { s =>
      stack.Luks(s) { s2 =>
        Shell(s"dd if=/dev/urandom of=${s.bdev.path} bs=512 count=10") // wipe
        assert(s2.exists)
      }
    }
  }
  test("memory") {
    Memory(Sector.M(1)) { s =>
      assert(s.exists)
    }
  }
  test("memory (nesting)") {
    Memory(Sector.M(1)) { s1 =>
      Memory(Sector.K(16)) {s2 =>
        assert(s1.exists)
        assert(s2.exists)
        assert(s1.bdev.path != s2.bdev.path)
      }
    }
  }
// FIXME (fails to umount at around 500th)
//  test("stack (not leaking)") {
//    (0 until 100000).foreach { _ => // 100GB in total
//      Memory(Sector.M(1)) { _ => }
//    }
//  }
  test("<> op") {
    val res = 100 <> 10
    if (isDebugMode)
      assert(res === 10)
    else
      assert(res === 100)
  }
  test("suite pool allocate") {
    slowDevice(Sector.M(16 <> 4)) { s1 =>
      fastDevice(Sector.M(2 <> 1)) { s2 =>
        assert(s1.exists)
        assert(s2.exists)
      }
    }
  }
  test("table line (writeboost)") {
    Memory(Sector.M(16)) { s =>
      val t1 = Writeboost.Table(s, s)
      logger.info(t1.line)
      val t2 = Writeboost.Table(s, s, Map("writeback_threshold" -> 50, "nr_max_batched_writeback" -> 128))
      logger.info(t2.line)
    }
  }
  test("status line parse (writeboost)") {
    var args: Array[String] = (1 to 24).toArray.map(_.toString)
    args ++= Array("10",
      "writeback_threshold", "25",
      "nr_max_batched_writeback", "26",
      "update_sb_record_interval", "27",
      "sync_data_interval", "28",
      "read_cache_threshold", "29")
    val status = DMState.Status(
      Sector(0),
      Sector(100),
      "writeboost",
      args
    )
    val res = Writeboost.Status.parse(status)
    logger.debug(s"parsed result: ${res}")
    assert(res.cursorPos === 1)
    assert(res.stat(Writeboost.StatKey(true, false, true, false)) === 18) // 8 + (1<<3 + 1<<1)
    assert(res.nrPartialFlushed === 24)
    assert(res.tunables("sync_data_interval") === 28)
  }
}
