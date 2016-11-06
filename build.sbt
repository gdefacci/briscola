organization  in ThisBuild  := "com.github.gdefacci"
version       in ThisBuild  := "0.1.0-SNAPSHOT"
scalaVersion  in ThisBuild  := "2.11.8"

lazy val sharedSettings = Defaults.defaultSettings ++ Seq(
		version := "0.1.0",
		scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    parallelExecution in Test := false,
    testFrameworks += new TestFramework("com.github.gdefacci.bdd.runner.Framework")

	)
  
lazy val ddd = Project("ddd",  file("ddd"))
  .settings(sharedSettings ++ Seq(
    description := "Core ddd abstractions",
    libraryDependencies += scalazCore,
    libraryDependencies ++= rx
    // libraryDependencies ++= scalaCheck
  ))

lazy val briscola = Project("ddd-briscola",  file("ddd-briscola"))
  .settings(sharedSettings ++ Seq(
    description := "Briscola game implementation",
    libraryDependencies += scalazCore,
    libraryDependencies ++= bdd,
    libraryDependencies ++= rx,
    libraryDependencies ++= macroDi
  )).dependsOn(ddd)

lazy val web = Project("ddd-briscola-web",  file("ddd-briscola-web"))
  .settings(sharedSettings ++ Seq(
    description := "Briscola web frontend",
    libraryDependencies += servletApi,
    libraryDependencies += websocketJsr,
    libraryDependencies += scalazCore,
    libraryDependencies ++= bdd,
    libraryDependencies += argonaut,
    libraryDependencies += sgrafamento,
    libraryDependencies ++= raz,
    libraryDependencies ++= logging,
    libraryDependencies ++= rx,
    libraryDependencies ++= http4s,
    //libraryDependencies += javaJsoLd,
    //libraryDependencies ++= slf4j,
    libraryDependencies ++= jettyWebSocket,
    //libraryDependencies += scalaTest % "test",
    libraryDependencies += scalajhttp % "test"
  )).dependsOn(briscola)
  
