organization  in ThisBuild  := "org.obl"
version       in ThisBuild  := "0.1.0-SNAPSHOT"
scalaVersion  in ThisBuild  := "2.11.7"

lazy val sharedSettings = Defaults.defaultSettings ++ Seq(
		version := "0.1.0",
		scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    organization := "org.obl"
	)
  
lazy val ddd = Project("ddd",  file("ddd"))
  .settings(sharedSettings ++ Seq(
    description := "Core ddd abstractions",
    libraryDependencies ++= (scalazCore +: rx)
  ))

lazy val briscola = Project("ddd-briscola",  file("ddd-briscola"))
  .settings(sharedSettings ++ Seq(
    description := "Briscola game implementation",
    libraryDependencies ++= (scalazCore +: rx))
  ).dependsOn(ddd)

lazy val web = Project("ddd-briscola-web",  file("ddd-briscola-web"))
  .settings(sharedSettings ++ Seq(
    description := "Briscola web frontend",
    libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
    libraryDependencies += "javax.websocket" % "javax.websocket-api" % "1.0" % "provided",
    libraryDependencies ++= (scalazCore +: (raz ++ rx ++ http4s ++ slf4j)) ),
    libraryDependencies ++= Seq(
        "org.eclipse.jetty" % "jetty-webapp" % jettyVersion,
        "org.eclipse.jetty.websocket" % "websocket-server" % jettyVersion,
        "org.eclipse.jetty.websocket" % "javax-websocket-server-impl" % jettyVersion)
  ).dependsOn(briscola)
  
