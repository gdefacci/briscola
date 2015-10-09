package org.obl.briscola

import org.obl.ddd._
import org.obl.briscola.player._

sealed trait BriscolaError extends DomainError

case object GameNotStarted extends BriscolaError 
case object GameAlreadyStarted extends BriscolaError 
case object GameAlreadyFinished extends BriscolaError
case object GameDropped extends BriscolaError

case class InvalidTurn(player:PlayerId, currentGamePlayer:PlayerId) extends BriscolaError
case class TooManyPlayers(players:Set[PlayerId], maxPlayerNumber:Int) extends BriscolaError
case class TooFewPlayers(players:Set[PlayerId], minPlayerNumber:Int) extends BriscolaError
case class PlayersDoNotExist(players:Set[PlayerId]) extends BriscolaError 
case class PlayerDoesNotOwnCard(id:PlayerId, card:Card, ownedCards:Set[Card]) extends BriscolaError
case class InvalidPlayer(id:PlayerId) extends BriscolaError

