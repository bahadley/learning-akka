name := "akkademydb"

organization := "com.akkademy"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

lazy val akkaVersion = "2.4.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.akkademy" %% "akkademydb-messages" % "0.0.1-SNAPSHOT",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.7"
)
