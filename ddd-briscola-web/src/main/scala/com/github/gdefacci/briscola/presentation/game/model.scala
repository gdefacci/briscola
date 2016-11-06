package com.github.gdefacci.briscola.presentation.game

import org.obl.raz.Path
import com.github.gdefacci.briscola.game.Seed
import com.github.gdefacci.briscola.presentation.ADT

final case class Card(number: Int, seed: Seed, points: Int)

final case class Score(cards: Set[Card])

object GameStateKind extends Enumeration {
  val empty, active, dropped, finished = Value
}

final case class Move(player: Path, card: Card)

final case class PlayerState(player:Path, cards: Set[Card], score: Score)
final case class PlayerFinalState(player:Path, points:Int, score: Score)

sealed trait GameState extends ADT[GameStateKind.type] {
  def kind: GameStateKind.Value
}

object DropReasonKind extends Enumeration {
  val playerLeft = Value
}

sealed trait DropReason extends ADT[DropReasonKind.type] {
  def kind: DropReasonKind.Value
}

final case class PlayerLeft(player: Path, reason: Option[String]) extends DropReason {
  val kind = DropReasonKind.playerLeft
}

final case class ActiveGameState(self: Path, briscolaCard: Card, teams:Option[Seq[Path]], moves: Seq[Move], nextPlayers: Seq[Path], currentPlayer: Path,
    isLastHandTurn: Boolean, isLastGameTurn: Boolean, players: Set[Path], playerState: Option[Path], deckCardsNumber: Int) extends GameState {
  def kind = GameStateKind.active
}
final case class DroppedGameState(self: Path, briscolaCard: Card, teams:Option[Seq[Path]], moves: Seq[Move], nextPlayers: Seq[Path], dropReason: DropReason) extends GameState {
  def kind = GameStateKind.dropped
}

sealed trait GameResult
final case class PlayersGameResult(playersOrderByPoints: Seq[PlayerFinalState], winner: PlayerFinalState) extends GameResult
final case class TeamsGameResult(teamsOrderByPoints: Seq[TeamScore], winnerTeam: TeamScore) extends GameResult
final case class TeamScore(teamName:String, players:Set[Path], cards:Set[Card], points:Int)

final case class FinalGameState(self: Path, briscolaCard: Card, gameResult:GameResult) extends GameState {
  def kind = GameStateKind.finished
}
case object EmptyGameState extends GameState {
  def kind = GameStateKind.empty
}
