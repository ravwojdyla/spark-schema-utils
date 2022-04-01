import sbt._
import Keys._

val sparkVersion = "3.2.1"

lazy val commonSettings = Def.settings(
  organization := "vc.related",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.15",
  scalacOptions ++= Seq("-target:jvm-1.8",
                        "-deprecation",
                        "-feature",
                        "-unchecked"
                        ),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)

lazy val root: Project = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "rs_spark_utils",
    description := "rs_spark_utils",
    publish / skip := true,
    run / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
    run / fork := true,
    libraryDependencies ++= Seq(
      "org.apache.spark" % "spark-core_2.12" % sparkVersion,
      "org.apache.spark" % "spark-sql_2.12" % sparkVersion,
      "org.slf4j" % "slf4j-simple" % "1.7.36"
    )
  )
  .enablePlugins(JavaAppPackaging)

