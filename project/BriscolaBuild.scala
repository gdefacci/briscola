import sbt._
import Keys._

import scala.util.Properties.envOrNone

object BriscolaBuild extends Build {

  lazy val http4sVersion = "0.12.2"
  
  lazy val http4s = Seq(
    "org.http4s" %% "http4s-dsl"          % http4sVersion,
    "org.http4s" %% "http4s-servlet"      % http4sVersion,
    "org.http4s" %% "http4s-server"       % http4sVersion,
    "org.http4s" %% "http4s-jetty"        % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-argonaut"     % http4sVersion
  )
 
  lazy val scalazCore = "org.scalaz" %% "scalaz-core" % "7.1.7" 
  
  lazy val rx = Seq(
    "io.reactivex" %% "rxscala" % "0.26.0",
    "io.reactivex" % "rxjava" % "1.1.1"
  )
  
  val razVersion = "0.8.0-SNAPSHOT"
  
  lazy val raz = Seq(
    "org.obl" %% "raz" % razVersion,
    "org.obl" %% "raz-http4s" % razVersion
  )
  
  lazy val scalajhttp = "org.scalaj" %% "scalaj-http" % "2.3.0"
  
  lazy val jettyVersion = "9.3.2.v20150730"  
  
  lazy val servletApi = "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
  
  lazy val websocketJsr = "javax.websocket" % "javax.websocket-api" % "1.0" % "provided"
  
  lazy val jettyWebSocket = Seq(
    "org.eclipse.jetty" % "jetty-webapp" % jettyVersion,
    "org.eclipse.jetty.websocket" % "websocket-server" % jettyVersion,
    "org.eclipse.jetty.websocket" % "javax-websocket-server-impl" % jettyVersion
  )
  
  lazy val scalaCheck = Seq(
    "org.scalacheck" %% "scalacheck" % "1.13.0",
    "org.cvogt" %% "scalacheck-extensions" % "0.2"
  )

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "2.2.6"

  lazy val logging = {
    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
    val slf4jLog4j = "org.slf4j" % "slf4j-log4j12" % "1.7.16"
    Seq(scalaLogging, slf4jLog4j)
  }
  
  lazy val javaJsoLd = "com.github.jsonld-java" % "jsonld-java" % "0.8.2"
  
}
