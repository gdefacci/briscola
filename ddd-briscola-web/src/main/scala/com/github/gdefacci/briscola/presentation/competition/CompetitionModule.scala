package com.github.gdefacci.briscola.presentation.competition

import com.github.gdefacci.briscola.presentation

import com.github.gdefacci.briscola.service.competition.CompetitionsService
import com.github.gdefacci.briscola.web.util.ToPresentation
import com.github.gdefacci.briscola.competition.CompetitionError
import com.github.gdefacci.briscola.web.util.WebSocketChannel

import argonaut.EncodeJson
import com.github.gdefacci.briscola.presentation.player.PlayersInputAdapter
import com.github.gdefacci.briscola.presentation.player.PlayerRoutes
import com.github.gdefacci.briscola.presentation.RoutesServletConfig

object CompetitionModule {

  def gamePlayersInputAdapter(playerRoutes: PlayerRoutes) =
    new PlayersInputAdapter(playerRoutes)

  def plan(routesServletConfig: RoutesServletConfig,
    routes: CompetitionRoutes,
    service: CompetitionsService,
    toPresentation: ToPresentation[CompetitionError],
    presentationAdapter: CompetitionPresentationAdapter,
    inputAdapter: PlayersInputAdapter) = {
    import presentationAdapter._

    new CompetitionsPlan(routesServletConfig.competitions, routes, service, inputAdapter, toPresentation)
  }

  def stateChangeFilter(service: CompetitionsService, presentationAdapter: CompetitionPresentationAdapter) = {
    import presentationAdapter._
    new CompetitionsStateChangeFilter(service)
  }

  def channel(service: CompetitionsService, competitionsStateChangeFilter: CompetitionsStateChangeFilter)(
      implicit enc: EncodeJson[presentation.EventAndState[presentation.competition.CompetitionEvent, presentation.competition.CompetitionState]]) =
    WebSocketChannel(service.changes, competitionsStateChangeFilter)

}