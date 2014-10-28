import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._


name := """flickr_attributor"""

version := "1.0.3"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  cache,
  ws
)


libraryDependencies += "org.scalaj" %% "scalaj-http" % "0.3.16"


maintainer in Docker := "Pierre Baillet <pierre@baillet.name>"

name in Docker := "flickr_attributor"

version in Docker <<= version

dockerBaseImage := "williamyeh/java8"

dockerRepository := Some("octplane")

dockerExposedPorts in Docker := Seq(9000)

dockerExposedVolumes in Docker := Seq("/opt/docker/logs")

bashScriptExtraDefines += """export JAVA_OPTS="-Xmx256m -Xms256m""""
