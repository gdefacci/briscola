package org.obl.briscola

import org.obl.ddd._
import org.obl.briscola.player._

sealed trait BriscolaEvent extends Event

case class GameStarted(game: ActiveGameState) extends BriscolaEvent
case class CardPlayed(playerId: PlayerId, card: Card) extends BriscolaEvent
case class GameDropped(reason:DropReason) extends BriscolaEvent
  
