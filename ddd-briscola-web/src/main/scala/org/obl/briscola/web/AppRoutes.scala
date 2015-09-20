package org.obl.briscola.web

import org.obl.raz.BasePath
import org.obl.raz.BasePosition
import org.obl.raz.SegmentPosition

trait AppRoutes {
  def playerRoutes:PlayerRoutes
  def gameRoutes:GameRoutes
  def competitionRoutes:CompetitionRoutes
}

object AppRoutesImpl {
  
  def apply(path:BasePath[BasePosition, SegmentPosition]) = {
    new AppRoutesImpl(new Resources(path))
  }
  
}

class AppRoutesImpl(val resources:Resources) extends AppRoutes {
  
  lazy val playerRoutes = new PlayerRoutes {
    def Players = resources.Players
    def PlayerById = resources.Players.byId
    def PlayerWebSocket = resources.Players.websocketById
  }
  
  val gameRoutes = new GameRoutes {
    def Games = resources.Games
    def GameById = resources.Games.byId
    def Player = resources.Games.player
  }
  
  val competitionRoutes = new CompetitionRoutes {
    def Competitions = resources.Competitions
    def CompetitionById = resources.Competitions.byId
    def AcceptCompetition = resources.Competitions.accept
    def DeclineCompetition  = resources.Competitions.decline
    def CreateCompetition = resources.Competitions.create
  }
 
}