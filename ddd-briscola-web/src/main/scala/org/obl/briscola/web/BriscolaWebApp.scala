package org.obl.briscola.web

import org.http4s.dsl._
import org.obl.briscola.player.PlayerId
import org.obl.briscola.service.BriscolaApp

class BriscolaWebApp(val routes: AppRoutes, val app: BriscolaApp) extends PlansModule with ChannelModule[PlayerId] {

  val toPresentation = {
    import org.http4s.dsl._
    new ToPresentation(err => InternalServerError(err.toString))
  }

  lazy val SiteMap = new modules.SiteMap(routes, app, toPresentation)
  lazy val Players = new modules.Players(routes, app, toPresentation)
  lazy val Games = new modules.Games(routes, app, toPresentation)
  lazy val Competitions = new modules.Competitions(routes, app, toPresentation)
  
  lazy val plans = SiteMap.plans ++ Players.plans ++ Competitions.plans ++ Games.plans
  lazy val channel = WebSocketChannel.merge(Seq(Players.channel, Competitions.channel, Games.channel))

}

class BriscolaWebAppConfig(val routes: AppRoutes, app: BriscolaApp) {
  lazy val webApp = new BriscolaWebApp(routes, app)

  class ConfiguredPlayerWebSocketEndPoint extends 
    WebSocketEndPoint(routes.playerWebSocketRoutes.PlayerById, WebSocketConfig.fromObservableFactory(webApp.channel) )

}

