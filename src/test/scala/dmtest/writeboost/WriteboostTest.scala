package dmtest.writeboost

import org.scalatest.Suites

class WriteboostTest extends Suites(
  new StackTest,
  new REPRO_111
)
