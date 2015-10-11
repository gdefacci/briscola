package org.obl.briscola

import org.obl.ddd._
import org.obl.briscola.player._

sealed trait BriscolaCommand extends Command

case class StartGame(players:Set[PlayerId]) extends BriscolaCommand
case class PlayCard(playerId:PlayerId, card:Card) extends BriscolaCommand
case class PlayerDropGame(player:PlayerId, reason:Option[String]) extends BriscolaCommand
