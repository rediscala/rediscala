import com.typesafe.tools.mima.core.DirectMissingMethodProblem
import com.typesafe.tools.mima.core.ProblemFilters
import com.typesafe.tools.mima.core.ReversedMissingMethodProblem
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

releaseTagName := (ThisBuild / version).value

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("publishSigned"),
  releaseStepCommandAndRemaining("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges,
)

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

val baseSourceUrl = "https://github.com/rediscala/rediscala/tree/"

def Scala213 = "2.13.16"

def scalaVersions = Seq(Scala213, "3.3.4")

lazy val commonSettings = Def.settings(
  organization := "io.github.rediscala",
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("https://github.com/rediscala/rediscala")),
  scmInfo := Some(ScmInfo(url("https://github.com/rediscala/rediscala"), "scm:git:git@github.com:rediscala/rediscala.git")),
  mimaPreviousArtifacts := Set(organization.value %% name.value % "1.17.0"),
  pomExtra := (
    <developers>
      <developer>
        <id>xuwei-k</id>
        <name>Kenji Yoshida</name>
        <url>https://github.com/xuwei-k</url>
      </developer>
    </developers>
  ),
  publishTo := sonatypePublishToBundle.value,
  publishMavenStyle := true,
)

commonSettings

lazy val standardSettings = Def.settings(
  commonSettings,
  name := "rediscala",
  Test / baseDirectory := (LocalRootProject / baseDirectory).value,
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions ++= Seq(
    "-release:8",
    "-Wunused:imports",
    "-encoding",
    "UTF-8",
    "-deprecation",
    "-feature",
    "-unchecked"
  ),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) =>
        Seq("-Xsource:3-cross")
      case _ =>
        Nil
    }
  },
  scalacOptions ++= PartialFunction
    .condOpt(CrossVersion.partialVersion(scalaVersion.value)) { case Some((2, _)) =>
      Seq("-Xlint")
    }
    .toList
    .flatten,
  Compile / doc / scalacOptions ++= {
    Seq(
      "-sourcepath",
      (LocalRootProject / baseDirectory).value.getAbsolutePath
    )
  },
  autoAPIMappings := true,
  Test / fork := true,
  Compile / doc / scalacOptions ++= {
    val branch = {
      if (isSnapshot.value) sys.process.Process("git rev-parse HEAD").lineStream_!.head
      else (LocalRootProject / version).value
    }
    Seq[String](
      "-doc-source-url",
      baseSourceUrl + branch + "€{FILE_PATH}.scala"
    )
  },
  libraryDependencies ++= Seq(
    "redis.clients" % "jedis" % "5.2.0" % Test,
    "com.dimafeng" %% "testcontainers-scala" % "0.41.5" % Test,
    "org.scalatest" %% "scalatest-wordspec" % "3.2.19" % Test,
    "org.scalacheck" %% "scalacheck" % "1.18.1" % Test,
    "org.apache.pekko" %% "pekko-actor" % "1.1.3",
    "org.apache.pekko" %% "pekko-testkit" % "1.1.3" % Test,
  )
)

lazy val rediscala = projectMatrix
  .defaultAxes(VirtualAxis.jvm)
  .in(file("."))
  .settings(
    standardSettings,
    TaskKey[Unit]("runDockerTests") := Def.taskDyn {
      val dockerTests = (Test / compile).value
        .asInstanceOf[sbt.internal.inc.Analysis]
        .apis
        .internal
        .collect {
          case (className, analyzed) if analyzed.api.classApi.structure.parents.collect { case p: xsbti.api.Projection =>
                p.id
              }.exists(Set("RedisDockerServer")) =>
            className
        }
        .toList
        .sorted
      assert(dockerTests.nonEmpty)
      streams.value.log.info(dockerTests.mkString("testOnly ", ", ", ""))
      Def.task {
        (Test / testOnly).toTask(dockerTests.mkString(" ", " ", "")).value
      }
    }.value,
  )
  .jvmPlatform(
    scalaVersions = scalaVersions,
  )

standardSettings
Compile / sources := Nil
Test / sources := Nil
publish / skip := true
scalaVersion := Scala213

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
scalafixAll := {}
mimaPreviousArtifacts := Set.empty
ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % "0.6.0"
