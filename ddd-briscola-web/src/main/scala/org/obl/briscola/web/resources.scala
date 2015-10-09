package org.obl.briscola.web

import scalaz.{-\/}
import org.obl.raz._
import org.obl.raz.PathConverter._
import org.obl.raz.ext.ResourceHolder
import org.obl.briscola.player.PlayerId
import org.obl.briscola.GameId
import org.obl.briscola.competition.CompetitionId
import org.obl.raz.ext.BaseResource
import org.obl.raz.exceptions.PathExpectationException

object resources {

  private[resources] val playerIdSegment = Segment.long.map(PlayerId(_)).contramap((id: PlayerId) => id.id)

  private object WebSockets extends BaseResource("ws") {

    object Players extends BaseResource(prefix :+ "players") {

      lazy val byId = this / playerIdSegment 

    }
  }
  
  def playerWebSocket(host:PathBase, contextPath:PathSg):PathConverter[PlayerId, PlayerId, String, SegmentPosition, SegmentPosition] = 
    PathConverter.encodersAt(host,
      PathConverter.prependEncoders(contextPath, WebSockets.Players.byId.toPathConverter))
  
  def playerWebSocketUriTemplate:UriTemplate = WebSockets.Players.byId.toPathConverter.toUriTemplate("playerId")
  
  def playerWebSocketPathDecoder(contextPath:PathSg):PathDecoder[PlayerId] =
    PathDecoder.prepend(contextPath, WebSockets.Players.byId)
  
  object Players extends BaseResource {

    val byId = this / playerIdSegment

    val login = this / "login"
  }

  object Games extends BaseResource {

    private val gameIdSegment = Segment.long.map(GameId(_)).contramap((id: GameId) => id.id)

    val byId = this / gameIdSegment

    val player = byId / "player" / playerIdSegment

  }

  object Competitions extends BaseResource {

    private val competitionIdSegment = Segment.long.map(CompetitionId(_)).contramap((id: CompetitionId) => id.id)

    val byId = this / competitionIdSegment

    val player = byId / "player" / playerIdSegment

    val accept = player / "accept"

    val decline = player / "decline"

    val create = this / "player" / playerIdSegment

  }

  object SiteMap extends BaseResource {
    
    val players = Players
    
  }
}