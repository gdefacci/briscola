package com.github.gdefacci.briscola.presentation.competition

import com.github.gdefacci.briscola.presentation.ADT
import org.obl.raz.Path

object CompetitionEventKind extends Enumeration {
  val createdCompetition, confirmedCompetition, playerAccepted, playerDeclined = Value
}
sealed trait CompetitionEvent extends ADT[CompetitionEventKind.type] {
  def kind: CompetitionEventKind.Value
}
final case class CreatedCompetition(issuer: Path, competition: Path) extends CompetitionEvent {
  lazy val kind = CompetitionEventKind.createdCompetition
}
final case class CompetitionAccepted(player: Path, competition: Path) extends CompetitionEvent {
  lazy val kind = CompetitionEventKind.playerAccepted
}
final case class CompetitionDeclined(player: Path, competition: Path, reason: Option[String]) extends CompetitionEvent {
  lazy val kind = CompetitionEventKind.playerDeclined
}
