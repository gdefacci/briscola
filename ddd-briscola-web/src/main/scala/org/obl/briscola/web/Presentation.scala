package org.obl.briscola.web

import org.obl.raz.Path
import org.obl.briscola.Card
import org.obl.briscola.Seed
import org.obl.briscola.competition.MatchKind
import org.obl.briscola.competition.CompetitionStartDeadline
import org.obl.briscola.player.PlayerId

object Presentation {

  case class Player(self:Path, name:String, webSocket:Path, createCompetition:Path)
  case class PlayerState(player:Path, cards: Set[Card], score: Set[Card])
  case class PlayerFinalState(player:Path, points:Int, score: Set[Card])
  
  object GameStateKind extends Enumeration {
    val empty, active, finished = Value
  }
  
  case class Move(player:Path, card:Card)
  
  sealed trait GameState {
    def gameStateKind:GameStateKind.Value
  }
  
  case class ActiveGameState(self:Path, gameSeed:Seed.Value, moves:Seq[Move], nextPlayers:Seq[Path], currentPlayer:Path, 
      isLastHandTurn:Boolean, isLastGameTurn:Boolean, players:Seq[Path], playerState:Option[Path]) extends GameState {
    def gameStateKind = GameStateKind.active
  }
  case class FinalGameState(self:Path, gameSeed:Seed.Value, playersOrderByPoints:Seq[PlayerFinalState], winner:PlayerFinalState) extends GameState {
    def gameStateKind = GameStateKind.finished
  }
  case object EmptyGameState extends GameState {
    def gameStateKind = GameStateKind.empty
  }
  
  
  object BriscolaEventKind extends Enumeration {
    val gameStarted, cardPlayed = Value
  }
  sealed trait BriscolaEvent {
    def kind:BriscolaEventKind.Value
  }
  case class GameStarted(game:ActiveGameState) extends BriscolaEvent {
    lazy val kind = BriscolaEventKind.gameStarted
  }
  case class CardPlayed(game:Path, player:Path, card:Card) extends BriscolaEvent {
    lazy val kind = BriscolaEventKind.gameStarted
  }
  
  object CompetitionStateKind extends Enumeration {
    val empty, open, dropped, fullfilled = Value
  }
  
  case class Competition(players:Set[Path], kind:MatchKind, deadlineKind:CompetitionStartDeadline)
  case class CompetitionState(self:Option[Path], 
      competition:Option[Competition], kind:CompetitionStateKind.Value, 
      acceptingPlayers:Set[Path], decliningPlayers:Set[Path], 
      accept:Option[Path], decline:Option[Path])
  
  object CompetitionEventKind extends Enumeration {
    val createdCompetition, confirmedCompetition, playerAccepted, playerDeclined = Value
  }
  sealed trait CompetitionEvent {
    def kind:CompetitionEventKind.Value
  }
  case class CreatedCompetition(issuer:Path, competition:Path) extends CompetitionEvent {
    lazy val kind = CompetitionEventKind.createdCompetition
  } 
  case class ConfirmedCompetition(competition:Path) extends CompetitionEvent {
    lazy val kind = CompetitionEventKind.confirmedCompetition
  }
  case class CompetitionAccepted(player:Path, competition:Path) extends CompetitionEvent {
    lazy val kind = CompetitionEventKind.playerAccepted
  }
  case class CompetitionDeclined(player:Path, competition:Path, reason:Option[String]) extends CompetitionEvent {
    lazy val kind = CompetitionEventKind.playerDeclined
  }
      
  case class EventAndState[E,S](event:E, state:S)    

  object Input {

    case class Competition(players:Set[PlayerId], kind:MatchKind, deadlineKind:CompetitionStartDeadline)
    case class Player(name:String)
//    case class Collection[T](members:Seq[T])
    
    
  }
  
}