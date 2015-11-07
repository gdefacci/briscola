package org.obl.briscola

import org.obl.ddd._
import org.obl.briscola.player._

object Seed extends Enumeration {
  val bastoni, coppe, denari, spade = Value
}

final case class Card(number: Byte, seed: Seed.Value)  {
  
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

final case class Deck(cards: Seq[Card]) {

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
  
  def id(gm: GameState):Option[GameId] = gm match {
    case EmptyGameState => None
    case gm: ActiveGameState => Some(gm.id)
    case gm: FinalGameState => Some(gm.id)
    case gm: DroppedGameState => Some(gm.id)
  }
}

sealed trait GameState extends State

final case class GameId(id:Long)

case class GameTeams(teams:Seq[TeamGameState])
case class TeamGameState(team:TeamId, score:Set[Card])

case object EmptyGameState extends GameState

sealed trait GameStateTeamMixin {
  def teams:Option[Teams]
  
  def teamById(teamId:TeamId) = teams.flatMap( _.teams.find( team => team.id == teamId) )
}

final case class ActiveGameState(id:GameId, briscolaCard:Card, deck: Deck, moves: Seq[Move], nextPlayers: Seq[PlayerState], teams:Option[Teams]) 
  extends GameState with GameStateTeamMixin {
  
  assert(nextPlayers.nonEmpty)

  lazy val currentPlayer:PlayerState = nextPlayers.head

  lazy val isLastHandTurn = nextPlayers.length == 1
  
  lazy val isLastGameTurn = isLastHandTurn && deck.isEmpty && nextPlayers.head.cards.size == 1
  
  lazy val players:Set[PlayerState] = moves.map(_.player).toSet ++ nextPlayers
  
  lazy val deckCardsNumber = deck.cards.length
  
}

sealed trait DropReason
final case class PlayerLeft(player:PlayerId, reason:Option[String]) extends DropReason

final case class DroppedGameState(id:GameId, briscolaCard:Card, deck: Deck, moves: Seq[Move], nextPlayers: Seq[PlayerState], dropReason:DropReason, teams:Option[Teams]) 
  extends GameState with GameStateTeamMixin 

final case class FinalGameState(id:GameId, briscolaCard:Card, players:Seq[PlayerFinalState], teams:Option[Teams]) extends GameState with GameStateTeamMixin {
  
  lazy val playersOrderByPoints = players.sortBy(_.score) 
  
  lazy val winner = playersOrderByPoints.head
  
} 

final case class PlayerState(id: PlayerId, cards: Set[Card], score: Score)
final case class PlayerFinalState(id: PlayerId, points:Int, score: Score)

final case class Move(player: PlayerState, card: Card)

private object comparison {
	val LT = -1
	val EQ = 0
  val GT = 1
} 

final case class Score(cards:Set[Card]) extends Ordered[Score] {
  lazy val points = cards.map(_.points).sum
  
  lazy val numberOfCards = cards.size
  
  def add(score:Score):Score = add(score.cards)
  def add(cards:Iterable[Card]) = Score(this.cards ++ cards)
  
  def compare(that: Score): Int = 
    if (points > that.points) comparison.LT
    else if (points == that.points) {
      if (numberOfCards > that.numberOfCards) comparison.LT
      else if (numberOfCards < that.numberOfCards) comparison.GT
      else comparison.EQ
    } else comparison.GT
  
} 

object Score {
  val empty = Score(Set.empty)
}