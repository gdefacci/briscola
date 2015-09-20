package org.obl.briscola.web

import org.obl.raz.BasePath
import org.obl.raz.BasePosition
import org.obl.raz.SegmentPosition
import org.obl.briscola.BriscolaApp

class BriscolaWebApp(routes:AppRoutes, app:BriscolaApp) extends App {
  
  private lazy val playerPresentationAdapter = PlayerPresentationAdapter (routes.playerRoutes, routes.competitionRoutes)
  
  private lazy val gamePresentationAdapter = GamePresentationAdapter(routes.gameRoutes, routes.playerRoutes)
  
  private lazy val competitionPresentationAdapter = CompetitionPresentationAdapter(routes.playerRoutes, routes.competitionRoutes)
  
  lazy val playersPlan = new PlayersPlan(routes.playerRoutes, app.playerService, 
      playerPresentationAdapter, gamePresentationAdapter, competitionPresentationAdapter) 
  
  lazy val gamesPlan = new GamesPlan(routes.gameRoutes, app.gameService, gamePresentationAdapter) 
  
  lazy val competitionsPlan = new CompetitionsPlan(routes.competitionRoutes, routes.playerRoutes, app.competitionService, competitionPresentationAdapter) 
  
}