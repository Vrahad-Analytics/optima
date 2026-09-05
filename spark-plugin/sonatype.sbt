
import xerial.sbt.Sonatype._

ThisBuild / sonatypeCredentialHost := "central.sonatype.com"

ThisBuild / sonatypeTimeoutMillis := 600000 // 10 minutes

sonatypeProfileName := "io.telemetria"

ThisBuild / sonatypeProfileName := "io.telemetria"

ThisBuild / publishMavenStyle := true

ThisBuild / licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

ThisBuild / sonatypeProjectHosting := Some(GitHubHosting("Vrahad-Analytics", "optima", "ardb40@gmail.com"))

ThisBuild / description := "Optima by Telemetria - open source performance monitoring for Apache Spark"

ThisBuild / homepage := Some(url("https://github.com/Vrahad-Analytics/optima"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/Vrahad-Analytics/optima"),
    "scm:git@github.com:Vrahad-Analytics/optima.git"
  )
)
ThisBuild / developers := List(
    Developer(
    id = "vrahad-analytics",
    name = "Vrahad Analytics",
    email = "ardb40@gmail.com",
    url = url("https://github.com/Vrahad-Analytics")
  )
)
