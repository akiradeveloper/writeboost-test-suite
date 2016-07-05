package dmtest.writeboost

import org.scalatest.Suites

class WriteboostTest extends Suites(
  new PureTest,
  new StackTest,
  new LogicTest,
  new DataCompositionTest,
  new FaultInjectionTest,
  new ScenarioTest,
  new PerfTest,
  new REPRO_111
)
