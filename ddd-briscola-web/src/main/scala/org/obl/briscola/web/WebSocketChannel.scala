package org.obl.briscola.web

import org.obl.briscola.web.util.ArgonautEncodeHelper.asJson

import argonaut.EncodeJson
import rx.lang.scala.Observable

trait WebSocketChannel[T] extends (T => Observable[String]) {

  def apply(pid: T): Observable[String]

}

object WebSocketChannel {

  def apply[T](f: T => Observable[String]) = new WebSocketChannel[T] {
    def apply(pid: T): Observable[String] = f(pid)
  }

  def apply[I, T, R](changes: => Observable[T], filter: => I => PartialFunction[T, R])(implicit enc: EncodeJson[R]): WebSocketChannel[I] =
    WebSocketChannel[I]((pid: I) =>
      changes.collect(filter(pid)).map { v =>
        asJson(v)(enc)
      })
      
  def merge[T](channels:Seq[WebSocketChannel[T]]):WebSocketChannel[T] = apply[T]( v => channels.foldLeft(Observable.empty:Observable[String]) { (obs, ch) =>
    obs.merge( ch(v) )
  } )

}

/*
class WsPlayerChannelImpl[I,T,R](changes: => Observable[T], filter: => I => PartialFunction[T, R])(implicit enc:EncodeJson[R]) extends WsChannel[I] {
  def apply(pid:I):Observable[String] = {
    changes.collect(filter(pid)).map { v =>
      asJson(v)(enc)
    }
  }
}
*/

