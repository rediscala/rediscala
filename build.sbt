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
  releaseStepCommandAndRemaining("sonaRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges,
)

val baseSourceUrl = "https://github.com/rediscala/rediscala/tree/"

lazy val commonSettings = Def.settings(
  organization := "io.github.rediscala",
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("https://github.com/rediscala/rediscala")),
  scmInfo := Some(ScmInfo(url("https://github.com/rediscala/rediscala"), "scm:git:git@github.com:rediscala/rediscala.git")),
  mimaPreviousArtifacts := Set(organization.value %% name.value % "2.0.0"),
  pomExtra := (
    <developers>
      <developer>
        <id>xuwei-k</id>
        <name>Kenji Yoshida</name>
        <url>https://github.com/xuwei-k</url>
      </developer>
    </developers>
  ),
  publishTo := (if (isSnapshot.value) None else localStaging.value),
  publishMavenStyle := true,
)

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
    "redis.clients" % "jedis" % "7.0.0" % Test,
    "com.dimafeng" %% "testcontainers-scala" % "0.43.6" % Test,
    "org.scalatest" %% "scalatest-wordspec" % "3.2.19" % Test,
    "org.scalacheck" %% "scalacheck" % "1.19.0" % Test,
    "org.apache.pekko" %% "pekko-actor" % "1.2.1",
    "org.apache.pekko" %% "pekko-testkit" % "1.2.1" % Test,
  )
)

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
}.value

standardSettings
scalaVersion := "3.3.7"

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % "0.6.17"
