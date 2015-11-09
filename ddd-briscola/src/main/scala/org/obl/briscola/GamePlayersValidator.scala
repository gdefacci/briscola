package org.obl.briscola

import scalaz.{-\/, \/, \/-}

import player._

object GamePlayersValidator {
  
  def withValidPlayersAndTeams[T](gamePlayers:GamePlayers, playerById:PlayerId => Option[Player])(f:(Set[Player], Option[Teams]) => T):BriscolaError \/ T = {
    val players = GamePlayers.getPlayers(gamePlayers)
    for (
      _       <- checkPlayersNumber(players);
      players <- checkAllPlayersExists(playerById, players);
      teams   <- checkValidTeams(gamePlayers)
    ) yield {
      f(players, teams)
    }
  }
  
  private def checkValidTeams(gamePlayers:GamePlayers):BriscolaError \/ Option[Teams] = {
    val teams = GamePlayers.teams(gamePlayers)
    teams match {
      case r @ Some(teams) =>
        if (teams.teams.size > GameState.MAX_TEAMS_NUMBER) -\/(TooManyTeams(teams, GameState.MAX_TEAMS_NUMBER))
        else if (teams.teams.size < GameState.MIN_TEAMS_NUMBER) -\/(TooFewTeams(teams, GameState.MIN_TEAMS_NUMBER))
        else {
          val playersNumber = teams.teams.head.players.size
          if (!teams.teams.forall( t => t.players.size == playersNumber )) -\/(TeamsMustHaveSameNumberOfPlayers(teams))
          else if (playersNumber > GameState.TEAM_MAX_PLAYERS_NUMBER) -\/(TooManyPlayersPerTeam(teams, GameState.TEAM_MAX_PLAYERS_NUMBER))
          else if (playersNumber < GameState.TEAM_MIN_PLAYERS_NUMBER) -\/(TooFewPlayersPerTeam(teams, GameState.TEAM_MIN_PLAYERS_NUMBER))
          else \/-(r)
        }
        
      case _=> \/-(None)
    }
  }
  
  private def checkPlayersNumber(players:Set[PlayerId]):BriscolaError \/ Unit = {
    val playersNumber = players.size
    if (playersNumber > GameState.MAX_PLAYERS) -\/(TooManyPlayers(players, GameState.MAX_PLAYERS))
    else if (playersNumber < GameState.MIN_PLAYERS) -\/(TooFewPlayers(players, GameState.MIN_PLAYERS))
    else \/-(())
  }
  
  private def checkAllPlayersExists(playerById:PlayerId => Option[Player], players:Set[PlayerId]):PlayersDoNotExist \/ Set[Player] = {
    players.foldLeft[PlayersDoNotExist \/ Set[Player]](\/-(Set.empty)) { (acc, i) =>
      acc match {
        case err @ -\/(PlayersDoNotExist(nonExistingPlayers)) => {
          playerById(i) match {
            case Some(p) => err
            case None => -\/(PlayersDoNotExist(nonExistingPlayers + i))
          }
        }
        case \/-(players) => playerById(i) match {
          case Some(p) => \/-(players + p)
          case None => -\/(PlayersDoNotExist(Set(i)))
        }
      }
    }
  }
}