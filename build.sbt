organization in ThisBuild := "org.obl"
version      in ThisBuild := "0.1.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.11.6"

lazy val sharedSettings = Defaults.defaultSettings ++ Seq(
		version := "0.1.0",
    //scalaVersion := scalaBuildVersion,
		scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
    //crossScalaVersions := Seq(scalaBuildVersion),
    //resolvers += "m2 cental" at "http://central.maven.org/maven2/",
    //organization := "org.obl"
	)
  
lazy val ddd = Project("ddd",  file("ddd"))
  .settings(sharedSettings ++ Seq(
    description := "Core ddd abstarctions",
    libraryDependencies ++= (scalazCore +: rx)
  ))

lazy val briscola = Project("ddd-briscola",  file("ddd-briscola"))
  .settings(sharedSettings ++ Seq(
    description := "Core ddd abstarctions",
    libraryDependencies ++= (scalazCore +: rx))
  ).dependsOn(ddd)
    
/*    
lazy val web = Project("ddd-briscola-web",  file("ddd-briscola-web"))
  .settings(sharedSettings ++ Seq(
    description := "Core ddd abstarctions",
    libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
    libraryDependencies ++= (scalazCore +: (rx ++ http4s)))
  ).enablePlugins(JettyPlugin).dependsOn(briscola)
*/    