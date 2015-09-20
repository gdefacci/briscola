package org.obl.briscola

import org.obl.briscola.player._
import org.obl.briscola.competition._

import org.obl.briscola.web.util.Channels

import scalaz.{-\/, \/, \/-}
import scalaz.stream.Process
import scalaz.concurrent.Task

trait PlayerService {

  def sendToPlayer(pl:PlayerId, ev:BriscolaEvent, gm:GameState):Boolean
  def sendToPlayer(pl:PlayerId, ev:CompetitionEvent, gm:CompetitionState):Boolean
  
  def createPlayer(nm:String):PlayerError \/ Player 
  
  def playerById(id:PlayerId):Option[Player]  
  def allPlayers:Iterable[Player]  
  
  def playerGamesChannel(pid:PlayerId):Option[Process[Task, (BriscolaEvent, GameState)]]
  def playerCompetitionsChannel(pid:PlayerId):Option[Process[Task, (CompetitionEvent, CompetitionState)]]
  
}

trait BasePlayerService extends PlayerService {
  
  protected def playerRepository:PlayerRepository
  
  private lazy val gamesChannel = new Channels[PlayerId, (BriscolaEvent, GameState)] 
  private lazy val competitionChannel = new Channels[PlayerId, (CompetitionEvent, CompetitionState)] 

  def createPlayer(nm:String):PlayerError \/ Player = 
    if (playerRepository.containsName(nm)) -\/(PlayerWithSameNameAlredyExists(nm))
    else {
      val id = playerRepository.newId
      val player = Player(id,nm)
      playerRepository.put(id, player)
      \/-(player)
    }
  
  def playerById(id:PlayerId):Option[Player]  = playerRepository.get(id)
  def allPlayers:Iterable[Player] = playerRepository.all 
  
  def sendToPlayer(pl:PlayerId, ev:BriscolaEvent, gm:GameState):Boolean = 
    playerById(pl).map { player =>
      gamesChannel.send(pl, (ev, gm))
      true
    }.getOrElse(false)

  def sendToPlayer(pl:PlayerId, ev:CompetitionEvent, gm:CompetitionState):Boolean = 
    playerById(pl).map { player =>
      competitionChannel.send(pl, (ev, gm))
      true
    }.getOrElse(false)
  
  
  def playerCompetitionsChannel(pid:PlayerId):Option[Process[Task, (CompetitionEvent, CompetitionState)]] =
    competitionChannel.channel(pid)
  
  def playerGamesChannel(pid:PlayerId):Option[Process[Task, (BriscolaEvent, GameState)]] =
    gamesChannel.channel(pid)
  
}
