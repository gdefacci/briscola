package com.github.gdefacci.briscola.game

import com.github.gdefacci.briscola.player._

sealed trait BriscolaCommand 

final case class StartGame(players:GamePlayers) extends BriscolaCommand
final case class PlayCard(playerId:PlayerId, card:Card) extends BriscolaCommand
final case class PlayerDropGame(player:PlayerId, reason:Option[String]) extends BriscolaCommand

 