package org.obl.briscola.web

import org.obl.raz._
import org.obl.raz.PathConverter._
import org.obl.raz.ext.ResourceHolder
import org.obl.briscola.player.PlayerId
import org.obl.briscola.GameId
import org.obl.briscola.competition.CompetitionId

class Resources(val root:BasePath[BasePosition, SegmentPosition]) extends ResourceHolder {

  val playerIdSegment = Segment.long.map(PlayerId(_)).contramap( (id:PlayerId) => id.id)
  
  object Players extends BaseResource("players") {
    
    val byId = this / playerIdSegment
    val websocketById = byId / "websocket"
    
  }
  
  object Games extends BaseResource("games") {
    
    val gameIdSegment = Segment.long.map(GameId(_)).contramap( (id:GameId) => id.id)
    
    val byId = this / gameIdSegment
    
    val player = byId / "player" / playerIdSegment
    
  }
  
  object Competitions extends BaseResource("competitions") {
    
    val competitionIdSegment = Segment.long.map(CompetitionId(_)).contramap( (id:CompetitionId) => id.id)
    
    val byId = this / competitionIdSegment
    
    private val player = byId / "player" / playerIdSegment
    
    val accept = player / "accept"
    
    val decline = player / "decline"
    
    val create = this / "player" / playerIdSegment
    
  }

  

}