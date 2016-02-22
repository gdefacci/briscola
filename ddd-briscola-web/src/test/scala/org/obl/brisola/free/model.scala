package org.obl.brisola.free

import argonaut.DecodeJson
import com.ning.http.client.Response
import scalaz.Functor
import scalaz.Free
import scalaz.Free.liftF
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
  
  def http(method:HTTPMethod, url:String, body:Option[String]):FreeStep[Response] = liftF( HTTPCall(method, url, body, identity) ) 
  def http(method:HTTPMethod, url:Path, body:Option[String]):FreeStep[Response] = http(method, url.render, body)
  
  def get(url:String):FreeStep[Response] = http(GET, url, None)
  def get(url:Path):FreeStep[Response] = get(url.render)
  
  def post(url:String, body:String):FreeStep[Response] = http(POST, url, Some(body))
  def post(url:Path, body:String):FreeStep[Response] = post(url.render, body)

  def webSocket(url:Path):FreeStep[Observable[String]] = webSocket(url.render)
  def webSocket(url:String):FreeStep[Observable[String]] = liftF( WebSocket(url, identity) )
  def parse[T](body:String)(implicit decoder:DecodeJson[T]):FreeStep[T] = liftF( Parse[T,T](body, decoder, identity) )
  def check(value:Boolean, desc:String):FreeStep[Unit] = liftF( Check(value, desc, ()) )
  
}

case class HTTPCall[N](method:HTTPMethod, url:String, body:Option[String], next:Response => N) extends Step[N]
case class Parse[N,T](body:String, decoder:DecodeJson[T], next:T => N) extends Step[N]
case class Check[N](value:Boolean, description:String, next:N) extends Step[N]
case class WebSocket[N](url:String, next:Observable[String] => N) extends Step[N]



