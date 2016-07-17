package dmtest.writeboost

import org.scalatest.Suites

class WriteboostTest extends Suites(
  new PureTest,
  new StackTest,
  new LogicTest,
  new DataCompositionTest,
  new ReadCachingTest,
  // new FaultInjectionTest,
  new ScenarioTest,
  new REPRO_111,
  new REPRO_115
)
