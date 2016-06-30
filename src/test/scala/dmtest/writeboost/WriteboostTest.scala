package dmtest.writeboost

import org.scalatest.Suites

class WriteboostTest extends Suites(
  new StackTest,
  new LogicTest,
  new ScenarioTest,
  new PerfTest,
  new REPRO_111
)
