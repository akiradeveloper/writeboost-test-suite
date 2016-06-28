package dmtest

import org.scalatest.Suites

class SelfTest extends Suites(
  new MiscTest,
  new StackTest,
  new writeboost.PureTest,
  new PoolTest,
  new DMStateTest,
  new FileSystemTest,
  new RandomPatternTest)
