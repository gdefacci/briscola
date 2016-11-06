package com.github.gdefacci.briscola.presentation.player

import com.github.gdefacci.briscola.presentation
import com.github.gdefacci.briscola.player.PlayerId

import scalaz.{ -\/, \/, \/- }

//object GamePlayersInputAdapter {
//  def apply(playerRoutes: PlayerRoutes) = {
//    val routes = playerRoutes
//    new GamePlayersInputAdapter {
//      lazy val playerRoutes = routes
//    }
//  }
//}

class PlayersInputAdapter(playerRoutes: PlayerRoutes) {

  private def apply(teamPlayer: presentation.competition.Input.TeamPlayer): Throwable \/ com.github.gdefacci.briscola.player.TeamPlayer = {
    import playerRoutes._
    val pidDecoder = playerRoutes.PlayerById.fullPath
    pidDecoder.decodeFull(teamPlayer.player).map { pid =>
      com.github.gdefacci.briscola.player.TeamPlayer(pid, com.github.gdefacci.briscola.player.TeamInfo(teamPlayer.teamName))
    }
  }

  private def apply(teamInfo: presentation.competition.Input.TeamInfo): Throwable \/ com.github.gdefacci.briscola.player.TeamInfo = {
    \/-(com.github.gdefacci.briscola.player.TeamInfo(teamInfo.name))
  }

  def apply(gamePlayers: presentation.competition.Input.GamePlayers): Throwable \/ com.github.gdefacci.briscola.player.GamePlayers = {
    gamePlayers match {

      case presentation.competition.Input.Players(players) =>
        import playerRoutes._
        val z: Throwable \/ Set[PlayerId] = \/-(Set.empty[PlayerId])
        players.foldLeft(z) { (acc, path) =>
          acc.flatMap { pids =>
            val pidDecoder = playerRoutes.PlayerById.fullPath
            pidDecoder.decodeFull(path).map(pid => pids + pid)
          }
        }.map(com.github.gdefacci.briscola.player.Players(_))

      case presentation.competition.Input.TeamPlayers(players, teamInfos) =>
        val zPlayers: Throwable \/ Set[com.github.gdefacci.briscola.player.TeamPlayer] = \/-(Set.empty[com.github.gdefacci.briscola.player.TeamPlayer])
        val teamPlayers = players.foldLeft(zPlayers) { (acc, teamPlayer) =>
          acc.flatMap { teamPlayers =>
            apply(teamPlayer).map(tp => teamPlayers + tp)
          }
        }
        for (tps <- teamPlayers) yield (com.github.gdefacci.briscola.player.TeamPlayers(tps.toSet))
    }

  }

}
