package com.github.gdefacci.briscola.service

import com.github.gdefacci.briscola.Event
import com.github.gdefacci.briscola.game._
import com.github.gdefacci.briscola.service.game._
import com.github.gdefacci.briscola.competition._
import com.github.gdefacci.briscola.service.competition._
import com.github.gdefacci.briscola.tournament._
import com.github.gdefacci.briscola.service.tournament._
import com.github.gdefacci.briscola.player._
import com.github.gdefacci.briscola.service.player._
import com.github.gdefacci.ddd.rx.PublishEvolver
import com.github.gdefacci.ddd.rx.ObservableCommandRunner
import com.github.gdefacci.di.runtime.Bind
import com.github.gdefacci.ddd.Decider
import javax.inject.Singleton

class GameAppModule {

  lazy val eventStore = EventsStore[Event]

  def gameDecider(idFactory: IdFactory[GameId], playerRepository: PlayerRepository): GameDecider = {
    new GameDecider {
      def nextId: GameId = idFactory.newId
      def playerById(id: PlayerId): Option[Player] = playerRepository.byId(id)
    }
  }

  def gameEvolver: PublishEvolver[GameState, BriscolaEvent] = new PublishEvolver[GameState, BriscolaEvent](new GameEvolver {})

  def competitionDecider(idFactory: IdFactory[CompetitionId], playerRepository: PlayerRepository): CompetitionDecider =
    new CompetitionDecider {
      def nextId: CompetitionId = idFactory.newId
      def playerById(id: PlayerId): Option[Player] = playerRepository.byId(id)
    }

  def competitionEvolver:PublishEvolver[CompetitionState, CompetitionEvent] = new PublishEvolver[CompetitionState, CompetitionEvent](new CompetitionEvolver {})

  def tournamentDecider(playerRepository: PlayerRepository): TournamentDecider  = 
    new TournamentDecider {
      def playerById(id: PlayerId): Option[Player] = playerRepository.byId(id)
    }
    
  
  def tournamentEvolver(idFactory: IdFactory[TournamentId]): PublishEvolver[TournamentState, TournamentEvent] = 
    new PublishEvolver[TournamentState, TournamentEvent](new TournamentEvolver {
      def nextId: TournamentId = idFactory.newId
    })
    
  def createObservableCommandRunner[S, C, E, Err](decider: Decider[S, C, E, Err], evolver: PublishEvolver[S, E]):ObservableCommandRunner[S,C,E,Err] =
    ObservableCommandRunner[S,C,E,Err](decider, evolver)
  
  @Singleton val bindCompetitionService = Bind[CompetitionsService, CompetitionsServiceImpl]
  @Singleton val bindGameService = Bind[GameService, GameServiceImpl]  
  @Singleton val bindPlayerService = Bind[PlayerService, PlayerServiceImpl]  
	@Singleton val bindTournamentService = Bind[TournamentService, TournamentServiceImpl]  
}