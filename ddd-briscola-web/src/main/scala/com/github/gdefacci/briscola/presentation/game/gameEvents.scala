package com.github.gdefacci.briscola.presentation.game

import org.obl.raz.Path
import com.github.gdefacci.briscola.presentation.ADT

object BriscolaEventKind extends Enumeration {
  val gameStarted, cardPlayed, gameDropped = Value
}
sealed trait BriscolaEvent extends ADT[BriscolaEventKind.type] {
  def kind: BriscolaEventKind.Value
}
final case class GameStarted(game: ActiveGameState) extends BriscolaEvent {
  lazy val kind = BriscolaEventKind.gameStarted
}
final case class CardPlayed(game: Path, player: Path, card: Card) extends BriscolaEvent {
  lazy val kind = BriscolaEventKind.cardPlayed
}
final case class GameDropped(game: Path, reason: DropReason) extends BriscolaEvent {
  lazy val kind = BriscolaEventKind.gameDropped
}
