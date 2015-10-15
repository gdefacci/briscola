package org.obl.briscola

import org.obl.ddd._
import org.obl.briscola.player._

sealed trait BriscolaEvent extends Event

final case class GameStarted(game: ActiveGameState) extends BriscolaEvent
final case class CardPlayed(playerId: PlayerId, card: Card) extends BriscolaEvent
final case class GameDropped(reason:DropReason) extends BriscolaEvent
  
