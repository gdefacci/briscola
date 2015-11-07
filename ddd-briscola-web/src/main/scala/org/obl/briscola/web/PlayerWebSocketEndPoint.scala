package org.obl.briscola
package web

import org.obl.briscola.player.PlayerId
import org.obl.briscola.presentation.EventAndState
import org.obl.briscola.web.StateChangeFilter.StateChangeFilter
import org.obl.briscola.web.util.ArgonautEncodeHelper.responseBody
import org.obl.briscola.web.util.UrlParseUtil
import org.obl.ddd.StateChange

import StateChangeFilter.StateChangeFilter
import argonaut.EncodeJson
import javax.websocket.CloseReason
import javax.websocket.Endpoint
import javax.websocket.EndpointConfig
import javax.websocket.Session
import rx.lang.scala.Observable

class SessionWrapper(session:Session) {
  
  private def sendText(session: Session, msg: String) = {
    println("sending to websocket " + msg);
    session.getBasicRemote.sendText(msg)
  }
  
  def receiveFrom[T](obs:Observable[T])(implicit enc:EncodeJson[T]) = {
    obs.subscribe { es =>
      if (session.isOpen()) sendText(session, responseBody(es))
    }
  }
  
}

trait PlayerSocketConfig {
  def config(session:Session, pid:PlayerId):Unit 
}

object PlayerSocketConfig {
  
  def apply(cfg:(Session, PlayerId) => Unit) = new PlayerSocketConfig {
    def config(session:Session, pid:PlayerId):Unit = {
      cfg(session, pid)
    }
  }
  
  def apply(cfgs:Seq[PlayerSocketConfig]):PlayerSocketConfig = 
    PlayerSocketConfig { (session, pid) =>
      cfgs.foreach( cfg => cfg.config(session, pid) )
    }
  
}

class BasePlayerSocketConfig[S,E,PS,PE](
    changes: => Observable[StateChange[S, E]],
    gameStateChangeFilter: StateChangeFilter[S, E, PS, PE])(implicit ej: EncodeJson[EventAndState[PE, PS]]) extends PlayerSocketConfig {
  
  def config(session: Session, pid: PlayerId): Unit = {
    new SessionWrapper(session).receiveFrom(changes.collect(gameStateChangeFilter(pid)))
  }
}


class PlayerWebSocketEndPoint(playerWebSocketRoutes: => PlayerWebSocketRoutes, wsConfig:PlayerSocketConfig) extends Endpoint {
    
  import jsonEncoders._

  override def onOpen(session: Session, config: EndpointConfig) = {
    println("-" * 80)
    println("Openign websocket "+session.getRequestURI.toString())
    println("-" * 80)
    val sw = new SessionWrapper(session)
    UrlParseUtil.parseUrl(session.getRequestURI.toString()).map { url =>
      url match {
        case playerWebSocketRoutes.PlayerById(pid) =>
          wsConfig.config(session, pid)
          
        case x => {
          println("*" * 80)
          println(playerWebSocketRoutes.PlayerById.toUriTemplate("var").render)
          println(s"unmatched websocket uri, uri:$x")
        }
      }
    }
  }

  override def onClose(session: Session, closeReason: CloseReason) = {
  }

  override def onError(session: Session, thr: Throwable) = {
  }

}