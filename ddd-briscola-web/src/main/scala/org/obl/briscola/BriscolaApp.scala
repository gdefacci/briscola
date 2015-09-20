package org.obl.briscola

import org.obl.ddd._
import org.obl.briscola.player._
import org.obl.ddd.rx.PublishEvolver
import org.obl.briscola.competition._

trait BriscolaApp {
  def playerService:PlayerService
  def gameService:BriscolaService 
  def competitionService:CompetitionsService 
}

class BriscolaAppImpl(
  val eventsStore:EventStore[Event],
  val gameRepository:GameRepository,
  val playerRepository:PlayerRepository,
  val competitionRepository:CompetitionRepository
) extends BriscolaApp { self =>

  private lazy val gameEvolver = new PublishEvolver[GameState, BriscolaEvent](new GameEvolver {})
  private lazy val gameRunner = {
    lazy val gameDecider = new GameDecider{
      def nextId:GameId = gameRepository.newId
      def playerById(id:PlayerId):Option[Player] = playerRepository.get(id)
    }
    
    Runner(gameDecider, gameEvolver)
  }
  
  private lazy val competitionEvolver = new PublishEvolver[CompetitionState, CompetitionEvent](new CompetitionEvolver {})
  private lazy val competitionRunner = {
    lazy val competitionDecider = new CompetitionDecider{
      def nextId:CompetitionId = competitionRepository.newId
      def playerById(id:PlayerId):Option[Player] = playerRepository.get(id)
    }
    
    Runner(competitionDecider, competitionEvolver)
  }
  
  lazy val playerService:PlayerService = new BasePlayerService() {
    val playerRepository = self.playerRepository
  }
  
  lazy val gameService = new BaseBriscolaService {
    lazy val gameRepository = self.gameRepository
    lazy val gameRunner = self.gameRunner
  }
  
  lazy val competitionService = new BaseCompetitionsService {
    lazy val competitionRepository = self.competitionRepository
    lazy val competitionRunner = self.competitionRunner
  }
  
  gameEvolver.changes.subscribe { sc =>
    sc.state match {
      case gm:ActiveGameState => gm.players.foreach { pl =>
        playerService.sendToPlayer(pl.id, sc.event, sc.state)
      }
      case gm:FinalGameState => gm.players.foreach { playerState =>
        playerService.sendToPlayer(playerState.id, sc.event, sc.state)
      }
      case _ => ()
    }
  }
  
  competitionEvolver.changes.subscribe { sc =>
    sc.state match {
      case oc:OpenCompetition => oc.competition.players.foreach { player =>
        playerService.sendToPlayer(player.id, sc.event, sc.state)
      }
      case oc:DroppedCompetition => oc.competition.players.foreach { player =>
        playerService.sendToPlayer(player.id, sc.event, sc.state)
      }
      case EmptyCompetition => ()
      case c:FullfilledCompetition => 
        /** FIXME, c.competition.kind is ignored */
        gameService.startGame(c.acceptingPlayers)
    }
  }
  
  competitionEvolver.changes.subscribe(sc => eventsStore.put(sc.event) )
  gameEvolver.changes.subscribe(sc => eventsStore.put(sc.event) )
  
}