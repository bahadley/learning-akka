name := "akkademy-db-client"

organization := "com.akkademy"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

lazy val akkaVersion = "2.4.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.akkademy" %% "akkademy-db-messages" % "0.0.1-SNAPSHOT",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)
