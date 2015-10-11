package org.obl.briscola

import org.obl.ddd._
import org.obl.briscola.player._

object Seed extends Enumeration {
  val bastoni, coppe, denari, spade = Value
}

case class Card(number: Byte, seed: Seed.Value)  {
  
  def points = {
    number match {
      case 1 => 11
      case 3 => 10
      case 8 => 2
      case 9 => 3
      case 10 => 4
      case _ => 0
    }
  }
  
}

object Deck {
  
  lazy val empty = new Deck(Seq.empty)

  
  def initial = {
    val cards = for (seed <- Seed.values; number <- 1.to(10)) yield (Card(number.toByte, seed))
    Deck(new util.Random().shuffle(cards.toSeq))
  }

}

case class Deck(cards: Seq[Card]) {

  lazy val isEmpty = cards.isEmpty
  
  def takeCards(n:Int):(Set[Card], Deck) = {
    val (crds, deck) = cards.splitAt(n)
    crds.toSet -> Deck(deck)
  }
  
  def briscolaCard(numberOfPlayers:Int):Card = 
    cards.last    
  
}

object GameState {
  
  lazy val empty = EmptyGameState 
  
  val MIN_PLAYERS = 2
  val MAX_PLAYERS = 8
}

sealed trait GameState extends State

case class GameId(id:Long)

case object EmptyGameState extends GameState 
case class ActiveGameState(id:GameId, briscolaCard:Card, deck: Deck, moves: Seq[Move], nextPlayers: Seq[PlayerState]) extends GameState {
  
  assert(nextPlayers.nonEmpty)

  lazy val currentPlayer:PlayerState = nextPlayers.head

  lazy val isLastHandTurn = nextPlayers.length == 1
  
  lazy val isLastGameTurn = isLastHandTurn && deck.isEmpty && nextPlayers.head.cards.size == 1
  
  lazy val players:Seq[PlayerState] = moves.map(_.player) ++ nextPlayers
  
  lazy val deckCardsNumber = deck.cards.length
  
}

sealed trait DropReason
case class PlayerLeft(player:PlayerId, reason:Option[String]) extends DropReason

case class DroppedGameState(id:GameId, briscolaCard:Card, deck: Deck, moves: Seq[Move], nextPlayers: Seq[PlayerState], dropReason:DropReason) extends GameState 

case class FinalGameState(id:GameId, briscolaCard:Card, players:Seq[PlayerFinalState]) extends GameState {
  
  lazy val playersOrderByPoints = players.toSeq.sortWith { (ps1, ps2) =>
    ps1.points > ps2.points || (ps1.points == ps2.points && ps1.score.size > ps2.score.size)
  }
  
  lazy val winner = playersOrderByPoints.head
  
} 

case class Move(player: PlayerState, card: Card)

case class PlayerState(id: PlayerId, cards: Set[Card], score: Set[Card])
case class PlayerFinalState(id: PlayerId, points:Int, score: Set[Card])
