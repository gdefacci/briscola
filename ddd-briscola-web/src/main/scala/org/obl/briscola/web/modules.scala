package org.obl.briscola.web

import org.obl.briscola.player.PlayerId
import org.obl.briscola.presentation.adapters.PlayerPresentationAdapter
import org.obl.briscola.service.BriscolaApp
import org.obl.briscola.presentation.adapters.GamePresentationAdapter
import org.obl.briscola.presentation.adapters.CompetitionPresentationAdapter
import org.obl.briscola.presentation.adapters.input.GamePlayersInputAdapter
import org.obl.briscola.presentation
import org.obl.briscola.web.plans._
import org.obl.briscola.web.util.WebSocketChannel
import org.obl.briscola.web.util.ToPresentation

object modules {

  import jsonEncoders._
  
  class Players(routes: AppRoutes, app: BriscolaApp, toPresentation:ToPresentation) extends PlansModule with ChannelModule[PlayerId] {

    lazy val presentationAdapter = PlayerPresentationAdapter(routes.playerRoutes, routes.playerWebSocketRoutes, routes.competitionRoutes)
    import presentationAdapter._

    lazy val plans = new PlayersPlan(routes.servletConfig.players, routes.playerRoutes, app.playerService, toPresentation) :: Nil
    lazy val channel = WebSocketChannel(app.playerService.changes, new PlayersStateChangeFilter)

  }
  
  class Games(routes: AppRoutes, app: BriscolaApp, toPresentation:ToPresentation) extends PlansModule with ChannelModule[PlayerId] {

    lazy val presentationAdapter = GamePresentationAdapter(routes.gameRoutes, routes.playerRoutes)
    import presentationAdapter._

    lazy val plans = new GamesPlan(routes.servletConfig.games, routes.gameRoutes, app.gameService, toPresentation) :: Nil
    lazy val channel = WebSocketChannel(app.gameService.changes, new GamesStateChangeFilter(app.gameService))

  }

  class Competitions(routes: AppRoutes, app: BriscolaApp, toPresentation:ToPresentation) extends PlansModule with ChannelModule[PlayerId] {

    lazy val presentationAdapter = CompetitionPresentationAdapter(routes.playerRoutes, routes.competitionRoutes)
    lazy val gamePlayersInputAdapter = GamePlayersInputAdapter(routes.playerRoutes)
    import presentationAdapter._

    lazy val plans = new CompetitionsPlan(routes.servletConfig.competitions, routes.competitionRoutes, app.competitionService, gamePlayersInputAdapter, toPresentation) :: Nil
    lazy val channel = WebSocketChannel(app.competitionService.changes, new CompetitionsStateChangeFilter(app.competitionService))

  }

  class SiteMap(routes: AppRoutes, app: BriscolaApp, toPresentation:ToPresentation) extends PlansModule {
    lazy val siteMap =
      presentation.SiteMap(routes.playerRoutes.Players.path, routes.playerRoutes.PlayerLogin.path)

    lazy val plans = new SiteMapPlan(routes.servletConfig.siteMap, routes.siteMapRoutes, siteMap) :: Nil
  }

}