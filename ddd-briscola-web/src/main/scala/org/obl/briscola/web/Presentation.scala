package org.obl.briscola.web

import org.obl.raz.Path
import org.obl.briscola.Seed
import org.obl.briscola.player.PlayerId

object Presentation {

  final case class Card(number: Byte, seed: Seed.Value, points:Int)
  
  final case class EventAndState[E,S](event:E, state:S)    
  final case class Collection[T](members:Iterable[T])
  
  final case class Score(cards:Set[Card])
  
  final case class Player(self:Path, name:String, webSocket:Path, createCompetition:Path)
  final case class PlayerState(player:Path, cards: Set[Card], score: Score)
  final case class PlayerFinalState(player:Path, points:Int, score: Score)

  sealed trait ADT[E <: Enumeration] {
    def kind:E#Value
  }
  
  object PlayerEventKind extends Enumeration {
    val playerLogOn, playerLogOff = Value
  }
  
  sealed trait PlayerEvent extends ADT[PlayerEventKind.type] {
    def kind:PlayerEventKind.Value
  }
  final case class PlayerLogOn(player:Path) extends PlayerEvent {
    lazy val kind = PlayerEventKind.playerLogOn
  }
  final case class PlayerLogOff(player:Path) extends PlayerEvent {
    lazy val kind = PlayerEventKind.playerLogOn
  }
  
  object GameStateKind extends Enumeration {
    val empty, active, dropped, finished = Value
  }
  
  final case class Move(player:Path, card:Card)

  sealed trait GameState extends ADT[GameStateKind.type] {
    def kind:GameStateKind.Value
  }

  object DropReasonKind extends Enumeration {
    val playerLeft = Value
  }
  
  sealed trait DropReason extends ADT[DropReasonKind.type] {
    def kind:DropReasonKind.Value
  }
  
  final case class PlayerLeft(player:Path, reason:Option[String]) extends DropReason {
    val kind = DropReasonKind.playerLeft
  }
  
  final case class ActiveGameState(self:Path, briscolaCard:Card, moves:Seq[Move], nextPlayers:Seq[Path], currentPlayer:Path, 
      isLastHandTurn:Boolean, isLastGameTurn:Boolean, players:Set[Path], playerState:Option[Path], deckCardsNumber:Int) extends GameState {
    def kind = GameStateKind.active
  }
  final case class DroppedGameState(self:Path, briscolaCard:Card, moves:Seq[Move], nextPlayers:Seq[Path], dropReason:DropReason) extends GameState {
    def kind = GameStateKind.dropped
  }
  final case class FinalGameState(self:Path, briscolaCard:Card, playersOrderByPoints:Seq[PlayerFinalState], winner:PlayerFinalState) extends GameState {
    def kind = GameStateKind.finished
  }
  case object EmptyGameState extends GameState {
    def kind = GameStateKind.empty
  }
  
  object BriscolaEventKind extends Enumeration {
    val gameStarted, cardPlayed, gameDropped = Value
  }
  sealed trait BriscolaEvent extends ADT[BriscolaEventKind.type] {
    def kind:BriscolaEventKind.Value
  }
  final case class GameStarted(game:ActiveGameState) extends BriscolaEvent {
    lazy val kind = BriscolaEventKind.gameStarted
  }
  final case class CardPlayed(game:Path, player:Path, card:Card) extends BriscolaEvent {
    lazy val kind = BriscolaEventKind.cardPlayed
  }
  final case class GameDropped(game:Path, reason:DropReason) extends BriscolaEvent {
    lazy val kind = BriscolaEventKind.gameDropped
  }
  
  object CompetitionStateKind extends Enumeration {
    val open, dropped, fullfilled = Value
  }

  object MatchKindKind extends Enumeration {
    val singleMatch, numberOfGamesMatchKind, targetPointsMatchKind = Value
  }
  
  sealed trait MatchKind extends ADT[MatchKindKind.type] 
  
  case object SingleMatch extends MatchKind {
    val kind = MatchKindKind.singleMatch
  }
  final case class NumberOfGamesMatchKind(numberOfMatches:Int) extends MatchKind {
    val kind = MatchKindKind.numberOfGamesMatchKind
  }
  final case class TargetPointsMatchKind(winnerPoints:Int) extends MatchKind {
    val kind = MatchKindKind.targetPointsMatchKind
  }
  
  object CompetitionStartDeadlineKind extends Enumeration {
    val allPlayers, onPlayerCount = Value  
  }
  
  sealed trait CompetitionStartDeadline extends ADT[CompetitionStartDeadlineKind.type]
  case object AllPlayers extends CompetitionStartDeadline {
    val kind = CompetitionStartDeadlineKind.allPlayers
  }
  
  final case class OnPlayerCount(count:Int) extends CompetitionStartDeadline {
    val kind = CompetitionStartDeadlineKind.onPlayerCount
  }

  final case class Competition(players:Set[Path], kind:MatchKind, deadline:CompetitionStartDeadline)
  
  final case class CompetitionState(self:Path, 
      competition:Option[Competition], kind:CompetitionStateKind.Value, 
      acceptingPlayers:Set[Path], decliningPlayers:Set[Path], 
      accept:Option[Path], decline:Option[Path]) extends ADT[CompetitionStateKind.type]
  
  object CompetitionEventKind extends Enumeration {
    val createdCompetition, confirmedCompetition, playerAccepted, playerDeclined = Value
  }
  sealed trait CompetitionEvent extends ADT[CompetitionEventKind.type] {
    def kind:CompetitionEventKind.Value
  }
  final case class CreatedCompetition(issuer:Path, competition:Path) extends CompetitionEvent {
    lazy val kind = CompetitionEventKind.createdCompetition
  } 
  final case class CompetitionAccepted(player:Path, competition:Path) extends CompetitionEvent {
    lazy val kind = CompetitionEventKind.playerAccepted
  }
  final case class CompetitionDeclined(player:Path, competition:Path, reason:Option[String]) extends CompetitionEvent {
    lazy val kind = CompetitionEventKind.playerDeclined
  }
      
  object Input {

    import org.obl.briscola.competition

    final case class Competition(players:Seq[PlayerId], kind:competition.MatchKind, deadline:competition.CompetitionStartDeadline)
    final case class Player(name:String, password:String)
    
  }
  
  final case class SiteMap(players:Path, playerLogin:Path) 
  
}