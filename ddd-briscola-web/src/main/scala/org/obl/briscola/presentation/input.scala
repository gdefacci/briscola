package org.obl.briscola.presentation

import org.obl.briscola.player.GamePlayers
import org.obl.raz.Path

object Input {

  import org.obl.briscola.competition.MatchKind
  import org.obl.briscola.competition.CompetitionStartDeadline

  final case class Competition(players: GamePlayers, kind: MatchKind, deadline: CompetitionStartDeadline)
  final case class Player(name: String, password: String)

  /*
   * actually not used must: replace uses of corresponding models from org.obl.briscola.player
   */
//  sealed trait GamePlayers
//  final case class Players(players: Set[Path]) extends GamePlayers
//  final case class TeamPlayers(players: Set[TeamPlayer], teams:Set[TeamInfo]) extends GamePlayers
//  
//  final case class TeamPlayer(player:Path, teamName:String)
//  final case class TeamInfo(name:String)
//  
//  final case class Team(name:String, players:Set[Path])
//  final case class Teams(teams:Seq[Team]) 

  
}
 