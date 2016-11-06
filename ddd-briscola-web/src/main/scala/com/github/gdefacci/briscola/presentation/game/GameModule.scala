package com.github.gdefacci.briscola.presentation.game

import com.github.gdefacci.briscola.service.game.GameService
import com.github.gdefacci.briscola.game.BriscolaError
import com.github.gdefacci.briscola.web.util.ToPresentation
import com.github.gdefacci.briscola.web.util.WebSocketChannel
import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.presentation.EventAndState
import com.github.gdefacci.briscola.presentation.RoutesServletConfig

object GameModule {
  
  def plan(routesServletConfig: RoutesServletConfig, 
      routes: GameRoutes, 
      service: GameService, 
      toPresentation:ToPresentation[BriscolaError], 
      gamePresentationAdapter:GamePresentationAdapter):GamesPlan = {
    import gamePresentationAdapter._
    new GamesPlan(routesServletConfig.games, routes, service, toPresentation)
  }
  
  def stateChangeFilter(service: GameService, gamePresentationAdapter:GamePresentationAdapter) = {
    import gamePresentationAdapter._
    new GamesStateChangeFilter(service)
  }

  def channel(service: GameService, gamesStateChangeFilter:GamesStateChangeFilter)(
      implicit enc: argonaut.EncodeJson[EventAndState[BriscolaEvent,GameState]]):WebSocketChannel[PlayerId] =
    WebSocketChannel(service.changes, gamesStateChangeFilter)
  
}