package org.obl.briscola.player

import org.obl.ddd._

final case class PlayerId(id: Long)
final case class Player(id: PlayerId, name: String, password:String)

case class TeamId(id:Long)
final case class Team(id:TeamId, name:String, players:Seq[PlayerId])
final case class Teams(teams:Seq[Team]) 

sealed trait GamePlayers
final case class Players(players:Set[PlayerId]) extends GamePlayers
final case class TeamPlayers(players:Set[TeamPlayer], teams:Set[TeamInfos]) extends GamePlayers

object GamePlayers {
  
  def getPlayers(gamePlayers:GamePlayers):Set[PlayerId] = {
    gamePlayers match {
      case Players(players) => players
      case _ => ???
    }
  }
  
  def filterPlayersByPlayerId(gamePlayers:GamePlayers, predicate:PlayerId => Boolean):GamePlayers = {
    gamePlayers match {
      case Players(players) => Players(players.filter(predicate))
      case _ => ???
    }
  }
  
}


final case class TeamInfos(name:String)
final case class TeamPlayer(player:PlayerId, teamName:String) 