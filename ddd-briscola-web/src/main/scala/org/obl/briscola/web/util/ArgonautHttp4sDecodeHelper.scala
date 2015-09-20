package org.obl.briscola.web.util

import scalaz.{ -\/, \/, \/- }
import argonaut.DecodeJson
import org.http4s.Request
import argonaut.JsonParser
import scalaz.stream.Process
import scalaz.concurrent.Task
import org.http4s.Response
import org.http4s.dsl._

object ArgonautHttp4sDecodeHelper {

  case class ParseBody[T](req: Request) {
    def apply(f:String \/ T => Task[Response])(implicit dj: DecodeJson[T]): scalaz.concurrent.Task[Response] = {
      val parser = scalaz.stream.Process.await1[String] map { content =>
        f( JsonParser.parse(content).flatMap(json => dj.decodeJson(json).toDisjunction.leftMap(_._1)) )
      }
      
      (req.bodyAsText() |> parser).runLastOr(InternalServerError("error parsing body content")).run
    }  
  }


}