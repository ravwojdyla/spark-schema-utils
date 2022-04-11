import sbt._
import Keys._

val sparkVersion = "3.2.1"
val scalatestVersion = "3.2.11"

lazy val commonSettings = Def.settings(
  organization := "io.github.ravwojdyla",
  headerLicense := Some(HeaderLicense.ALv2("2022", "Rafal Wojdyla")),
  scalaVersion := "2.12.15",
  crossScalaVersions := Seq(scalaVersion.value),
  scalacOptions ++= Seq("-target:jvm-1.8",
                        "-deprecation",
                        "-feature",
                        "-unchecked"
                        ),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url("https://github.com/ravwojdyla/spark-schema-utils")),
  resolvers += Resolver.sonatypeRepo("public"),
  sonatypeProfileName := "io.github.ravwojdyla",
  sonatypeCredentialHost := "s01.oss.sonatype.org",
  sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
)


lazy val root: Project = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "spark-schema-utils",
    description := "spark-schema-utils",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion,
      "org.slf4j" % "slf4j-simple" % "1.7.36",
      "org.scalatest" %% "scalatest" % scalatestVersion % Test,
      "org.apache.spark" %% "spark-core" % sparkVersion % Test classifier "tests",
      "org.apache.spark" %% "spark-sql" % sparkVersion % Test classifier "tests",
      "org.apache.spark" %% "spark-catalyst" % sparkVersion % Test classifier "tests"
    )
  )

