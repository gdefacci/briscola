package com.github.gdefacci.briscola.web.util

import org.obl.raz.Path
import org.slf4j.LoggerFactory

import com.typesafe.scalalogging.Logger

import javax.websocket.{CloseReason, Endpoint, EndpointConfig, Session}
import rx.lang.scala.Observable
import org.obl.raz.Api.PathDecoder
import argonaut.EncodeJson

class SessionWrapper(session:Session) {
  
  lazy val log = Logger(LoggerFactory.getLogger(getClass))
  
  private def sendText(session: Session, msg: String) = {
    log.debug("sending to websocket " + msg);
    session.getBasicRemote.sendText(msg)
  }
  
  def receiveTextFrom[T](obs:Observable[String]) = {
    obs.subscribe { es =>
      if (session.isOpen()) sendText(session, es)
    }
  }
  
}

trait WebSocketConfig[T] {
  def config(session:Session, pid:T):Unit 
}

object WebSocketConfig {
  
	def fromObservableFactory[T](changes: T => Observable[String]):WebSocketConfig[T] =
	  apply((session:Session, pid:T) => new SessionWrapper(session).receiveTextFrom(changes(pid)))
	
  def apply[T](cfg:(Session, T) => Unit) = new WebSocketConfig[T] {
    def config(session:Session, pid:T):Unit = {
      cfg(session, pid)
    }
  }
  
  def apply[T](cfgs:Seq[WebSocketConfig[T]]):WebSocketConfig[T] = 
    WebSocketConfig { (session, pid) =>
      cfgs.foreach( cfg => cfg.config(session, pid) )
    }
  
}

class WebSocketEndPoint[T](ById:PathDecoder[T], wsConfig:WebSocketConfig[T]) extends Endpoint {
 
  lazy val log = Logger(LoggerFactory.getLogger(getClass))
  
  override def onOpen(session: Session, config: EndpointConfig) = {
    log.debug("-" * 80)
    log.debug("Openign websocket "+session.getRequestURI.toString())
    log.debug("-" * 80)
    val sw = new SessionWrapper(session)
    Path.fromJavaUri(new java.net.URI(session.getRequestURI.toString())).map { url =>
      url match {
        case ById(pid) =>
          wsConfig.config(session, pid)
          
        case x => {
          log.debug("*" * 80)
          log.debug(s"unmatched websocket uri, uri:$x")
        }
      }
    }
  }

  override def onClose(session: Session, closeReason: CloseReason) = {
  }

  override def onError(session: Session, thr: Throwable) = {
  }

}