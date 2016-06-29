package dmtest

import dmtest._
import dmtest.stack._

class DMStateTest extends DMTestSuite {
  test("call all methods") {
    val sz = Sector.M(16)
    Memory(sz) { s =>
      Linear.Table(s, Sector(0), sz).create { ss =>
        ss.dm.suspend()
        ss.dm.resume()
        val table = ss.dm.table
        logger.info(s"${table}")
        val status = ss.dm.table
        logger.info(s"${status}")
      }
    }
  }
}
