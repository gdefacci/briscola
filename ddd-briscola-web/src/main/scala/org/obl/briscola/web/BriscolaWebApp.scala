package org.obl.briscola.web

import org.obl.briscola.presentation
import org.obl.raz.BasePath
import org.obl.raz.BasePosition
import org.obl.raz.SegmentPosition
import org.obl.briscola.service.BriscolaApp
import org.obl.briscola.web.util.Containerconfigurator
import javax.servlet.ServletContext
import org.obl.briscola.web.util.ServletContextPlanAdder._
import org.obl.briscola.web.util.WSEndPointAdder._
import javax.websocket.server.ServerContainer
import scala.reflect.ClassTag

class BriscolaWebApp(routes:AppRoutes, val app:BriscolaApp) {
  
  lazy val playerPresentationAdapter = PlayerPresentationAdapter (routes.playerRoutes, routes.playerWebSocketRoutes, routes.competitionRoutes)
  
  lazy val gamePresentationAdapter = GamePresentationAdapter(routes.gameRoutes, routes.playerRoutes)
  
  lazy val competitionPresentationAdapter = CompetitionPresentationAdapter(routes.playerRoutes, routes.competitionRoutes)
  
  lazy val gamePlayersInputAdapter = GamePlayersInputAdapter(routes.playerRoutes)
  
  lazy val playersPlan = new PlayersPlan(routes.playerRoutes, app.playerService, 
      playerPresentationAdapter, gamePresentationAdapter, competitionPresentationAdapter) 
  
  lazy val gamesPlan = new GamesPlan(routes.gameRoutes, app.gameService, gamePresentationAdapter) 
  
  lazy val competitionsPlan = new CompetitionsPlan(routes.competitionRoutes, app.competitionService, competitionPresentationAdapter, gamePlayersInputAdapter) 
  
  lazy val siteMap = 
    presentation.SiteMap(routes.playerRoutes.Players.encodePath, routes.playerRoutes.PlayerLogin.encodePath)
  
  lazy val siteMapPlan = new SiteMapPlan(routes.siteMapRoutes, siteMap) 
  
  lazy val playerSocketConfig:PlayerSocketConfig = {
    import jsonEncoders._
    
    val gmCfg = new BasePlayerSocketConfig(app.gameService.changes, new GamesStateChangeFilter(app.gameService, gamePresentationAdapter))
    val compCfg = new BasePlayerSocketConfig(app.competitionService.changes, new CompetitionsStateChangeFilter(app.competitionService, competitionPresentationAdapter))
    val playerCfg = new BasePlayerSocketConfig(app.playerService.changes, new PlayersStateChangeFilter(playerPresentationAdapter))
    
    PlayerSocketConfig(Seq(gmCfg, compCfg, playerCfg))
  }
  
}

class BriscolaWebAppConfig(val routesConfig: RoutesServletConfig, app: BriscolaApp) {
  lazy val routes:AppRoutes = AppRoutesImpl(routesConfig)
  lazy val webApp = new BriscolaWebApp(routes, app)
  
  class ConfiguredPlayerWebSocketEndPoint extends PlayerWebSocketEndPoint(routes.playerWebSocketRoutes, webApp.playerSocketConfig)

}

class BriscolaContainerConfigurator[T <: PlayerWebSocketEndPoint](config:BriscolaWebAppConfig)(implicit classTag:ClassTag[T]) extends Containerconfigurator {

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