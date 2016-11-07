package com.github.gdefacci.briscola.presentation

import org.obl.raz._
import org.obl.raz.ext._
import PathConverter.{ Segment, Param, Fragment }
import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.game.GameId
import com.github.gdefacci.briscola.competition.CompetitionId

class Resources(authority: Authority, applicationPath: TPath[PathPosition.Segment, PathPosition.Segment], routesConfig:RoutesServletConfig) {

  val playerIdSegment = Segment.long.map(PlayerId(_)).contramap((id: PlayerId) => id.id)

  object WebSockets {

    object Players extends WebSocketResource(WS(authority), applicationPath, routesConfig.webSocket) {

      lazy val byId = this / "players" / playerIdSegment

    }

  }

  val prefix = HTTP(authority) append applicationPath

  object Players extends ServletResource(prefix, routesConfig.players) {

    val byId = this / playerIdSegment

    val login = this / "login"
  }

  object Games extends ServletResource(prefix, routesConfig.games) {

    private val gameIdSegment = Segment.long.map(GameId(_)).contramap((id: GameId) => id.id)

    val byId = this / gameIdSegment

    val player = byId / "player" / playerIdSegment

    val team = byId / "team" / Segment.string

  }

  object Competitions extends ServletResource(prefix, routesConfig.competitions) {

    private val competitionIdSegment = Segment.long.map(CompetitionId(_)).contramap((id: CompetitionId) => id.id)

    val byId = this / competitionIdSegment

    val player = byId / "player" / playerIdSegment

    val accept = player / "accept"

    val decline = player / "decline"

    val create = this / "player" / playerIdSegment

  }

  object SiteMap extends ServletResource(prefix,routesConfig.siteMap) {

    val players = Players

  }
}