package org.obl.briscola.web

import org.obl.raz.Api._
import org.obl.raz.UriTemplate
import org.obl.briscola.player.PlayerId
import org.obl.briscola.GameId
import org.obl.briscola.competition.CompetitionId

trait PlayerRoutes {
  def Players: PathMatchDecoder
  def PlayerLogin: PathMatchDecoder
  def PlayerById: PathCodec.Symmetric[PlayerId]
}

trait PlayerWebSocketRoutes {
  def PlayerById: PathCodec.Symmetric[PlayerId]
  def playerByIdUriTemplate: UriTemplate
}

trait GameRoutes {
  def Games: PathMatchDecoder
  def GameById: PathCodec.Symmetric[GameId]
  def Player: PathCodec.Symmetric[(GameId, PlayerId)]
  def Team: PathCodec.Symmetric[(GameId, String)]
}

trait CompetitionRoutes {
  def Competitions: PathMatchDecoder
  def CompetitionById: PathCodec.Symmetric[CompetitionId]
  def PlayerCompetitionById: PathCodec.Symmetric[(CompetitionId, PlayerId)]
  def AcceptCompetition: PathCodec.Symmetric[(CompetitionId, PlayerId)]
  def DeclineCompetition: PathCodec.Symmetric[(CompetitionId, PlayerId)]
  def CreateCompetition: PathCodec.Symmetric[PlayerId]
}

trait SiteMapRoutes {
  def SiteMap:PathMatchDecoder
}

class AppRoutes(resources:Resources) {
  
  lazy val servletConfig:RoutesServletConfig = resources.servletConfig
  
  lazy val playerRoutes = new PlayerRoutes {
    lazy val Players = resources.Players.pathMatchDecoder
    lazy val PlayerLogin = resources.Players.login.pathMatchDecoder
    lazy val PlayerById = resources.Players.byId.pathCodec
  }
  
  lazy val playerWebSocketRoutes = new PlayerWebSocketRoutes {
    lazy val PlayerById = resources.WebSockets.Players.byId.pathConverter
    lazy val playerByIdUriTemplate: UriTemplate = this.PlayerById.encodeUriTemplate("playerId")
  }
  
  lazy val gameRoutes = new GameRoutes {
    lazy val Games = resources.Games.pathMatchDecoder
    lazy val GameById = resources.Games.byId.pathCodec
    lazy val Player = resources.Games.player.pathCodec
    lazy val Team = resources.Games.team.pathCodec
  }
  
  lazy val competitionRoutes  = new CompetitionRoutes {
    lazy val Competitions = resources.Competitions.pathMatchDecoder
    lazy val CompetitionById = resources.Competitions.byId.pathCodec
    lazy val PlayerCompetitionById = resources.Competitions.player.pathCodec
    lazy val AcceptCompetition = resources.Competitions.accept.pathCodec
    lazy val DeclineCompetition = resources.Competitions.decline.pathCodec
    lazy val CreateCompetition = resources.Competitions.create.pathCodec
  }

  lazy val siteMapRoutes = new SiteMapRoutes {
    def SiteMap:PathMatchDecoder = resources.SiteMap.pathMatchDecoder 
  }
}