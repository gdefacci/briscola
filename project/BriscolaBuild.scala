import sbt._
import Keys._

import scala.util.Properties.envOrNone

object BriscolaBuild extends Build {

  lazy val scalazCore          = "org.scalaz"               %% "scalaz-core"             % "7.1.3"
  
  lazy val http4sVersion = "0.10.0"
  
  lazy val http4s = Seq(
   "org.http4s" %% "http4s-dsl"          % http4sVersion,
   "org.http4s" %% "http4s-servlet"      % http4sVersion,
   "org.http4s" %% "http4s-server"       % http4sVersion,
   "org.http4s" %% "http4s-jetty"        % http4sVersion,
   "org.http4s" %% "http4s-blaze-server" % http4sVersion,
   "org.http4s" %% "http4s-argonaut"     % http4sVersion
  )
  
  lazy val rx = Seq(
    "io.reactivex" %% "rxscala" % "0.25.0",
    "io.reactivex" % "rxjava" % "1.0.14"
  )
  
  val razVersion = "0.7-SNAPSHOT"
  lazy val raz = Seq(
    "org.obl" %% "raz" % razVersion,
    "org.obl" %% "raz-http4s" % razVersion
  )
  
  lazy val slf4j = Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5"
  )
    
}
