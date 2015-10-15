package org.obl.briscola
package web

import javax.websocket.Endpoint
import javax.websocket.Session
import javax.websocket.CloseReason
import javax.websocket.EndpointConfig
import org.obl.briscola.web.util.UrlParseUtil
import org.obl.briscola.web.util.ArgonautEncodeHelper._
import org.obl.briscola.web.Presentation.EventAndState
import javax.servlet.http.HttpServlet
import javax.servlet.ServletConfig
import javax.websocket.server.ServerContainer
import javax.websocket.server.ServerEndpointConfig
import org.obl.raz.PathSg
import org.obl.briscola.competition._
import org.obl.briscola.player._
import org.obl.ddd.StateChange
import rx.lang.scala.Observable
import argonaut.EncodeJson
import org.obl.briscola.web.Presentation.EventAndState
import StateChangeFilter._
import org.obl.briscola.service._
import org.obl.briscola.service.player.PlayerEvent
import org.obl.briscola.web.util.Plan
import org.obl.briscola.web.util.ServletRoutes
import org.obl.raz.RelativePath
import org.obl.raz.PathSg

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

class PlayerWebSocketEndPoint(contextPath: PathSg, playerWebSocketRoutes: => PlayerWebSocketRoutes,
    playerService: => PlayerService,
    gameService: => BriscolaService,
    competitionService: => CompetitionsService,
    playerStateChangeFilter:StateChangeFilter[Iterable[Player], PlayerEvent, Iterable[Presentation.Player], Presentation.PlayerEvent],
    gameStateChangeFilter:StateChangeFilter[GameState, BriscolaEvent, Presentation.GameState, Presentation.BriscolaEvent],
    competitionStateChangeFilter:StateChangeFilter[CompetitionState, CompetitionEvent, Presentation.CompetitionState, Presentation.CompetitionEvent]) extends Endpoint {
    

  import jsonEncoders._

  override def onOpen(session: Session, config: EndpointConfig) = {
    println("-" * 80)
    println("Openign websocket "+session.getRequestURI.toString())
    println("-" * 80)
    val sw = new SessionWrapper(session)
    UrlParseUtil.parseUrl(session.getRequestURI.toString()).map { url =>
      url match {
        case playerWebSocketRoutes.PlayerById(pid) =>
          sw.receiveFrom(gameService.changes.collect(gameStateChangeFilter(pid)))
          sw.receiveFrom(competitionService.changes.collect(competitionStateChangeFilter(pid)))
          sw.receiveFrom(playerService.changes.collect(playerStateChangeFilter(pid)))
          
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