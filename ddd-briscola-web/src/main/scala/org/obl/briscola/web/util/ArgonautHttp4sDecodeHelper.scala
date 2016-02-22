package org.obl.briscola.web.util

import scalaz.{ -\/, \/, \/- }
import argonaut.DecodeJson
import org.http4s.Request
import argonaut.JsonParser
import scalaz.stream.Process
import scalaz.concurrent.Task
import org.http4s.Response
import org.http4s.dsl._
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

object ArgonautHttp4sDecodeHelper {

  lazy val logger = Logger(LoggerFactory.getLogger(getClass))

  def decode[T](content:String)(implicit dj: DecodeJson[T]):String \/ T = 
    JsonParser.parse(content).flatMap(json => dj.decodeJson(json).toDisjunction.leftMap(_._1)) 
  
  case class ParseBody[T](req: Request) {
    def apply(f:String \/ T => Task[Response])(implicit dj: DecodeJson[T]): scalaz.concurrent.Task[Response] = {
      val parser = scalaz.stream.Process.await1[String] map { content =>
        val parseRes = decode(content) //.flatMap(json => dj.decodeJson(json).toDisjunction.leftMap(_._1)) 
        logger.debug("parsing")
        logger.debug(s"$content")
        logger.debug(s"result: $parseRes")
        f( parseRes )
      }
      
      (req.bodyAsText() |> parser).runLastOr(InternalServerError("error parsing body content")).run
    }  
  }


}