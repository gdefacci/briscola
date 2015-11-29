package org.obl.briscola.presentation

import org.obl.raz.Path

object PlayerEventKind extends Enumeration {
  val playerLogOn, playerLogOff = Value
}

sealed trait PlayerEvent extends ADT[PlayerEventKind.type] {
  def kind: PlayerEventKind.Value
}
final case class PlayerLogOn(player: Path) extends PlayerEvent {
  lazy val kind = PlayerEventKind.playerLogOn
}
final case class PlayerLogOff(player: Path) extends PlayerEvent {
  lazy val kind = PlayerEventKind.playerLogOn
}
  