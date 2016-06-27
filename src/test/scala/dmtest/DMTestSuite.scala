package dmtest

import java.nio.file.{Path, Paths}

import dmtest.stack.{Memory, Direct, Loopback, Pool}
import org.scalatest.{Outcome, fixture, BeforeAndAfterAll}

trait DMTestSuite extends fixture.FunSuite with BeforeAndAfterAll {
  def isDebugMode: Boolean = Config.rootConfig.isEmpty
  implicit class Compare[A](a: A) {
    def `<>`(b: A): A = if (isDebugMode) b else a
  }

  override type FixtureParam = this.type
  override def withFixture(test: OneArgTest): Outcome = {
    logger.info(s"[TEST] ${test.name}")
    test(this)
  }
//  override def withFixture(test: NoArgTest) = {
//    logger.info(s"[TEST] ${test.name}")
//    test()
//  }

  def slowDevice(size: Sector): Stack = {
    if (isDebugMode) {
      Memory(size)
    } else {
      slowPool.alloc(size)
    }
  }
  def fastDevice(size: Sector): Stack = {
    if (isDebugMode) {
      Memory(size)
    } else {
      fastPool.alloc(size)
    }
  }

  private var slowPool: Pool = _
  private var fastPool: Pool = _

  override def beforeAll = {
    if (!isDebugMode) {
      val config = Config.rootConfig.get
      slowPool = Pool(Direct(config.slowDevice))
      fastPool = Pool(Direct(config.fastDevice))
    }
  }
  override def afterAll = {
  }
}
