package org.obl.briscola.web

import org.obl.briscola.player.PlayerId
import org.obl.briscola.service.BriscolaService
import argonaut.EncodeJson
import rx.lang.scala.Observable
import org.obl.briscola.web.util.ArgonautEncodeHelper._
import org.obl.briscola.service.CompetitionsService
import org.obl.briscola.presentation._
import org.obl.briscola.service.PlayerService

trait WsPlayerChannel extends (PlayerId => Observable[String]) {
  
  def apply(pid:PlayerId):Observable[String]
  
}

class WsPlayerChannelImpl[T,R](changes: => Observable[T], filter: => PlayerId => PartialFunction[T, R])(implicit enc:EncodeJson[R]) extends WsPlayerChannel {
  def apply(pid:PlayerId):Observable[String] = {
    changes.collect(filter(pid)).map { v =>
      asJson(v)(enc)
    }
  }
}

class GameWsPlayerChannel1(gameService: => BriscolaService, gamePresentationAdapter: => GamePresentationAdapter)(
    implicit enc:EncodeJson[EventAndState[BriscolaEvent, GameState]]) extends WsPlayerChannel {
  
  private lazy val filter = new GamesStateChangeFilter(gameService, gamePresentationAdapter)
  def apply(pid:PlayerId):Observable[String] = {
    gameService.changes.collect(filter(pid)).map { v =>
      asJson(v)(enc)
    }
  }
}

class CompetitionWsPlayerChannel(competitionService: => CompetitionsService, toPresentation: => CompetitionPresentationAdapter)(
    implicit enc:EncodeJson[EventAndState[CompetitionEvent, CompetitionState]]) extends WsPlayerChannel {
  
  private lazy val filter = new CompetitionsStateChangeFilter(competitionService, toPresentation)
  def apply(pid:PlayerId):Observable[String] = {
    competitionService.changes.collect(filter(pid)).map( asJson(_)(enc) )
  }
}

class PlayerWsPlayerChannel(playerService: => PlayerService, toPresentation: => PlayerPresentationAdapter)(
    implicit enc:EncodeJson[EventAndState[PlayerEvent, Iterable[Player]]]) extends WsPlayerChannel {
  
  private lazy val filter = new PlayersStateChangeFilter(toPresentation)
  def apply(pid:PlayerId):Observable[String] = {
    playerService.changes.collect(filter(pid)).map { v =>
      asJson(v)(enc)
    }
  }
}

