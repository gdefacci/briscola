package org.obl.briscola.service

import org.obl.briscola.player.PlayerId
import org.obl.briscola.competition.MatchKind
import scalaz.{ -\/, \/, \/- }
import org.obl.briscola._
import org.obl.briscola.tournament._
import rx.lang.scala.Observable
import org.obl.ddd.StateChange
import rx.lang.scala.Subscription

trait TournamentService {

  def startTournament(player: Set[PlayerId], kind: MatchKind): TournamentError \/ TournamentState
//  def setTournamentGame(tournamentId: TournamentId, game: ActiveGameState): Option[TournamentError \/ TournamentState]
//  def setGameOutcome(tournamentId: TournamentId, game: FinalGameState): Option[TournamentError \/ TournamentState]
//  def dropTournamentGame(tournamentId: TournamentId, game: DroppedGameState): Option[TournamentError \/ TournamentState]

  def tournamentById(id: TournamentId): Option[TournamentState]
  def allTournament: Iterable[TournamentState]

  def changes: Observable[StateChange[TournamentState, TournamentEvent]]

//  def isCompleted(id: TournamentId): Boolean
}

trait BaseTournamentService extends BaseAggregateService[TournamentId, TournamentState, TournamentCommand, TournamentEvent, TournamentError] with TournamentService {

  protected def repository: TournamentRepository

  protected def aggregateId(s: TournamentState): Option[TournamentId] = TournamentState.id(s)

//  def startTournament(players: Set[PlayerId], kind: MatchKind): TournamentError \/ TournamentState =
//    runCommand(EmptyTournamentState, StartTournament(players, kind))

    
  def gameService: BriscolaService

  private def startGame(ts: ActiveTournamentState): TournamentError \/ TournamentState = {
    gameService.startGame(ts.players) match {
      case -\/(gmErr) =>
        -\/(TournamentGameError(gmErr))

      case \/-(game @ ActiveGameState(gid, _, _, _, _)) =>
        val res = runCommand(ts, SetTournamentGame(game))
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

      case \/-(DroppedGameState(_, _, _, _, _, _)) =>
        -\/(TournamentGameError(GameAlreadyDropped))

      case \/-(FinalGameState(_, _, _)) =>
        -\/(TournamentGameError(GameAlreadyFinished))

    }
  }
  
  def startTournament(players: Set[PlayerId], kind: MatchKind): TournamentError \/ TournamentState =
    runCommand(EmptyTournamentState, StartTournament(players, kind)).flatMap {
      case ts @ ActiveTournamentState(id, players, kind, finished, actives) =>
        startGame(ts)
      case x => \/-(x)
    }

  private def setTournamentGame(tournamentId: TournamentId, game: ActiveGameState): Option[TournamentError \/ TournamentState] =
    repository.get(tournamentId).map { ts =>
      runCommand(ts, SetTournamentGame(game))
    }

  private def setGameOutcome(tournamentId: TournamentId, game: FinalGameState): Option[TournamentError \/ TournamentState] =
    repository.get(tournamentId).map { ts =>
      runCommand(ts, SetGameOutcome(game)) match {
        case \/-(ts1 @ ActiveTournamentState(_,_,_,_,_)) => startGame(ts1)
        case x => x
      }
    }

  private def dropTournamentGame(tournamentId: TournamentId, game: DroppedGameState): Option[TournamentError \/ TournamentState] =
    repository.get(tournamentId).map { ts =>
      runCommand(ts, DropTournamentGame(game))
    }

  def tournamentById(id: TournamentId): Option[TournamentState] = repository.get(id)
  def allTournament: Iterable[TournamentState] = repository.all

  def isCompleted(id: TournamentId): Boolean = repository.get(id) match {
    case Some(cts: CompletedTournamentState) => true
    case _ => false
  }

}