package com.github.gdefacci.free

import argonaut.DecodeJson
import scalaz.Functor
import scalaz.Free
import org.obl.raz.Path
import com.github.gdefacci.bdd.TestResult

sealed trait ClientStep[Next]

case class HTTPCall[N](method: HTTPMethod, url: String, body: Option[String], next: ClientStep.Response => N) extends ClientStep[N]
case class Parse[N, T](body: String, decoder: DecodeJson[T], next: T => N) extends ClientStep[N]
case class WebSocket[N](url: String, next: ObservableHolder[String] => N) extends ClientStep[N]
case class Check[N](run:() => TestResult[String], val next:N) extends ClientStep[N]

object ClientStep {

  implicit def functor[S]: Functor[({ type Type[T] = ClientStep[T] })#Type] = new Functor[({ type Type[T] = ClientStep[T] })#Type] {
    def map[A, B](step: ClientStep[A])(f: A => B): ClientStep[B] = step match {
      case HTTPCall(mthd, url, body, next) => HTTPCall(mthd, url, body, next andThen f)
      case Parse(body, decoder, next) => Parse(body, decoder, next andThen f)
      case WebSocket(url, next) => WebSocket(url, next andThen f)
      case Check(run, next) => Check(run, f(next))
    }
  }

  type Response = scalaj.http.HttpResponse[String]

  type Free[A] = scalaz.Free[({ type Type[T] = ClientStep[T] })#Type, A]

}

object ClientStepFactory {

  import ClientStep.Response

  type FreeStep[A] = ClientStep.Free[A]

  def liftF[A](value: => ClientStep[A]) = Free.liftF[ClientStep, A](value)

  def pure[A](value: => A): FreeStep[A] = Free.point[({ type Type[T] = ClientStep[T] })#Type, A](value)

  def http(method: HTTPMethod, url: String, body: Option[String]): FreeStep[Response] = liftF(HTTPCall(method, url, body, identity))
  def http(method: HTTPMethod, url: Path, body: Option[String]): FreeStep[Response] = http(method, url.render, body)

  def get(url: String): FreeStep[Response] = http(GET, url, None)
  def get(url: Path): FreeStep[Response] = get(url.render)

  def post(url: String): FreeStep[Response] = http(POST, url, None)
  def post(url: Path): FreeStep[Response] = post(url.render)

  def post(url: String, body: String): FreeStep[Response] = http(POST, url, Some(body))
  def post(url: Path, body: String): FreeStep[Response] = post(url.render, body)

  def webSocket(url: Path): FreeStep[ObservableHolder[String]] = webSocket(url.render)
  private def webSocket(url: String): FreeStep[ObservableHolder[String]] = liftF(WebSocket(url, identity))
  def parse[T](body: String)(implicit decoder: DecodeJson[T]): FreeStep[T] = liftF(Parse[T, T](body, decoder, identity))

  def check(t: => TestResult[String]):FreeStep[Unit] = liftF( Check[Unit](() => t, ()) )

}