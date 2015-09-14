import sbt._
import Keys._

import scala.util.Properties.envOrNone

object BriscolaBuild extends Build {

  lazy val scalazCore          = "org.scalaz"               %% "scalaz-core"             % "7.1.3"
  
  lazy val httpsVersion = "0.10.0"
  
  lazy val http4s = Seq(
   "org.http4s" %% "http4s-dsl"          % httpsVersion  ,
   "org.http4s" %% "http4s-servlet"      % httpsVersion  ,
   "org.http4s" %% "http4s-jetty"        % httpsVersion  ,
   "org.http4s" %% "http4s-argonaut"     % httpsVersion  
  )
  
  lazy val rx = Seq(
    "io.reactivex" %% "rxscala" % "0.25.0",
    "io.reactivex" % "rxjava" % "1.0.14"
  )
    
}
