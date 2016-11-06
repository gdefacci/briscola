package com.github.gdefacci.briscola.service.game

import com.github.gdefacci.briscola.game._
import com.github.gdefacci.ddd.rx.ObservableCommandRunner
import com.github.gdefacci.briscola.player._
import scalaz.{ -\/, \/, \/- }
import rx.lang.scala.Observable
import com.github.gdefacci.ddd.{StateChange, TStateChange}

trait GameService {
  def startGame(players: GamePlayers): BriscolaError \/ GameState
  def playCard(id: GameId, pid: PlayerId, card: Card): BriscolaError \/ Option[GameState]

  def changes: Observable[StateChange[GameState, BriscolaEvent]]

  def finishedGames: Observable[TStateChange[ActiveGameState, BriscolaEvent, FinalGameState]]
  def droppedGames: Observable[TStateChange[ActiveGameState, BriscolaEvent, DroppedGameState]]

  def gameById(id: GameId): Option[GameState]
  def allGames: Iterable[GameState]
  def isFinished(gameId: GameId): Boolean

}

trait GameRepository {

  def gameById(id: GameId): Option[GameState]
  def allGames: Iterable[GameState]

  def store(game: GameState): Unit
}

class GameServiceImpl(
    runner: ObservableCommandRunner[GameState, BriscolaCommand, BriscolaEvent, BriscolaError],
    repository: GameRepository) extends GameService {

  def gameById(id: GameId): Option[GameState] = repository.gameById(id)
  def allGames: Iterable[GameState] = repository.allGames
  
  def isFinished(gameId: GameId): Boolean = repository.gameById(gameId) match {
    case Some(FinalGameState(_, _, _, _)) => true
    case _ => false
  }
  
  def startGame(players: GamePlayers): BriscolaError \/ GameState =
    runner.run(EmptyGameState, StartGame(players))

  def playCard(id: GameId, pid: PlayerId, card: Card): BriscolaError \/ Option[GameState] =
    repository.gameById(id).map { gs =>
      runner.run(gs, PlayCard(pid, card))
    } match {
      case Some(v) => v.map(Some(_))
      case None => \/-(None)
    }

  lazy val changes: Observable[StateChange[GameState, BriscolaEvent]] = runner.changes  
    
  lazy val finishedGames: Observable[TStateChange[ActiveGameState, BriscolaEvent, FinalGameState]] =
    runner.changes.collect {
      case StateChange(sa: ActiveGameState, e, sb: FinalGameState) => TStateChange(sa, e, sb)
    }

  lazy val droppedGames: Observable[TStateChange[ActiveGameState, BriscolaEvent, DroppedGameState]] =
    runner.changes.collect {
      case StateChange(sa: ActiveGameState, e, sb: DroppedGameState) => TStateChange(sa, e, sb)
    }

}