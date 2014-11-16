import play.PlayScala


// TODO Replace with your project's/module's name
name := """play-angular-socket-server"""

// TODO Set your organization here
organization := "your.organization"

// TODO Set your version here
version := "2.3.1"

// Scala Version
scalaVersion := "2.10.4"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

// Dependencies
libraryDependencies ++= Seq(
  cache,
  filters,
  "com.couchbase.client" % "couchbase-client" % "1.4.3"
)

unmanagedResourceDirectories in Assets += baseDirectory.value / "bower_components"

//
// Scala Compiler Options
scalacOptions ++= Seq(
  "-target:jvm-1.7",
  "-encoding", "UTF-8",
  "-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint", // recommended additional warnings
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code"
)
