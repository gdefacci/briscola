package com.github.gdefacci.briscola.presentation.player

import com.github.gdefacci.briscola.service.player.PlayerService
import com.github.gdefacci.briscola.web.util.ToPresentation
import com.github.gdefacci.briscola.player._
import com.github.gdefacci.briscola.presentation
import com.github.gdefacci.briscola.web.util.WebSocketChannel
import com.github.gdefacci.briscola.presentation.RoutesServletConfig

object PlayersModule {

  def plan(
    routesServletConfig: RoutesServletConfig,
    playerRoutes: PlayerRoutes,
    playerService: PlayerService,
    toPresentation: ToPresentation[PlayerError],
    playerPresentationAdapter:PlayerPresentationAdapter): PlayersPlan = {
    import playerPresentationAdapter._
    new PlayersPlan(routesServletConfig.players, playerRoutes, playerService, toPresentation)
  }

  def stateChangeFilter(playerPresentationAdapter:PlayerPresentationAdapter) = {
    import playerPresentationAdapter._
    new PlayersStateChangeFilter
  }
  
  def channel(playerService: PlayerService,
    playersStateChangeFilter: PlayersStateChangeFilter)(
      implicit enc: argonaut.EncodeJson[presentation.EventAndState[PlayerEvent, Iterable[Player]]]): WebSocketChannel[PlayerId] =
    WebSocketChannel(playerService.changes, playersStateChangeFilter)(enc)

}