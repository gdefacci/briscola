package org.obl.briscola.web

import scala.reflect.ClassTag
import org.obl.briscola.presentation
import org.obl.briscola.service.BriscolaApp
import org.obl.briscola.web.util.Containerconfigurator
import org.obl.briscola.web.util.ServletContextPlanAdder.toJettyPlanAdder
import org.obl.briscola.web.util.WSEndPointAdder.toWSEndPointAdder
import javax.servlet.ServletContext
import javax.websocket.Endpoint
import javax.websocket.server.ServerContainer
import org.obl.raz.{Scheme, Authority, Path}

class BriscolaWebApp(val routes:AppRoutes, val app:BriscolaApp) {
  
  lazy val playerPresentationAdapter = PlayerPresentationAdapter (routes.playerRoutes, routes.playerWebSocketRoutes, routes.competitionRoutes)
  
  lazy val gamePresentationAdapter = GamePresentationAdapter(routes.gameRoutes, routes.playerRoutes)
  
  lazy val competitionPresentationAdapter = CompetitionPresentationAdapter(routes.playerRoutes, routes.competitionRoutes)
  
  lazy val gamePlayersInputAdapter = GamePlayersInputAdapter(routes.playerRoutes)
  
  lazy val playersPlan = new PlayersPlan(routes.servletConfig.players, routes.playerRoutes, app.playerService, 
      playerPresentationAdapter, gamePresentationAdapter, competitionPresentationAdapter) 
  
  lazy val gamesPlan = new GamesPlan(routes.servletConfig.games, routes.gameRoutes, app.gameService, gamePresentationAdapter) 
  
  lazy val competitionsPlan = new CompetitionsPlan(routes.servletConfig.competitions, routes.competitionRoutes, app.competitionService, competitionPresentationAdapter, gamePlayersInputAdapter) 
  
  lazy val siteMap = 
    presentation.SiteMap(routes.playerRoutes.Players.path, routes.playerRoutes.PlayerLogin.path)
  
  lazy val siteMapPlan = new SiteMapPlan(routes.servletConfig.siteMap, routes.siteMapRoutes, siteMap) 

  import jsonEncoders._
  
  lazy val gamePlayerChannels:WsPlayerChannel = new WsPlayerChannelImpl(app.gameService.changes, new GamesStateChangeFilter(app.gameService, gamePresentationAdapter))
  lazy val playerPlayerChannels:WsPlayerChannel = new WsPlayerChannelImpl(app.playerService.changes, new PlayersStateChangeFilter(playerPresentationAdapter))
  lazy val competitionPlayerChannels:WsPlayerChannel = new WsPlayerChannelImpl(app.competitionService.changes, new CompetitionsStateChangeFilter(app.competitionService, competitionPresentationAdapter))
  
  
  lazy val playerSocketConfig:PlayerSocketConfig = {
    
    val gmCfg = new BasePlayerSocketConfig(gamePlayerChannels)
    val compCfg = new BasePlayerSocketConfig(competitionPlayerChannels)
    val playerCfg = new BasePlayerSocketConfig(playerPlayerChannels)
    
    PlayerSocketConfig(Seq(gmCfg, compCfg, playerCfg))
  }
  
}


class BriscolaWebAppConfig(val routes:AppRoutes, app: BriscolaApp) {
  lazy val webApp = new BriscolaWebApp(routes, app)
  
  class ConfiguredPlayerWebSocketEndPoint extends PlayerWebSocketEndPoint(routes.playerWebSocketRoutes, webApp.playerSocketConfig)

}

class BriscolaContainerConfigurator[T <: Endpoint](config:BriscolaWebAppConfig)(implicit classTag:ClassTag[T]) extends Containerconfigurator {

  def configureWerbSockets(container: ServerContainer) = {
    container.addWebSocketEndPoint[T](config.routes.playerWebSocketRoutes.playerByIdUriTemplate)
  }

  def configureWeb(context: ServletContext) = {
    val webApp = config.webApp
    context.addPlan(webApp.competitionsPlan)
    context.addPlan(webApp.gamesPlan)
    context.addPlan(webApp.playersPlan)
    context.addPlan(webApp.siteMapPlan)
  }
  
}