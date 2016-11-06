package com.github.gdefacci.briscola.web.util

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
import argonaut.DecodeResult

object ArgonautHttp4sDecodeHelper {

  implicit class ScalazDecodeResult[A](r:DecodeResult[A]) {
    def toDisjunction = r.toEither.fold( p => -\/(p), v => \/-(v))
  }
  
  lazy val logger = Logger(LoggerFactory.getLogger(getClass))

  def decode[T](content:String)(implicit dj: DecodeJson[T]):String \/ T = 
    \/.fromEither(JsonParser.parse(content)).flatMap(json => dj.decodeJson(json).toDisjunction.leftMap(_._1)) 
  
  case class ParseBody[T](req: Request) {
    def apply(f:String \/ T => Task[Response])(implicit dj: DecodeJson[T]): scalaz.concurrent.Task[Response] = {
      val parser = scalaz.stream.Process.await1[String] map { content =>
        val parseRes = decode(content) //.flatMap(json => dj.decodeJson(json).toDisjunction.leftMap(_._1)) 
        logger.debug("parsing")
        logger.debug(s"$content")
        logger.debug(s"result: $parseRes")
        f( parseRes )
      }
      
      (req.bodyAsText() |> parser).runLastOr(InternalServerError("error parsing body content")).unsafePerformSync
    }  
  }


}