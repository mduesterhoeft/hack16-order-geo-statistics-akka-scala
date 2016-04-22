name := """hack16-order-geo-statistics-akka-scala"""

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.4",
  "com.typesafe.akka" %% "akka-http-core" % "2.4.4",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.4" % "test",
  "com.typesafe.akka" %% "akka-http-testkit-experimental" % "2.4.2-RC3" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")
