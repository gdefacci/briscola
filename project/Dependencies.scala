import sbt._
import Keys._

import scala.util.Properties.envOrNone

object Dependencies extends Build {

  val razVersion = "0.9.0-SNAPSHOT"
  val http4sVersion ="0.14.11a"
  val bddVersion = "0.1.0-SNAPSHOT"
  
  val http4s = Seq(
    "org.http4s" %% "http4s-dsl"          % http4sVersion,
    "org.http4s" %% "http4s-servlet"      % http4sVersion,
    "org.http4s" %% "http4s-server"       % http4sVersion,
    "org.http4s" %% "http4s-jetty"        % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-argonaut"     % http4sVersion
  )
  
  // https://mvnrepository.com/artifact/io.argonaut/argonaut_2.11
  val argonaut  = "io.argonaut" %% "argonaut" % "6.2-M3"

  val macroDiVersion = "0.1.0-SNAPSHOT"
  
  val macroDi = Seq(
    "com.github.gdefacci" %% "macro-di" % macroDiVersion
  )
 
  val scalazCore = "org.scalaz" %% "scalaz-core" % "7.2.6" 
  
  val rx = Seq(
    "io.reactivex" %% "rxscala" % "0.26.0",
    "io.reactivex" % "rxjava" % "1.1.1"
  )
  
  val raz = Seq(
    "org.obl" %% "raz" % razVersion,
    "org.obl" %% "raz-http4s" % razVersion
  )
  
  val scalajhttp = "org.scalaj" %% "scalaj-http" % "2.3.0"
  
  val jettyVersion = "9.3.2.v20150730"  
  
  val servletApi = "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
  
  val websocketJsr = "javax.websocket" % "javax.websocket-api" % "1.0" % "provided"
  
  val jettyWebSocket = Seq(
    "org.eclipse.jetty" % "jetty-webapp" % jettyVersion,
    "org.eclipse.jetty.websocket" % "websocket-server" % jettyVersion,
    "org.eclipse.jetty.websocket" % "javax-websocket-server-impl" % jettyVersion
  )
  
  val scalaCheck = Seq(
    "org.scalacheck" %% "scalacheck" % "1.13.0",
    "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.1"
    //"org.cvogt" %% "scalacheck-extensions" % "0.2"
  )

  val scalaTest = "org.scalatest" %% "scalatest" % "2.2.6"

  val logging = {
    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
    val slf4jLog4j = "org.slf4j" % "slf4j-log4j12" % "1.7.16"
    Seq(scalaLogging, slf4jLog4j)
  }
  
  val bdd = Seq(
    "com.github.gdefacci.bdd" %% "core" % bddVersion, 
    "com.github.gdefacci.bdd" %% "testkit" % bddVersion,
    "com.github.gdefacci.bdd" %% "sbt-test-interface" % bddVersion % "test"
    )
    
  val sgrafamento = "com.github.gdefacci" %% "sgrafamento"  % "0.1.0-SNAPSHOT"
}
