package org.obl.briscola.web

import org.obl.briscola.GameId
import org.obl.briscola.competition.CompetitionId
import org.obl.briscola.player.PlayerId
import org.obl.raz.BasePath.toPathSegmentAdder
import org.obl.raz.HPath.toHPathSegmentAdder
import org.obl.raz.PathConverter.Segment
import org.obl.raz.ext.BaseResource

object resources {

  private[resources] val playerIdSegment = Segment.long.map(PlayerId(_)).contramap((id: PlayerId) => id.id)

  object WebSockets extends BaseResource("ws") {

    object Players extends BaseResource(prefix :+ "players") {

      lazy val byId = this / playerIdSegment 

    }
  }
  
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