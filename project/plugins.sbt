addSbtPlugin("com.github.xuwei-k" % "scalafix-check" % "0.1.0")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.6")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.5")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")

addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")

if (sys.env.isDefinedAt("GITHUB_ACTION")) {
  Def.settings(
    addSbtPlugin("net.virtual-void" % "sbt-hackers-digest" % "0.1.2")
  )
} else {
  Nil
}
