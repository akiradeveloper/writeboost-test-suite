package dmtest.writeboost

import org.scalatest.Suites

class WriteboostTest extends Suites(
  new FlakeyTest,
  new PureTest,
  new StackTest,
  new LogicTest,
  new DataCompositionTest,
  new ReadCachingTest,
  new ScenarioTest,
  new FaultInjectionTest,
  new REPRO_111,
  new REPRO_115,
  new REPRO_116,
  new REPRO_118
)
