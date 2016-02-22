package org.obl.briscola.web

import org.http4s._
import org.http4s.dsl._
import org.http4s.server._
import scalaz.concurrent.Task
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

object Static {

  lazy val log = Logger(LoggerFactory.getLogger(getClass))
  
  lazy val plan = HttpService {
    case req @ GET -> path =>
      // captures everything after "/static" into `path`
      // Try http://localhost:8080/http4s/static/nasa_blackhole_image.jpg
      // See also org.http4s.server.staticcontent to create a mountable service for static content
      val pth = s"src/main/webapp$path"
      log.debug("serving resource")
      log.debug(pth)
      StaticFile.fromString(pth, Some(req)).fold(NotFound())(Task.now)
    
  } 
}