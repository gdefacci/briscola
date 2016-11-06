package com.github.gdefacci.briscola.web
package modules

import org.obl.raz.Api._
import org.obl.raz.UriTemplate
import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.game.GameId
import com.github.gdefacci.briscola.competition.CompetitionId

import javax.inject.Singleton
import com.github.gdefacci.briscola.presentation.player.PlayerRoutes
import com.github.gdefacci.briscola.presentation.player.PlayerWebSocketRoutes
import com.github.gdefacci.briscola.presentation.competition.CompetitionRoutes
import com.github.gdefacci.briscola.presentation.game.GameRoutes
import com.github.gdefacci.briscola.presentation.sitemap.SiteMapRoutes
import com.github.gdefacci.briscola.presentation.Resources

object RoutesModule {
  
  @Singleton def playerRoutes(resources:Resources):PlayerRoutes = new PlayerRoutes {
    lazy val Players = resources.Players.pathMatchDecoder
    lazy val PlayerLogin = resources.Players.login.pathMatchDecoder
    lazy val PlayerById = resources.Players.byId.pathCodec
  }
  
  @Singleton def playerWebSocketRoutes(resources:Resources):PlayerWebSocketRoutes = new PlayerWebSocketRoutes {
    lazy val PlayerById = resources.WebSockets.Players.byId.pathConverter
    lazy val playerByIdUriTemplate: UriTemplate = this.PlayerById.encodeUriTemplate("playerId")
  }
  
  @Singleton def gameRoutes(resources:Resources):GameRoutes = new GameRoutes {
    lazy val Games = resources.Games.pathMatchDecoder
    lazy val GameById = resources.Games.byId.pathCodec
    lazy val Player = resources.Games.player.pathCodec
    lazy val Team = resources.Games.team.pathCodec
  }
  
  @Singleton def competitionRoutes(resources:Resources):CompetitionRoutes = new CompetitionRoutes {
    lazy val Competitions = resources.Competitions.pathMatchDecoder
    lazy val CompetitionById = resources.Competitions.byId.pathCodec
    lazy val PlayerCompetitionById = resources.Competitions.player.pathCodec
    lazy val AcceptCompetition = resources.Competitions.accept.pathCodec
    lazy val DeclineCompetition = resources.Competitions.decline.pathCodec
    lazy val CreateCompetition = resources.Competitions.create.pathCodec
  }

  @Singleton def siteMapRoutes(resources:Resources):SiteMapRoutes = new SiteMapRoutes {
    def SiteMap:PathMatchDecoder = resources.SiteMap.pathMatchDecoder 
  }
}