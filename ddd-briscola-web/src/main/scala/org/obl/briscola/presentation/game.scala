package org.obl.briscola.presentation

import org.obl.raz.Path
import org.obl.briscola.Seed
import org.obl.briscola.player.PlayerId

final case class Card(number: Byte, seed: Seed.Value, points: Int)

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

final case class ActiveGameState(self: Path, briscolaCard: Card, moves: Seq[Move], nextPlayers: Seq[Path], currentPlayer: Path,
    isLastHandTurn: Boolean, isLastGameTurn: Boolean, players: Set[Path], playerState: Option[Path], deckCardsNumber: Int) extends GameState {
  def kind = GameStateKind.active
}
final case class DroppedGameState(self: Path, briscolaCard: Card, moves: Seq[Move], nextPlayers: Seq[Path], dropReason: DropReason) extends GameState {
  def kind = GameStateKind.dropped
}
final case class FinalGameState(self: Path, briscolaCard: Card, playersOrderByPoints: Seq[PlayerFinalState], winner: PlayerFinalState) extends GameState {
  def kind = GameStateKind.finished
}
case object EmptyGameState extends GameState {
  def kind = GameStateKind.empty
}
