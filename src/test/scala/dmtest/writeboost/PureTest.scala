package dmtest.writeboost

import dmtest._
import dmtest.stack.{Writeboost, Memory}

class PureTest extends DMTestSuite {
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
