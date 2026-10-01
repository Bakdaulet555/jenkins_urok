ThisBuild / scalaVersion := "2.13.16"
ThisBuild / organization := "edu.iot"
ThisBuild / version := "1.0.0"
lazy val root = (project in file(".")).settings(
  name := "akka-iot-service",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.6.21",
    "com.typesafe.akka" %% "akka-cluster" % "2.6.21",
    "com.typesafe.akka" %% "akka-testkit" % "2.6.21" % Test,
    "org.scalatest" %% "scalatest" % "3.2.19" % Test,
    "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.2.5"
  ),
  Compile / scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-release:11"),
  Test / fork := true,
  Test / parallelExecution := false,
  Test / javaOptions ++= Seq(
    "-Diot.profile=" + sys.props.getOrElse("iot.profile", "Standard"),
    "-Ddemo.fail=" + sys.props.getOrElse("demo.fail", "false"),
    "-Dakka.test.timefactor=" + sys.props.getOrElse("akka.test.timefactor", "2")
  ),
  Compile / mainClass := Some("iot.Main"),
  assembly / mainClass := Some("iot.Main"),
  assembly / assemblyJarName := "akka-iot-service.jar",
  assembly / assemblyMergeStrategy := {
    case "reference.conf" => MergeStrategy.concat
    case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case _ => MergeStrategy.first
  }
)
