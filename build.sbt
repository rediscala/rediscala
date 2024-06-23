import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

releaseTagName := (ThisBuild / version).value

val pekko = ActorLibCross("-pekko", "-pekko")
val akka = ActorLibCross("-akka", "-akka")

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

def scalaVersions = Seq("2.12.19", "2.13.14", "3.3.3")

lazy val commonSettings = Def.settings(
  organization := "io.github.rediscala",
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("https://github.com/rediscala/rediscala")),
  scmInfo := Some(ScmInfo(url("https://github.com/rediscala/rediscala"), "scm:git:git@github.com:rediscala/rediscala.git")),
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
    "-encoding",
    "UTF-8",
    "-deprecation",
    "-feature",
    "-unchecked"
  ),
  scalacOptions ++= PartialFunction
    .condOpt(CrossVersion.partialVersion(scalaVersion.value)) { case Some((2, _)) =>
      Seq("-Xlint", "-Xsource:3")
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
  version := {
    if (baseDirectory.value == (LocalRootProject / baseDirectory).value) {
      version.value
    } else {
      val v = version.value
      val snapshotSuffix = "-SNAPSHOT"
      val axes = virtualAxes.?.value.getOrElse(Nil)
      val suffix = (axes.contains(pekko), axes.contains(akka)) match {
        case (true, false) =>
          "-pekko"
        case (false, true) =>
          "-akka"
        case _ =>
          sys.error(axes.toString)
      }
      if (v.endsWith(snapshotSuffix)) {
        v.dropRight(snapshotSuffix.length) + suffix + snapshotSuffix
      } else {
        v + suffix
      }
    }
  },
)

lazy val rediscala = projectMatrix
  .defaultAxes(VirtualAxis.jvm)
  .in(file("."))
  .settings(
    standardSettings,
    libraryDependencies ++= {
      if (scalaBinaryVersion.value == "2.12") {
        Seq(
          "org.scala-lang.modules" %% "scala-collection-compat" % "2.12.0" % Test,
        )
      } else {
        Nil
      }
    },
    libraryDependencies ++= Seq(
      "com.dimafeng" %% "testcontainers-scala" % "0.41.4" % Test,
      "org.scalatest" %% "scalatest-wordspec" % "3.2.19" % Test,
      "org.scalacheck" %% "scalacheck" % "1.18.0" % Test,
    )
  )
  .jvmPlatform(
    scalaVersions = scalaVersions,
    axisValues = Seq(pekko),
    settings = Def.settings(
      Compile / unmanagedResourceDirectories += {
        (Compile / scalaSource).value.getParentFile / "resources-pekko"
      },
      libraryDependencies ++= Seq(
        "org.apache.pekko" %% "pekko-actor" % "1.0.2",
        "org.apache.pekko" %% "pekko-testkit" % "1.0.2" % Test,
      )
    ),
  )
  .jvmPlatform(
    scalaVersions = scalaVersions,
    axisValues = Seq(akka),
    settings = Def.settings(
      Compile / unmanagedResourceDirectories += {
        (Compile / scalaSource).value.getParentFile / "resources-akka"
      },
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor" % "2.6.21",
        "com.typesafe.akka" %% "akka-testkit" % "2.6.21" % Test,
      )
    ),
  )

Compile / sources := Nil
Test / sources := Nil
publish / skip := true
