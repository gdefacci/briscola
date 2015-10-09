package org.obl.briscola
package service

import rx.lang.scala.subjects.ReplaySubject
import rx.lang.scala.Observable

import org.obl.ddd._
import org.obl.briscola._
import org.obl.briscola.player._
import org.obl.briscola.competition._
import scalaz.{-\/, \/, \/-}
import scalaz.stream.Process
import scalaz.concurrent.Task
import org.obl.ddd.StateChange

trait BriscolaService  {
  def startGame(players:Set[PlayerId]):BriscolaError \/ GameState  
  def playCard(id:GameId, pid:PlayerId, card:Card):Option[(BriscolaError \/ GameState)]
  
  def gameById(id:GameId):Option[GameState]
  def allGames:Iterable[GameState] 
  
  def changes:Observable[StateChange[GameState, BriscolaEvent]]
  
  def isFinished(gameId:GameId):Boolean
}

trait BaseBriscolaService extends BriscolaService {

  protected def gameRepository:GameRepository
  
  protected def gameRunner:(GameState,BriscolaCommand) => BriscolaError \/ Seq[StateChange[GameState, BriscolaEvent]]
  
  private lazy val changesChannel = ReplaySubject[StateChange[GameState, BriscolaEvent]]
  
  private def runCommand(st:GameState, cmd:BriscolaCommand):BriscolaError \/ GameState = {
    gameRunner(st, cmd).map { chngs =>
      val gm = chngs.last.state
      gm match {
        case EmptyGameState => ()
        case gm:ActiveGameState => gameRepository.put(gm.id, gm)
        case gm:FinalGameState => gameRepository.put(gm.id, gm)
      }
      chngs.foreach(changesChannel.onNext(_))
      gm
    }
  }
  
  def isFinished(gameId:GameId):Boolean = gameRepository.get(gameId) match {
    case Some(FinalGameState(_,_,_)) => true
    case _ => false
  }
  
  def changes:Observable[StateChange[GameState, BriscolaEvent]] = changesChannel
  
  def startGame(players:Set[PlayerId]):BriscolaError \/ GameState = 
    runCommand(EmptyGameState, StartGame(players))
  
  def playCard(id:GameId, pid:PlayerId, card:Card):Option[(BriscolaError \/ GameState)] = 
    gameRepository.get(id).map { gs =>
      runCommand(gs, PlayCard(pid, card))
    }
  
  def allGames:Iterable[GameState] = gameRepository.all
  
  def gameById(id:GameId):Option[GameState] = gameRepository.get(id)
  
}