package dmtest

import org.scalatest.Suites

class SelfTest extends Suites(
  new MiscTest,
  new ConfigTest,
  new StackTest,
  new PoolTest,
  new DMStateTest,
  new FileSystemTest,
  new RandomPatternVerifierTest,
  new PatternedSeqIOTest,
  new writeboost.PureTest
)
