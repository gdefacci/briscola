package org.obl.briscola

import org.obl.ddd._
import org.obl.briscola._
import org.obl.briscola.player._
import org.obl.briscola.competition._
import scalaz.{-\/, \/, \/-}
import scalaz.stream.Process
import scalaz.concurrent.Task

trait BriscolaService  {
  def startGame(players:Set[PlayerId]):BriscolaError \/ GameState  
  def playCard(id:GameId, pid:PlayerId, card:Card):Option[(BriscolaError \/ GameState)]
  
  def gameById(id:GameId):Option[GameState]
  def allGames:Iterable[GameState] 
}

trait BaseBriscolaService extends BriscolaService {

  protected def gameRepository:GameRepository
  
  protected def gameRunner:(GameState,BriscolaCommand) => BriscolaError \/ (Seq[BriscolaEvent], GameState)
  
  private def runCommand(st:GameState, cmd:BriscolaCommand):BriscolaError \/ GameState = {
    gameRunner(st, cmd).map { p =>
      val (_, gm) = p
      gm match {
        case EmptyGameState => ()
        case gm:ActiveGameState => gameRepository.put(gm.id, gm)
        case gm:FinalGameState => gameRepository.put(gm.id, gm)
      }
      gm
    }
  }
  
  def startGame(players:Set[PlayerId]):BriscolaError \/ GameState = 
    runCommand(EmptyGameState, StartGame(players))
  
  def playCard(id:GameId, pid:PlayerId, card:Card):Option[(BriscolaError \/ GameState)] = 
    gameRepository.get(id).map { gs =>
      runCommand(gs, PlayCard(pid, card))
    }
  
  def allGames:Iterable[GameState] = gameRepository.all
  
  def gameById(id:GameId):Option[GameState] = gameRepository.get(id)
  
}