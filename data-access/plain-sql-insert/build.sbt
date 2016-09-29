name := "plain-sql-insert"

version := "0.0.1"

scalaVersion := "2.11.8"

lazy val akkaVersion = "2.4.10"
lazy val slickVersion = "3.1.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" % "slick-hikaricp_2.11" % slickVersion, 
  "com.h2database" % "h2" % "1.4.192",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.7"
)
