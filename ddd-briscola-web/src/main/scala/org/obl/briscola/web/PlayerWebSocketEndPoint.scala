package org.obl.briscola
package web

import org.obl.briscola.player.PlayerId
import org.obl.briscola.presentation.BriscolaEvent
import org.obl.briscola.presentation.EventAndState
import org.obl.briscola.presentation.GameState
import org.obl.briscola.service.BriscolaService
import org.obl.briscola.web.util.ArgonautEncodeHelper.asJson
import org.obl.briscola.web.util.UrlParseUtil
import org.slf4j.LoggerFactory

import com.typesafe.scalalogging.Logger

import argonaut.EncodeJson
import javax.websocket.CloseReason
import javax.websocket.Endpoint
import javax.websocket.EndpointConfig
import javax.websocket.Session
import rx.lang.scala.Observable

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

object BasePlayerSocketConfig {
  
  def games(gameService:BriscolaService, gamePresentationAdapter: => GamePresentationAdapter)(implicit enc:EncodeJson[EventAndState[BriscolaEvent, GameState]]):PlayerId => Observable[String] = {
    val gamesStateChangeFilter = new GamesStateChangeFilter(gameService, gamePresentationAdapter)
    pid => gameService.changes.collect(gamesStateChangeFilter.apply(pid)).map( asJson(_)(enc) ) 
  }
  
}

class BasePlayerSocketConfig(changes: PlayerId => Observable[String]) extends PlayerSocketConfig {
  
  def config(session: Session, pid: PlayerId): Unit = {
    new SessionWrapper(session).receiveTextFrom(changes(pid))
  }
}


class PlayerWebSocketEndPoint(playerWebSocketRoutes: => PlayerWebSocketRoutes, wsConfig:PlayerSocketConfig) extends Endpoint {
 
  lazy val log = Logger(LoggerFactory.getLogger(getClass))
  
  import jsonEncoders._

  override def onOpen(session: Session, config: EndpointConfig) = {
    log.debug("-" * 80)
    log.debug("Openign websocket "+session.getRequestURI.toString())
    log.debug("-" * 80)
    val sw = new SessionWrapper(session)
    UrlParseUtil.parseUrl(session.getRequestURI.toString()).map { url =>
      url match {
        case playerWebSocketRoutes.PlayerById(pid) =>
          wsConfig.config(session, pid)
          
        case x => {
          log.debug("*" * 80)
          log.debug(playerWebSocketRoutes.PlayerById.toUriTemplate("var").render)
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