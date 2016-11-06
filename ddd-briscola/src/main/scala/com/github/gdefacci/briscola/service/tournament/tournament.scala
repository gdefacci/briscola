package com.github.gdefacci.briscola.service.tournament

import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.game._
import com.github.gdefacci.briscola.service.game._
import com.github.gdefacci.briscola.competition.MatchKind
import scalaz.{ -\/, \/, \/- }
import com.github.gdefacci.briscola._
import com.github.gdefacci.briscola.tournament._
import rx.lang.scala.Observable
import com.github.gdefacci.ddd.StateChange
import rx.lang.scala.Subscription
import com.github.gdefacci.briscola.player.GamePlayers
import com.github.gdefacci.ddd.rx.ObservableCommandRunner

trait TournamentService {

  def startTournament(player: GamePlayers, kind: MatchKind): TournamentError \/ TournamentState

  def tournamentById(id: TournamentId): Option[TournamentState]
  def allTournament: Iterable[TournamentState]

  def changes: Observable[StateChange[TournamentState, TournamentEvent]]

}

trait TournamentRepository {
  
  def all:Iterable[TournamentState]

  def byId(id:TournamentId):Option[TournamentState]
  
  def store(id:TournamentState):Unit
}


class TournamentServiceImpl(
    runner: ObservableCommandRunner[TournamentState, TournamentCommand, TournamentEvent, TournamentError],
    repository: TournamentRepository, 
    gameService: GameService) extends TournamentService {

   lazy val changes = runner.changes  
  
  private def startGame(ts: ActiveTournamentState): TournamentError \/ TournamentState = {
    gameService.startGame(ts.players) match {
      case -\/(gmErr) =>
        -\/(TournamentGameError(gmErr))

      case \/-(game @ ActiveGameState(gid, _, _, _, _,_)) =>
        val res = runner.run(ts, SetTournamentGame(game))
        val tsId = ts.id
        res.foreach { 
          case _:ActiveTournamentState =>
            lazy val finishedGameSubscription: Subscription = gameService.finishedGames.subscribe { fgm =>
              setGameOutcome(tsId, fgm.newState)
    
              droppedGameSubscription.unsubscribe()
              finishedGameSubscription.unsubscribe()
            }
            lazy val droppedGameSubscription: Subscription = gameService.droppedGames.subscribe { fgm =>
              dropTournamentGame(tsId, fgm.newState)
    
              droppedGameSubscription.unsubscribe()
              finishedGameSubscription.unsubscribe()
            }
          case _ => ()
        }
        res

      case \/-(EmptyGameState) =>
        -\/(TournamentGameError(GameNotStarted))

      case \/-(DroppedGameState(_, _, _, _, _, _, _)) =>
        -\/(TournamentGameError(GameAlreadyDropped))

      case \/-(FinalGameState(_, _, _, _)) =>
        -\/(TournamentGameError(GameAlreadyFinished))

    }
  }
  
  def startTournament(players: GamePlayers, kind: MatchKind): TournamentError \/ TournamentState =
    runner.run(EmptyTournamentState, StartTournament(players, kind)).flatMap {
      case ts @ ActiveTournamentState(id, players, kind, finished, actives) =>
        startGame(ts)
      case x => \/-(x)
    }

  private def setTournamentGame(tournamentId: TournamentId, game: ActiveGameState): Option[TournamentError \/ TournamentState] =
    repository.byId(tournamentId).map { ts =>
      runner.run(ts, SetTournamentGame(game))
    }

  private def setGameOutcome(tournamentId: TournamentId, game: FinalGameState): Option[TournamentError \/ TournamentState] =
    repository.byId(tournamentId).map { ts =>
      runner.run(ts, SetGameOutcome(game)) match {
        case \/-(ts1 @ ActiveTournamentState(_,_,_,_,_)) => startGame(ts1)
        case x => x
      }
    }

  private def dropTournamentGame(tournamentId: TournamentId, game: DroppedGameState): Option[TournamentError \/ TournamentState] =
    repository.byId(tournamentId).map { ts =>
      runner.run(ts, DropTournamentGame(game))
    }

  def tournamentById(id: TournamentId): Option[TournamentState] = repository.byId(id)
  def allTournament: Iterable[TournamentState] = repository.all

}