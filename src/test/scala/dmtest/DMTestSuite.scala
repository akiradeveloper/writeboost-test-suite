package dmtest

import java.nio.file.{Path, Paths}

import dmtest.stack.{Memory, Direct, Pool}
import org.scalatest._

trait DMTestSuite extends FunSuite with BeforeAndAfterEach with BeforeAndAfterAll {
  def isDebugMode: Boolean = Config.rootConfig.isEmpty
  implicit class Compare[A](a: A) {
    def `<>`(b: A): A = if (isDebugMode) b else a
  }

  override def withFixture(test: NoArgTest) = {
    logger.info(s"[TEST] ${test.name}")
    test()
  }

  def slowDevice(size: Sector): Stack = {
    if (isDebugMode) {
      Memory(size)
    } else {
      Pool.S(slowPool, size)
    }
  }
  def fastDevice(size: Sector): Stack = {
    if (isDebugMode) {
      Memory(size)
    } else {
      Pool.S(fastPool, size)
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

  private var _numMount: Int = 0
  private var _numTable: Int = 0

  def numMount: Int = {
    Shell("mount").split("\n").size
  }

  def numTable: Int = {
    Shell("dmsetup table").split("\n").size
  }

  override def beforeEach = {
    _numMount = numMount
    _numTable = numTable
    TempFile.mount()
  }

  override def afterEach = {
    TempFile.umount()
    if (numMount != _numMount)
      logger.error("mount inconsistent before and after test")
    if (numTable != _numTable)
      logger.error("dmsetup table inconsistent before and after test")
  }
}
