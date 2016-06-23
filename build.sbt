name := "dmtest"

version := "0.0.1"

scalaVersion := "2.11.8"

// Each test suite can be run in parallel
parallelExecution in Test := false

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test",
  "ch.qos.logback" %  "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"
)
