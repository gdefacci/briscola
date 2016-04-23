package org.obl.brisola.free

import java.util.concurrent.TimeUnit
import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION
import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.duration.SECONDS
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketAdapter
import org.eclipse.jetty.websocket.client.WebSocketClient
import com.ning.http.client.Response
import argonaut.JsonParser
import dispatch.Http
import dispatch.implyRequestHandlerTuple
import dispatch.url
import rx.lang.scala.Observable
import rx.lang.scala.Subject
import rx.lang.scala.subjects.PublishSubject
import scalaz.{ -\/ , \/- }
import rx.lang.scala.subjects.ReplaySubject

case class TestInterpreterConfig(secondsTimeout: Int, webSocketClient:WebSocketClient = new WebSocketClient())

sealed trait TestResult

case class Error(error: Throwable) extends TestResult
case class Assert(value: Boolean, description: String) extends TestResult

class TestInterpreter(config: TestInterpreterConfig) {

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

    case WebSocket(url, next) =>
      webSocket(url) match {
        case Failure(err) => current :+ Error(new RuntimeException(s"error opening websocket at '$url'' error: $err"))
        case Success(v) => apply(next(v), current)
      }

  }, _ => current)

  private def httpCall(method: HTTPMethod, pth: String, body: Option[String]): Try[Response] = {
    import dispatch._
    val req0 = url(pth).setMethod(method.toString).setContentType("application/json", "UTF-8")
    val req = body match {
      case None => req0
      case Some(bdy) => req0 << bdy
    }
    Await.ready(Http(req > (i => i)), Duration(config.secondsTimeout, SECONDS)).value.get
  }

  private def webSocket(url: String): Try[Observable[String]] = {
    Try {
      val uri = java.net.URI.create(url);
      if (!config.webSocketClient.isStarted()) config.webSocketClient.start();
      val subj = ReplaySubject[String]
      lazy val socket: WebSocketAdapter = new ObservableWebSocketAdapter(subj, config.webSocketClient, config.secondsTimeout)
      val sess = config.webSocketClient.connect(socket, uri).get(config.secondsTimeout, TimeUnit.SECONDS)
      assert( sess.isOpen(), s"web socket session for url $url is not open" )
      subj
    }
  }
}

class ObservableWebSocketAdapter(subj: => Subject[String], client:WebSocketClient, private var seconds:Int) extends WebSocketAdapter {
  val timer = Observable.timer(Duration(seconds, SECONDS)) 

  timer.subscribe(v => (), v => (), () => close())
  
  override def onWebSocketConnect(sess:Session) {
  }
  
  override def onWebSocketText(text: String) {
    subj.onNext(text)
  }
  def close() {
    subj.onCompleted()
    if (getSession != null) getSession.close();
  }
  
  override def onWebSocketError(cause: Throwable) {
    subj.onError(cause);
  }
  override def onWebSocketClose(statusCode: Int, reason: String) {
    close()
  }

}