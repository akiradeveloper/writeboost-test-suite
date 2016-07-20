package dmtest

import org.scalatest.Suites

class SelfTest extends Suites(
  new MiscTest,
  new DMStateTest,
  new DataBufferTest,
  new ConfigTest,
  new StackTest,
  new PoolTest,
  new LuksTest,
  new FlakeyTest,
  new DMStateTest,
  new FileSystemTest,
  new RandomPatternVerifierTest,
  new PatternedSeqIOTest,
  new CreateRandomFilesTest
)
