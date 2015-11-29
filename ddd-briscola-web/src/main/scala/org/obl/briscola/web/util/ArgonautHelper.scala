package org.obl.briscola.web.util

import argonaut._
import Argonaut._
import org.obl.raz.PathDecoder
import scalaz.{ -\/, \/, \/- }

object ArgonautHelper {

  lazy val pathDecoder = DecodeJson[org.obl.raz.Path] { j =>
    j.as[String].toDisjunction.leftMap(_._1).flatMap { s =>
      UrlParseUtil.parseUrl(s) match {
        case None => -\/(s"$s is not a valid url")
        case Some(v) => \/-(v)
      }
    } match {
      case -\/(err) => DecodeResult.fail(err, j.history)
      case \/-(v) => DecodeResult.ok(v)
    }
  }
  
  def enumDecoder[E <: Enumeration](e:E) = DecodeJson[E#Value] { j =>
    j.as[String].flatMap[E#Value] { s =>
      e.values.find(s == _.toString) match {
        case Some(v) => DecodeResult.ok(v)
        case None => DecodeResult.fail(s"$s is not a valid ${e.getClass.getName}", j.history)
      }
    }
  }
  
  def enumEncoder[E <: Enumeration] = jencode1[E#Value, String]((p: E#Value) => p.toString)
  
  def fromMap[K, T](entries:Map[K, T], errMessage: => String)(implicit d:DecodeJson[K]) = DecodeJson[T] { t =>
    t.as[K].flatMap { k =>
      entries.get(k) match {
        case None => DecodeResult.fail(errMessage, t.history)
        case Some(v) => DecodeResult.ok(v)
      }
    }
  }
  
}