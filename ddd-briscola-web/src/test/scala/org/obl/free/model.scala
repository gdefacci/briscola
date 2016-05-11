package org.obl.free

import argonaut.DecodeJson
import scalaz.Functor
import scalaz.Free
import org.obl.raz.Path

sealed trait Step[S, Next]

case class HTTPCall[S,N](method:HTTPMethod, url:String, body:Option[String], next:Step.Response => N) extends Step[S,N]
case class Parse[S,N,T](body:String, decoder:DecodeJson[T], next:T => N) extends Step[S,N]
case class Check[S,N](value:Boolean, description:String, next:N) extends Step[S,N]
case class WebSocket[S,N](url:String, next:ObservableHolder[String] => N) extends Step[S,N]
case class GetState[S,Next](next:S => Next) extends Step[S,Next]

object Step {
  
  implicit def functor[S]: Functor[({ type Type[T] = Step[S,T]})#Type] = new Functor[({ type Type[T] = Step[S,T]})#Type] {
    def map[A,B](step: Step[S,A])(f: A => B): Step[S,B] = step match {
      case HTTPCall(mthd, url, body, next) => HTTPCall(mthd, url, body, next andThen f)
      case Parse(body, decoder, next) => Parse(body, decoder, next andThen f)
      case WebSocket(url, next) => WebSocket(url, next andThen f)
      case Check(v, desc, next) => Check(v, desc, f(next))
      case GetState(next) => GetState(next andThen f)
    }
  }
  
  type Response = scalaj.http.HttpResponse[String]
  
  type Free[S,A] = scalaz.Free[({type Type[T] = Step[S,T]})#Type, A]
  
}

class StepFactory[S] {

  import Step.Response
  
  type StepType[T] = Step[S,T]
  
  type FreeStep[A] = Step.Free[S, A]

  def liftF[A](value: => StepType[A]) = Free.liftF[StepType,A](value)
  
  def pure[A](value: => A): FreeStep[A] = Free.point[({type Type[T] = Step[S,T]})#Type, A](value)
  
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
  def parse[T](body:String)(implicit decoder:DecodeJson[T]):FreeStep[T] = liftF( Parse[S,T,T](body, decoder, identity) )
  def check(value:Boolean, desc:String):FreeStep[Unit] = liftF( Check(value, desc, ()) )
  
  def initialState:FreeStep[S] = liftF(GetState(identity))
  
}