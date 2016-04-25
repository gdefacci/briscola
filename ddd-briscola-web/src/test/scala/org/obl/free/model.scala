package org.obl.free

import argonaut.DecodeJson
import scalaz.Functor
import scalaz.Free
import scalaz.Free.{liftF,point}
import org.obl.briscola.presentation.SiteMap
import org.obl.raz.Path
import rx.lang.scala.Observable
import scala.util.Try

sealed trait Step[Next]

object Step {
  
  implicit val functor: Functor[Step] = new Functor[Step] {
    def map[A,B](step: Step[A])(f: A => B): Step[B] = step match {
      case HTTPCall(mthd, url, body, next) => HTTPCall(mthd, url, body, next andThen f)
      case Parse(body, decoder, next) => Parse(body, decoder, next andThen f)
      case WebSocket(url, next) => WebSocket(url, next andThen f)
      case Check(v, desc, next) => Check(v, desc, f(next))
    }
  }
  
  type FreeStep[A] = Free[Step, A]

  type Response = scalaj.http.HttpResponse[String]
  
  def liftF[A](value: => Step[A]) = Free.liftF[Step,A](value)
  
  def pure[A](value: => A): FreeStep[A] = point(value)
  
  def http(method:HTTPMethod, url:String, body:Option[String]):FreeStep[Response] = liftF( HTTPCall(method, url, body, identity) ) 
  def http(method:HTTPMethod, url:Path, body:Option[String]):FreeStep[Response] = http(method, url.render, body)
  
  def get(url:String):FreeStep[Response] = http(GET, url, None)
  def get(url:Path):FreeStep[Response] = get(url.render)
  
  def post(url:String):FreeStep[Response] = http(POST, url, None)
  def post(url:Path):FreeStep[Response] = post(url.render)

  def post(url:String, body:String):FreeStep[Response] = http(POST, url, Some(body))
  def post(url:Path, body:String):FreeStep[Response] = post(url.render, body)

  def webSocket(url:Path):FreeStep[ObservableHolder[String]] = webSocket(url.render)
  private def webSocket(url:String):FreeStep[ObservableHolder[String]] = liftF( WebSocket(url, identity) )
  def parse[T](body:String)(implicit decoder:DecodeJson[T]):FreeStep[T] = liftF( Parse[T,T](body, decoder, identity) )
  def check(value:Boolean, desc:String):FreeStep[Unit] = liftF( Check(value, desc, ()) )
  
}

case class HTTPCall[N](method:HTTPMethod, url:String, body:Option[String], next:Step.Response => N) extends Step[N]
case class Parse[N,T](body:String, decoder:DecodeJson[T], next:T => N) extends Step[N]
case class Check[N](value:Boolean, description:String, next:N) extends Step[N]
case class WebSocket[N](url:String, next:ObservableHolder[String] => N) extends Step[N]

case class ObservableHolder[T](source:Observable[T]) {
  
  private val buffer = collection.mutable.Buffer.empty[T]
  
  source.foreach { msg =>
    println("*"*160)
    println("received message " + msg)
    println("*"*160)
    buffer += msg 
  }
  
  def messages:Seq[T] = buffer.toSeq
  
}

