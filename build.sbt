name := "udemy-akka-essentials"

version := "0.1"

scalaVersion := "2.12.7"

val akkaVersion = "2.5.13"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion withSources() withJavadoc(),
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion withSources() withJavadoc(),
  "org.scalatest" %% "scalatest" % "3.0.5"
)