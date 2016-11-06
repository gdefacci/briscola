package com.github.gdefacci.briscola.player

import com.github.gdefacci.ddd._

final case class PlayerId(id: Long)
final case class Player(id: PlayerId, name: String, password:String)

sealed trait GamePlayers
final case class Players(players: Set[PlayerId]) extends GamePlayers

final case class TeamPlayers(players: Set[TeamPlayer]) extends GamePlayers {
  lazy val teamsInfos:Set[TeamInfo] = players.map(_.team)
  
  lazy val teams:Teams = Teams(players.foldLeft(Map.empty[String, Set[PlayerId]]) { (mp,teamPlayer) =>
      val teamName = teamPlayer.team.name
      val players = (mp.get(teamName) match {
        case None => Set(teamPlayer.player)
        case Some(players) => players + teamPlayer.player
      })
      mp + (teamName -> players)
  }.map {
    case (k,v) => Team(k,v)
  }.toSet)
  
}
final case class TeamPlayer(player:PlayerId, team:TeamInfo)
final case class TeamInfo(name:String)

final case class Team(name:String, players:Set[PlayerId])
final case class Teams(teams:Set[Team]) 

object GamePlayers {
  
  def getPlayers(gamePlayers:GamePlayers):Set[PlayerId] = {
    gamePlayers match {
      case Players(players) => players
      case TeamPlayers(tpls) => tpls.map(_.player)
    }
  }
  
  def filterPlayersByPlayerId(gamePlayers:GamePlayers, predicate:PlayerId => Boolean):GamePlayers = {
    gamePlayers match {
      case Players(players) => Players(players.filter(predicate))
      case TeamPlayers(tpls) => TeamPlayers(tpls.filter(tpl => predicate(tpl.player)))
    }
  }
 
  def teams(gamePlayers:GamePlayers):Option[Teams] = {
    gamePlayers match {
      case Players(players) => None
      case TeamPlayers(tplayers) => 
        val teamsMap = tplayers.foldLeft(Map.empty[String, Team]) { (acc, tpl) =>
          val teamName = tpl.team.name
          val v = acc.get(teamName) match {
            case Some(team) => team.copy(players = (team.players + tpl.player))
            case None => Team(teamName, Set(tpl.player))
          }
          acc + (teamName -> v)
        }
        Some(Teams(teamsMap.values.toSet)) 
    }
  }
  
}

object PlayersState {
  
  val empty = new PlayersState(Map.empty, Set.empty)
  
}

case class PlayersState(private val players:Map[PlayerId, Player], private val loggedPlayersSet:Set[PlayerId]) {
  
  def playerByName(name:String):Option[Player] = players.values.find(_.name == name)
  def playerById(id:PlayerId):Option[Player] = players.get(id)
  
  def isLogged(id:PlayerId):Boolean = loggedPlayersSet contains id
  
  def putPlayer(pl:Player):PlayersState = copy(players = players + (pl.id -> pl))
  def logPlayer(pl:PlayerId):PlayersState = copy(loggedPlayersSet = loggedPlayersSet + pl)
  def logoffPlayer(pl:PlayerId):PlayersState = copy(loggedPlayersSet = loggedPlayersSet - pl)
 
  lazy val loggedPlayers:Set[Player] = loggedPlayersSet.map(playerById(_).get)
}
 