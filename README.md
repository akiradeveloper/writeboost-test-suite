# writeboost-test-suite

[![Build Status](https://travis-ci.org/akiradeveloper/writeboost-test-suite.svg?branch=develop)](https://travis-ci.org/akiradeveloper/writeboost-test-suite)

**writeboost-test-suite** is a complete test suite project for [dm-writeboost](https://github.com/akiradeveloper/dm-writeboost). It's written in **Scala** and we can write test codes safely thanks to the static type checkings.

To make a reproducer for the kernel module, the best way is to write a complete test code using this test suite. The first step is to copy src/test/scala/dmtest/Template.scala as src/test/scala/dmtest/XXXTest.scala. And the next step is to fill the test name and the content of the test.

```scala
class XXXTest extends DMTestSuite {
  test("test name") {
    // TODO
  }
}
```

writeboost-test-suite is fully written in Scala but if you aren't familiar with the language, you can learn from other test codes and write your own test code quickly. 

Finally, with the reproducer, please send me a pull request. Feel free to ask me when you are writing a test code.

**To run the test**

First install sbt (http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Linux.html) and then type

```
$ sudo sbt "testOnly dmtest.XXXTest"
```

If you like to set up devices (slow and fast) you can put a ".dmtest.config" at $HOME.

```
slow_device=/dev/xxx
fast_device=/dev/yyy
```


Still you can send me a reproducer as shell script or description basis but using writeboost-test-suite is the most certain way to solve the problem you are facing.
