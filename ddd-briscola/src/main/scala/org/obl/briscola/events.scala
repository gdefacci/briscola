package org.obl.briscola

import org.obl.ddd._
import org.obl.briscola.player._

//object Events {

//  sealed trait PlayerEvent extends Event
//  case class PlayerCreated(player:Player) extends PlayerEvent
  
  sealed trait BriscolaEvent extends Event
  
  case class GameStarted(game:ActiveGameState) extends BriscolaEvent
  case class CardPlayed(gameId:GameId, playerId:PlayerId, card:Card) extends BriscolaEvent
  
//  case class HandFinished(game:ActiveGameState) extends BriscolaEvent
//  case class GameFinished(gameId:GameId, winner:PlayerFinalState, otherPlayers:Seq[PlayerFinalState]) extends BriscolaEvent
  
//}