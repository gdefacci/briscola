package org.obl.briscola
package web

import scalaz.{ -\/, \/, \/- }
import org.obl.ddd.DomainError
import org.http4s.Response
import scalaz.concurrent.Task
import argonaut.EncodeJson
import scalaz.Functor
import scala.language.higherKinds

trait PresentationAdapter[D, P] extends (D => P) {
  def apply(d: D):P
}

object PresentationAdapter {

  def apply[D, P](f: D => P) = new PresentationAdapter[D, P] {
    def apply(d: D): P = f(d)
  }
  
  def apply[D,P](d:D)(implicit pa:PresentationAdapter[D,P]) = pa(d)
    
  implicit def functorPresentationAdapter[D, P, F[_]](implicit pa: PresentationAdapter[D, P], F:Functor[F]): PresentationAdapter[F[D], F[P]] =
    PresentationAdapter[F[D], F[P]] { d:F[D] =>
      F.map(d)(pa)
    }
  
}

class ToPresentation(errorToPresentation: DomainError => Task[Response]) {

  import org.http4s.dsl._
  import org.obl.briscola.web.util.ArgonautEncodeHelper._

  //  def just[D,P](v:D)(implicit pa:PresentationAdapter[D,P], ej:EncodeJson[P]): Task[Response] = 
  //    apply(\/-(Some(v)))

  //  def apply[D,P,Err <: DomainError](v:Err \/ D)(implicit pa:PresentationAdapter[D,P], ej:EncodeJson[P]): Task[Response] = 
  //    create(v.map(Some(_)))

  //  def found[D,P](v:Option[D])(implicit pa:PresentationAdapter[D,P], ej:EncodeJson[P]): Task[Response] = 
  //    apply(\/-(v))

  def apply[D, P](vo: DomainError \/ Option[D])(implicit pa: PresentationAdapter[D, P], ej: EncodeJson[P]): Task[Response] = {
    vo.map {
      case None => NotFound()
      case Some(v) =>
        Ok(asJson(pa(v)))
    } match {
      case \/-(v) => v
      case -\/(err) => errorToPresentation(err)
    }

  }

}
