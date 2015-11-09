package org.obl.briscola.player

import org.obl.ddd._

final case class PlayerId(id: Long)
final case class Player(id: PlayerId, name: String, password:String)

sealed trait GamePlayers
final case class Players(players: Set[PlayerId]) extends GamePlayers
final case class TeamPlayers(players: Set[TeamPlayer], teams:Set[TeamInfo]) extends GamePlayers

final case class TeamPlayer(player:PlayerId, teamName:String)
final case class TeamInfo(name:String)

final case class Team(name:String, players:Set[PlayerId])
final case class Teams(teams:Seq[Team]) 

object GamePlayers {
  
  def getPlayers(gamePlayers:GamePlayers):Set[PlayerId] = {
    gamePlayers match {
      case Players(players) => players
      case TeamPlayers(tpls,_) => tpls.map(_.player)
    }
  }
  
  def filterPlayersByPlayerId(gamePlayers:GamePlayers, predicate:PlayerId => Boolean):GamePlayers = {
    gamePlayers match {
      case Players(players) => Players(players.filter(predicate))
      case TeamPlayers(tpls,teams) => TeamPlayers(tpls.filter(tpl => predicate(tpl.player)), teams)
    }
  }
 
  def teams(gamePlayers:GamePlayers):Option[Teams] = {
    gamePlayers match {
      case Players(players) => None
      case TeamPlayers(tplayers, tinfos) => 
        val teamsMap = tplayers.foldLeft(Map.empty[String, Team]) { (acc, tpl) =>
          val v = acc.get(tpl.teamName) match {
            case Some(team) => team.copy(players = (team.players + tpl.player))
            case None => Team(tpl.teamName, Set(tpl.player))
          }
          acc + (tpl.teamName -> v)
        }
        Some(Teams(teamsMap.values.toSeq)) 
    }
  }
  
}


 