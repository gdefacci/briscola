package org.obl.briscola
package service

import rx.lang.scala.subjects.ReplaySubject
import rx.lang.scala.Observable
import org.obl.ddd._
import org.obl.briscola._
import org.obl.briscola.player._
import org.obl.briscola.competition._
import scalaz.{ -\/, \/, \/- }
import scalaz.stream.Process
import scalaz.concurrent.Task
import org.obl.ddd.StateChange
import org.obl.briscola.web.util.TStateChange

trait BriscolaService {
  def startGame(players: Set[PlayerId]): BriscolaError \/ GameState
  def playCard(id: GameId, pid: PlayerId, card: Card): Option[(BriscolaError \/ GameState)]

  def gameById(id: GameId): Option[GameState]
  def allGames: Iterable[GameState]

  def changes: Observable[StateChange[GameState, BriscolaEvent]]

  def isFinished(gameId: GameId): Boolean
  
  def finishedGames:Observable[TStateChange[ActiveGameState, BriscolaEvent, FinalGameState]]
  def droppedGames:Observable[TStateChange[ActiveGameState, BriscolaEvent, DroppedGameState]] 
}

trait BaseBriscolaService extends BaseAggregateService[GameId, GameState, BriscolaCommand, BriscolaEvent, BriscolaError] with BriscolaService {

  protected def repository: GameRepository

  def aggregateId(gm: GameState) = GameState.id(gm)

  def isFinished(gameId: GameId): Boolean = repository.get(gameId) match {
    case Some(FinalGameState(_, _, _)) => true
    case _ => false
  }

  def startGame(players: Set[PlayerId]): BriscolaError \/ GameState =
    runCommand(EmptyGameState, StartGame(players))

  def playCard(id: GameId, pid: PlayerId, card: Card): Option[(BriscolaError \/ GameState)] =
    repository.get(id).map { gs =>
      runCommand(gs, PlayCard(pid, card))
    }

  def allGames: Iterable[GameState] = repository.all

  def gameById(id: GameId): Option[GameState] = repository.get(id)

  lazy val finishedGames:Observable[TStateChange[ActiveGameState, BriscolaEvent, FinalGameState]] =
    changes.collect {
      case StateChange(sa @ ActiveGameState(id1,_,_,_,_),e, sb @ FinalGameState(_,_,_)) => TStateChange(sa,e,sb)
    }
  
  lazy val droppedGames:Observable[TStateChange[ActiveGameState, BriscolaEvent, DroppedGameState]] =
    changes.collect {
      case StateChange(sa @ ActiveGameState(id1,_,_,_,_),e, sb @ DroppedGameState(_,_,_,_,_,_)) => TStateChange(sa,e,sb)
    }
}