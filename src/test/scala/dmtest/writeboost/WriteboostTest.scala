package dmtest.writeboost

import org.scalatest.Suites

class WriteboostTest extends Suites(
  new StackTest,
  new CompileRubyTest,
  new LogicTest,
  new REPRO_111
)
