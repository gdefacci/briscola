package com.github.gdefacci.briscola.presentation.competition

import org.obl.raz.Path

object Input {

  import com.github.gdefacci.briscola.competition.MatchKind
  import com.github.gdefacci.briscola.competition.CompetitionStartDeadline

  final case class Competition(players: GamePlayers, kind: MatchKind, deadline: CompetitionStartDeadline)

  sealed trait GamePlayers
  final case class Players(players: Set[Path]) extends GamePlayers
  final case class TeamPlayers(players: Set[TeamPlayer], teams:Set[TeamInfo]) extends GamePlayers
  
  final case class TeamPlayer(player:Path, teamName:String)
  final case class TeamInfo(name:String)
  
  final case class Team(name:String, players:Set[Path])
  final case class Teams(teams:Seq[Team]) 
  
}
 