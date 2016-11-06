package com.github.gdefacci.briscola.presentation

import org.obl.raz._
import org.obl.raz.Path.SegmentsPath
import org.obl.raz.ext._
import PathConverter.{ Segment, Param, Fragment }
import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.game.GameId
import com.github.gdefacci.briscola.competition.CompetitionId

trait RoutesServletConfig {
  
  def players:SegmentsPath 
  def games:SegmentsPath 
  def competitions:SegmentsPath 
  def siteMap:SegmentsPath 
  def webSocket:SegmentsPath 
  
}

object RoutesServletConfig extends RoutesServletConfig {
  val players = Path / "players"
  val games = Path / "games"
  val competitions = Path / "competitions"
  val siteMap = Path / "site-map"
  val webSocket = Path / "ws"
}

class Resources(authority: Authority, applicationPath: SegmentsPath, val servletConfig:RoutesServletConfig) {

  val playerIdSegment = Segment.long.map(PlayerId(_)).contramap((id: PlayerId) => id.id)

  object WebSockets {

    object Players extends WebSocketResource(WS(authority), applicationPath, servletConfig.webSocket) {

      lazy val byId = this / "players" / playerIdSegment

    }

  }

  val prefix = HTTP(authority) append applicationPath

  object Players extends ServletResource(prefix, servletConfig.players) {

    val byId = this / playerIdSegment

    val login = this / "login"
  }

  object Games extends ServletResource(prefix, servletConfig.games) {

    private val gameIdSegment = Segment.long.map(GameId(_)).contramap((id: GameId) => id.id)

    val byId = this / gameIdSegment

    val player = byId / "player" / playerIdSegment

    val team = byId / "team" / Segment.string

  }

  object Competitions extends ServletResource(prefix, servletConfig.competitions) {

    private val competitionIdSegment = Segment.long.map(CompetitionId(_)).contramap((id: CompetitionId) => id.id)

    val byId = this / competitionIdSegment

    val player = byId / "player" / playerIdSegment

    val accept = player / "accept"

    val decline = player / "decline"

    val create = this / "player" / playerIdSegment

  }

  object SiteMap extends ServletResource(prefix, servletConfig.siteMap) {

    val players = Players

  }
}