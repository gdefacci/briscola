package org.obl.free

import org.obl.raz.Path
import argonaut.JsonParser
import rx.lang.scala.Observable
import scalaz.{ -\/ , \/ ,  \/- }
import scalaz.Free

sealed trait TestResult

case class Error(error: Throwable) extends TestResult
case class Assert(value: Boolean, description: String) extends TestResult

class TestInterpreter[S](webSocket:Path => Throwable \/ Observable[String], initialState: => (List[String],S)) extends TestInterpreterFunction[S] {

  def apply(step: Step.Free[S, Any]): Seq[TestResult] = step.resume.fold({

    case Check(v, desc, next) =>
      Assert(v, desc) +: apply(next)

    case HTTPCall(method, pth, body, next) =>
      val resp = httpCall(method, pth, body)
      resp match {
        case -\/(err) => Seq(Error(err))
        case \/-(v) => apply(next(v))
      }

    case Parse(text, decoder, next) =>
      val resp = JsonParser.parse(text).flatMap(decoder.decodeJson(_).toDisjunction)
      resp match {
        case -\/(err) => Seq(Error(new RuntimeException(s"error parsing json, error: $err \n$text")))
        case \/-(v) => apply(next(v))
      }

    case WebSocket(uri, next) =>
      Path.fromJavaUri(new java.net.URI(uri)).map { uri =>
        webSocket(uri) match {
          case -\/(err) => Seq(Error(new RuntimeException(s"error opening websocket at '$uri'' error: ${err.getMessage}", err)))
          case \/-(v) => apply(next(ObservableHolder(v)))
        } 
      }.getOrElse( Seq(Error(new RuntimeException(s"error parsing '$uri''"))) )

    case GetState(next) =>
      initialState._1.map( desc => Assert(true, "Given "+desc)) ++ apply(next(initialState._2))
      
  }, _ => Nil)

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