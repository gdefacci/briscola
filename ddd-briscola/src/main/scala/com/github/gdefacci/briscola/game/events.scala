package com.github.gdefacci.briscola.game

import com.github.gdefacci.briscola.Event

import com.github.gdefacci.briscola.player._

sealed trait BriscolaEvent extends Event

final case class GameStarted(game: ActiveGameState) extends BriscolaEvent
final case class CardPlayed(playerId: PlayerId, card: Card) extends BriscolaEvent
final case class GameDropped(reason:DropReason) extends BriscolaEvent