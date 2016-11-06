package com.github.gdefacci.briscola.presentation.player

import org.obl.raz.Path
import com.github.gdefacci.briscola.presentation.ADT

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
  