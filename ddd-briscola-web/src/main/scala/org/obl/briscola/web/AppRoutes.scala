package org.obl.briscola.web

import org.obl.raz.BasePath
import org.obl.raz.BasePosition
import org.obl.raz.SegmentPosition
import org.obl.raz.Path
import org.obl.raz.PathConverter
import org.obl.raz.PathBase
import org.obl.raz.PathSg
import org.obl.raz.PathCodec
import org.obl.briscola.web.util.ServletRoutes

trait AppRoutes {
  def playerRoutes:PlayerRoutes
  def gameRoutes:GameRoutes
  def competitionRoutes:CompetitionRoutes
  def siteMapRoutes:SiteMapRoutes
  def playerWebSocketRoutes:PlayerWebSocketRoutes
}

object AppRoutesImpl {
  
  def apply(config:RoutesServletConfig) = new AppRoutesImpl(config)
  
}

trait SiteMapRoutes extends ServletRoutes {
  def SiteMap:Path
}

trait RoutesServletConfig {
  def host:PathBase
  def contextPath:PathSg
  
  def playerServletPath:PathSg
  def gameServletPath:PathSg
  def competitionServletPath:PathSg
  def siteMapServletPath:PathSg
  
}

class AppRoutesImpl(val config:RoutesServletConfig) extends AppRoutes {

  trait BaseRoutes {
    val host:PathBase = config.host
    val contextPath:PathSg = config.contextPath
  }
  
  val playerWebSocketRoutes = new PlayerWebSocketRoutes with BaseRoutes {
    private val playerById      = resources.WebSockets.Players.byId
    
    val PlayerById              = playerById.toPathConverter.encodersWrap.decoderWrap
    val playerByIdUriTemplate   = playerById.toUriTemplate("playerId")
  }
  
  val playerRoutes = new PlayerRoutes with BaseRoutes {
    val servletPath             = config.playerServletPath
    
    val Players                 = resources.Players.encodersWrap
    val PlayerLogin             = resources.Players.login.encodersWrap
    val PlayerById              = resources.Players.byId.toPathCodec.encodersWrap
  }
  
  val gameRoutes = new GameRoutes with BaseRoutes {
    val servletPath             = config.gameServletPath
    
    val Games                   = resources.Games.encodersWrap
    val GameById                = resources.Games.byId.toPathCodec.encodersWrap
    val Player                  = resources.Games.player.toPathCodec.encodersWrap
  }
  
  val competitionRoutes = new CompetitionRoutes with BaseRoutes {
    val servletPath             = config.competitionServletPath
    
    val Competitions            = resources.Competitions.encodersWrap
    val CompetitionById         = resources.Competitions.byId.toPathCodec.encodersWrap
    val PlayerCompetitionById   = resources.Competitions.player.toPathCodec.encodersWrap
    val AcceptCompetition       = resources.Competitions.accept.toPathCodec.encodersWrap
    val DeclineCompetition      = resources.Competitions.decline.toPathCodec.encodersWrap
    val CreateCompetition       = resources.Competitions.create.toPathCodec.encodersWrap
  }
 
  val siteMapRoutes = new SiteMapRoutes with BaseRoutes {
    val servletPath             = config.siteMapServletPath
    
    val SiteMap                 = resources.SiteMap.encodersWrap
  }
}