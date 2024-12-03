val scala3Version = "3.5.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "SudokuSolver_Scala",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "1.0.3" % Test,
    libraryDependencies += ("org.scala-graph" %% "graph-core" % "2.0.2").cross(CrossVersion.for3Use2_13),
    libraryDependencies += ("org.scala-graph" %% "graph-dot" % "2.0.0").cross(CrossVersion.for3Use2_13),

    scalacOptions ++= Seq("-new-syntax", "-deprecation")
  )
