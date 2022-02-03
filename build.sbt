val akkaVersion = "2.5.32"

val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion

val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion

val specs2 = "org.specs2" %% "specs2-core" % "4.8.3"

val stm = "org.scala-stm" %% "scala-stm" % "0.9.1"

val scalacheck = "org.scalacheck" %% "scalacheck" % "1.14.3"

val rediscalaDependencies = Seq(
  akkaActor,
  stm,
  akkaTestkit % "test",
  specs2 % "test",
  scalacheck % "test"
)


val baseSourceUrl = "https://github.com/etaty/rediscala/tree/"

val Scala211 = "2.11.12"
val Scala212 = "2.12.15"
val Scala213 = "2.13.8"

lazy val standardSettings = Def.settings(
  name := "rediscala",
  organization := "com.github.etaty",
  scalaVersion := Scala211,
  crossScalaVersions := Seq(Scala211, Scala212, Scala213),
  addCommandAlias("SetScala2_11", s"++ ${Scala211}! -v"),
  addCommandAlias("SetScala2_12", s"++ ${Scala212}! -v"),
  addCommandAlias("SetScala2_13", s"++ ${Scala213}! -v"),
  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("https://github.com/etaty/rediscala")),
  scmInfo := Some(ScmInfo(url("https://github.com/etaty/rediscala"), "scm:git:git@github.com:etaty/rediscala.git")),
  apiURL := Some(url("http://etaty.github.io/rediscala/latest/api/")),
  pomExtra := (
    <developers>
      <developer>
        <id>etaty</id>
        <name>Valerian Barbot</name>
        <url>http://github.com/etaty/</url>
      </developer>
    </developers>
    ),
  publishTo := sonatypePublishTo.value,
  publishMavenStyle := true,

  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-Xlint",
    "-deprecation",
    "-feature",
    "-language:postfixOps",
    "-unchecked"
  ),
  scalacOptions in (Compile, doc) ++= {
    Seq(
      "-sourcepath", (baseDirectory in LocalProject("rediscala")).value.getAbsolutePath
    )
  },
  autoAPIMappings := true,
  apiURL := Some(url("http://etaty.github.io/rediscala/")),
  scalacOptions in (Compile, doc) ++= {
    val v = (version in LocalProject("rediscala")).value
    val branch = if(v.trim.endsWith("SNAPSHOT")) "master" else v
    Seq[String](
      "-doc-source-url", baseSourceUrl + branch +"€{FILE_PATH}.scala"
    )
  },
)

lazy val root = Project(id = "rediscala",
  base = file(".")
).settings(
  standardSettings,
  libraryDependencies ++= rediscalaDependencies
)
