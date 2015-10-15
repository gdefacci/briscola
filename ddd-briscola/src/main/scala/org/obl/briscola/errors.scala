package org.obl.briscola

import org.obl.ddd._
import org.obl.briscola.player._

sealed trait BriscolaError extends DomainError

case object GameNotStarted extends BriscolaError 
case object GameAlreadyStarted extends BriscolaError 
case object GameAlreadyFinished extends BriscolaError
case object GameAlreadyDropped extends BriscolaError

final case class InvalidTurn(player:PlayerId, currentGamePlayer:PlayerId) extends BriscolaError
final case class TooManyPlayers(players:Set[PlayerId], maxPlayerNumber:Int) extends BriscolaError
final case class TooFewPlayers(players:Set[PlayerId], minPlayerNumber:Int) extends BriscolaError
final case class PlayersDoNotExist(players:Set[PlayerId]) extends BriscolaError 
final case class PlayerDoesNotOwnCard(id:PlayerId, card:Card, ownedCards:Set[Card]) extends BriscolaError
final case class InvalidPlayer(id:PlayerId) extends BriscolaError

