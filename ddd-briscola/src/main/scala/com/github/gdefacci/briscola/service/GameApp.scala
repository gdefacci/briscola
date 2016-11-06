package com.github.gdefacci.briscola.service

import com.github.gdefacci.briscola.player.GamePlayers
import com.github.gdefacci.briscola.Event
import com.github.gdefacci.briscola.competition.CompetitionId
import com.github.gdefacci.briscola.game.GameId
import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.tournament.TournamentId
import com.github.gdefacci.briscola.service.competition._
import com.github.gdefacci.briscola.service.game._
import com.github.gdefacci.briscola.service.player._
import com.github.gdefacci.briscola.service.tournament._

case class AppIdFactories(
  competition: IdFactory[CompetitionId],
  game: IdFactory[GameId],
  player: IdFactory[PlayerId],
  tournament: IdFactory[TournamentId])

case class AppRepositories(
  competition: CompetitionRepository,
  game: GameRepository,
  player: PlayerRepository,
  tournament: TournamentRepository)

case class AppServices(
  competition: CompetitionsService,
  game: GameService,
  player: PlayerService,
  tournament: TournamentService)

class GameApp(
    eventsStore: EventsStore[Event],
    idFactories: AppIdFactories,
    repositories: AppRepositories,
    val services: AppServices) {

  services.competition.competitionsFullfilled.subscribe { sc =>

    val gamePlayers: GamePlayers = GamePlayers.filterPlayersByPlayerId(sc.newState.competition.players, pid => sc.newState.acceptingPlayers.contains(pid))

    services.tournament.startTournament(gamePlayers, sc.newState.competition.kind)

  }

  services.competition.changes.subscribe { sc =>
    eventsStore.put(sc.event)
    repositories.competition.store(sc.state)
  }
  services.tournament.changes.subscribe { sc =>
    eventsStore.put(sc.event)
    repositories.tournament.store(sc.state)
  }
  services.game.changes.subscribe { sc =>
    eventsStore.put(sc.event)
    repositories.game.store(sc.state)
  }
  services.player.changes.subscribe { sc =>
    eventsStore.put(sc.event)
  }

}