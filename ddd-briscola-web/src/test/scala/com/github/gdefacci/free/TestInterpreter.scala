package com.github.gdefacci.free

import org.obl.raz.Path
import argonaut.JsonParser
import rx.lang.scala.Observable
import scalaz.{ -\/ , \/ ,  \/- }
import scalaz.Free
import com.github.gdefacci.bdd.{TestResult, Ok, Fail}
import scala.util.Try
import scala.util.Failure
import scala.util.Success

class TestInterpreter(webSocket:Path => Throwable \/ Observable[String]) extends InterpreterFunction {

  def apply[T](step: ClientStep.Free[T]): Try[T] = step.resume.fold({
    
    case Check(f, next) =>
      val r = f()
      r match {
        case Ok => apply(next)
        case Fail(msg) => Failure(new RuntimeException(msg))
      }

    case HTTPCall(method, pth, body, next) =>
      val resp = httpCall(method, pth, body)
      resp match {
        case -\/(err) => Failure(new RuntimeException(err))
        case \/-(v) => apply(next(v))
      }

    case Parse(text, decoder, next) =>
      (for {
        bdy <- JsonParser.parse(text).right
        r <- decoder.decodeJson(bdy).toEither.right
      } yield r).fold(
         err => Failure(new RuntimeException(s"error parsing json, error: $err \n$text")),
         v => apply(next(v))
      )

    case WebSocket(uri, next) =>
      Path.fromJavaUri(new java.net.URI(uri)).map { uri =>
        webSocket(uri) match {
          case -\/(err) => Failure(new RuntimeException(s"error opening websocket at '$uri'' error: ${err.getMessage}", err))
          case \/-(v) => apply(next(ObservableHolder(v)))
        } 
      }.getOrElse( Failure(new RuntimeException(s"error parsing '$uri''")) ) 

  }, t  => Success(t))

  private def httpCall(method: HTTPMethod, pth: String, body: Option[String]): Throwable \/ scalaj.http.HttpResponse[String] = {
    \/.fromTryCatchNonFatal { 
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