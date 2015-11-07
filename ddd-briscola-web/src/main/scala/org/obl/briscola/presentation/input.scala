package org.obl.briscola.presentation

import org.obl.briscola.player.PlayerId

object Input {

  import org.obl.briscola.competition

  final case class Competition(players: Seq[PlayerId], kind: competition.MatchKind, deadline: competition.CompetitionStartDeadline)
  final case class Player(name: String, password: String)

}
 