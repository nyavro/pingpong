import sbt.Keys._

name := "pingpong"

version := "1.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.10" % "2.3.7",
  "com.typesafe.akka" % "akka-persistence-experimental_2.10" % "2.3.7",
  "com.typesafe.akka" % "akka-testkit_2.10" % "2.3.7" % "test",
  "junit" % "junit" % "4.10" % "test",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
)