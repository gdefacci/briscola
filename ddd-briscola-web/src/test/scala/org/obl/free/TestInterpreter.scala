package org.obl.free

import scala.annotation.tailrec
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import org.obl.raz.Path
import argonaut.JsonParser
import rx.lang.scala.Observable
import scalaz.{ -\/ , \/ ,  \/- }
import scalaz.Free

case class TestInterpreterConfig(secondsTimeout: Int)

sealed trait TestResult

case class Error(error: Throwable) extends TestResult
case class Assert(value: Boolean, description: String) extends TestResult

class TestInterpreter(config: TestInterpreterConfig, webSocket:Path => Throwable \/ Observable[String]) {

  def apply(step: Step.FreeStep[Any]): Seq[TestResult] = apply(step, Nil)

  private def apply(step: Step.FreeStep[Any], current: Seq[TestResult]): Seq[TestResult] = step.resume.fold({

    case Check(v, desc, next) =>
      Assert(v, desc) +: apply(next, current)

    case HTTPCall(method, pth, body, next) =>
      val resp = httpCall(method, pth, body)
      resp match {
        case Failure(err) => current :+ Error(err)
        case Success(v) => apply(next(v), current)
      }

    case Parse(text, decoder, next) =>
      val resp = JsonParser.parse(text).flatMap(decoder.decodeJson(_).toDisjunction)
      resp match {
        case -\/(err) => current :+ Error(new RuntimeException(s"error parsing json, error: $err \n$text"))
        case \/-(v) => apply(next(v), current)
      }

    case WebSocket(uri, next) =>
      Path.fromJavaUri(new java.net.URI(uri)).map { uri =>
        webSocket(uri) match {
          case -\/(err) => current :+ Error(new RuntimeException(s"error opening websocket at '$uri'' error: $err"))
          case \/-(v) => apply(next(ObservableHolder(v)), current)
        } 
      }.getOrElse( current :+ Error(new RuntimeException(s"error parsing '$uri''")) )
      
  }, _ => current)
  
  private def httpCall(method: HTTPMethod, pth: String, body: Option[String]): Try[scalaj.http.HttpResponse[String]] = {
    Try { 
      val httpCall = scalaj.http.Http(pth)
      val req = body match { 
        case None => httpCall.method(method.toString())
        case Some(data) => method match {
          case PUT => httpCall.put(data)
          case POST => httpCall.postData(data)
          case _ => throw new RuntimeException(s"body is forbidden for method $method")
        }
      }
      req.asString
    }
  }

}