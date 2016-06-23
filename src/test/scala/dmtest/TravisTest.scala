package dmtest

import org.scalatest.Suites

class TravisTest extends Suites(
  new StackTest,
  new FileSystemTest)
