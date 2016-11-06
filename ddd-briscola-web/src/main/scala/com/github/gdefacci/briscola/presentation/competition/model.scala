package com.github.gdefacci.briscola.presentation.competition

import com.github.gdefacci.briscola.presentation.ADT

import org.obl.raz.Path

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
final case class NumberOfGamesMatchKind(numberOfMatches: Int) extends MatchKind {
  val kind = MatchKindKind.numberOfGamesMatchKind
}
final case class TargetPointsMatchKind(winnerPoints: Int) extends MatchKind {
  val kind = MatchKindKind.targetPointsMatchKind
}

object CompetitionStartDeadlineKind extends Enumeration {
  val allPlayers, onPlayerCount = Value
}

sealed trait CompetitionStartDeadline extends ADT[CompetitionStartDeadlineKind.type]
case object AllPlayers extends CompetitionStartDeadline {
  val kind = CompetitionStartDeadlineKind.allPlayers
}

final case class OnPlayerCount(count: Int) extends CompetitionStartDeadline {
  val kind = CompetitionStartDeadlineKind.onPlayerCount
}

final case class Competition(players: Set[Path], kind: MatchKind, deadline: CompetitionStartDeadline)

final case class CompetitionState(self: Path,
  competition: Option[Competition], kind: CompetitionStateKind.Value,
  acceptingPlayers: Set[Path], decliningPlayers: Set[Path],
  accept: Option[Path], decline: Option[Path]) extends ADT[CompetitionStateKind.type]
