package org.obl.briscola.presentation.adapters.input

import org.obl.briscola.presentation
import org.obl.briscola.web.PlayerRoutes
import org.obl.briscola.player.PlayerId

import scalaz.{ -\/, \/, \/- }

object GamePlayersInputAdapter {
  def apply(playerRoutes: PlayerRoutes) = {
    val routes = playerRoutes
    new GamePlayersInputAdapter {
      lazy val playerRoutes = routes
    }
  }
}

trait GamePlayersInputAdapter {

  val playerRoutes: PlayerRoutes

  private def apply(teamPlayer: presentation.Input.TeamPlayer): Throwable \/ org.obl.briscola.player.TeamPlayer = {
    import playerRoutes._
    val pidDecoder = playerRoutes.PlayerById.fullPath
    pidDecoder.decodeFull(teamPlayer.player).map { pid =>
      org.obl.briscola.player.TeamPlayer(pid, teamPlayer.teamName)
    }
  }

  private def apply(teamInfo: presentation.Input.TeamInfo): Throwable \/ org.obl.briscola.player.TeamInfo = {
    \/-(org.obl.briscola.player.TeamInfo(teamInfo.name))
  }

  def apply(gamePlayers: presentation.Input.GamePlayers): Throwable \/ org.obl.briscola.player.GamePlayers = {
    gamePlayers match {

      case presentation.Input.Players(players) =>
        import playerRoutes._
        val z: Throwable \/ Set[PlayerId] = \/-(Set.empty[PlayerId])
        players.foldLeft(z) { (acc, path) =>
          acc.flatMap { pids =>
            val pidDecoder = playerRoutes.PlayerById.fullPath
            pidDecoder.decodeFull(path).map(pid => pids + pid)
          }
        }.map(org.obl.briscola.player.Players(_))

      case presentation.Input.TeamPlayers(players, teamInfos) =>
        val zPlayers: Throwable \/ Set[org.obl.briscola.player.TeamPlayer] = \/-(Set.empty[org.obl.briscola.player.TeamPlayer])
        val teamPlayers = players.foldLeft(zPlayers) { (acc, teamPlayer) =>
          acc.flatMap { teamPlayers =>
            apply(teamPlayer).map(tp => teamPlayers + tp)
          }
        }
        val zTeamInfos: Throwable \/ Set[org.obl.briscola.player.TeamInfo] = \/-(Set.empty[org.obl.briscola.player.TeamInfo])
        val mTeasmInfos = teamInfos.foldLeft(zTeamInfos) { (acc, teamInfo: presentation.Input.TeamInfo) =>
          acc.flatMap { tinfos =>
            apply(teamInfo).map(ti => tinfos + ti)
          }
        }
        for (tps <- teamPlayers; tis <- mTeasmInfos) yield (org.obl.briscola.player.TeamPlayers(tps, tis))
    }

  }

}
