package dmtest

import org.scalatest.Suites

class SelfTest extends Suites(
  new StackTest,
  new FileSystemTest,
  new RandomPatternTest)
