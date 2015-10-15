package org.obl.briscola
package service

import org.obl.ddd._
import org.obl.briscola.player._
import org.obl.ddd.rx.PublishEvolver
import org.obl.briscola.competition._
import org.obl.briscola.tournament._

trait BriscolaApp {
  def playerService: PlayerService
  def gameService: BriscolaService
  def competitionService: CompetitionsService
  def tournamentService: TournamentService
}

class BriscolaAppImpl(
    val eventsStore: EventStore[Event],
    val gameRepository: GameRepository,
    val playerRepository: PlayerRepository,
    val competitionRepository: CompetitionRepository,
    val tournamentRepository: TournamentRepository) extends BriscolaApp { self =>

  private lazy val gameRunner = {
    lazy val gameDecider = new GameDecider {
      def nextId: GameId = gameRepository.newId
      def playerById(id: PlayerId): Option[Player] = playerRepository.get(id)
    }
    lazy val gameEvolver = new PublishEvolver[GameState, BriscolaEvent](new GameEvolver {})

    Runner.changes(gameDecider, gameEvolver)
  }

  private lazy val competitionRunner = {
    lazy val competitionDecider = new CompetitionDecider {
      def nextId: CompetitionId = competitionRepository.newId
      def playerById(id: PlayerId): Option[Player] = playerRepository.get(id)
    }
    lazy val competitionEvolver = new PublishEvolver[CompetitionState, CompetitionEvent](new CompetitionEvolver {})

    Runner.changes(competitionDecider, competitionEvolver)
  }
  
  private lazy val tournamentRunner = {
    lazy val tournamentDecider = new TournamentDecider {
      def playerById(id: PlayerId): Option[Player] = playerRepository.get(id)
    }
    lazy val tournamentEvolver = new PublishEvolver[TournamentState, TournamentEvent](new TournamentEvolver {
      def nextId:TournamentId = tournamentRepository.newId
    })

    Runner.changes(tournamentDecider, tournamentEvolver)
  }

  lazy val playerService: PlayerService = new BasePlayerService() {
    val playerRepository = self.playerRepository
  }

  lazy val gameService:BriscolaService = new BaseBriscolaService {
    lazy val repository = self.gameRepository
    lazy val runner = self.gameRunner
  }

  lazy val competitionService:CompetitionsService = new BaseCompetitionsService {
    lazy val repository = self.competitionRepository
    lazy val runner = self.competitionRunner
  }
  
  lazy val tournamentService:TournamentService = new BaseTournamentService {
    lazy val repository = self.tournamentRepository
    lazy val runner = self.tournamentRunner
    lazy val gameService = self.gameService
  }

  competitionService.competitionsFullfilled.subscribe { sc =>
    
    tournamentService.startTournament(sc.newState.acceptingPlayers, sc.newState.competition.kind)
    
  }
  
  competitionService.changes.subscribe(sc => eventsStore.put(sc.event))
  tournamentService.changes.subscribe(sc => eventsStore.put(sc.event))
  gameService.changes.subscribe(sc => eventsStore.put(sc.event))
  playerService.changes.subscribe(sc => eventsStore.put(sc.event))

}