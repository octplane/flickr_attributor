name := """flickr_attributor"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)


libraryDependencies ++= Seq(
 "org.scalaj" %% "scalaj-http" % "0.3.16"
)