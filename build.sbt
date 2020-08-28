val scala = "2.13.3"
val Http4sVersion = "0.21.7"
val MunitVersion = "0.7.11"
val TwitterVersion = "20.8.0"
val CirisVersion = "1.1.2"

lazy val root = (project in file("."))
  .settings(
    organization := "com.your.domain",
    name := "http4s example",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-core" % Http4sVersion,
      "org.http4s" %% "http4s-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-finagle" % s"$Http4sVersion+",
      "io.circe" %% "circe-generic" % "0.13.0",
      "is.cir" %% "ciris" % CirisVersion,
      "is.cir" %% "ciris-enumeratum" % CirisVersion,
      "com.twitter" %% "twitter-server" % TwitterVersion,
      "com.twitter" %% "twitter-server-logback-classic" % TwitterVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.scalameta" %% "munit-scalacheck" % MunitVersion % Test
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    addCompilerPlugin(
      "org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full
    ),
    addCompilerPlugin(scalafixSemanticdb),
    addCommandAlias(
      "rmUnused",
      """set scalacOptions -= "-Xfatal-warnings";scalafix RemoveUnused;set scalacOptions += "-Xfatal-warnings""""
    )
  )
