package org.obl.briscola.presentation

//import org.obl.briscola.player.PlayerId
import org.obl.briscola.player.GamePlayers

object Input {

  import org.obl.briscola.competition

  final case class Competition(players: GamePlayers, kind: competition.MatchKind, deadline: competition.CompetitionStartDeadline)
  final case class Player(name: String, password: String)

//  sealed trait GamePlayers
//  case class Players(players: Set[PlayerId]) extends GamePlayers
//  case class TeamPlayers(players: Set[TeamPlayer], teams:Set[Team]) extends GamePlayers
//  
//  case class TeamPlayer(player:PlayerId, teamName:String)
//  case class Team(name:String)
  
}
 