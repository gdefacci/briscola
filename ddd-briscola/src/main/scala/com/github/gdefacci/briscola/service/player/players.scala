package com.github.gdefacci.briscola.service.player

import com.github.gdefacci.briscola.service.IdFactory

import com.github.gdefacci.briscola.player._
import com.github.gdefacci.briscola.competition._
import scalaz.{ -\/, \/, \/- }
import rx.lang.scala.{Subject, Observable}
import rx.lang.scala.subjects.ReplaySubject
import com.github.gdefacci.ddd.StateChange

/**
 * FIXME add logOff(pid:PlayerId)
 */
trait PlayerService {

  def logon(name: String, password: String): PlayerError \/ Player 
  def createPlayer(nm: String, password: String): PlayerError \/ Player

  def playerById(id: PlayerId): Option[Player]
  def loggedPlayers: Iterable[Player]

  def changes: Observable[StateChange[Iterable[Player], PlayerEvent]]
  
}

case class PlayerLogInfo(player:Player, isLogged:Boolean)

trait PlayerRepository extends {
  def allPlayers:Iterable[PlayerLogInfo]
  def loggedPlayers:Iterable[Player]
  
  def byId(id:PlayerId):Option[Player]
  def byName(name:String):Option[Player]
  
  def playerLogInfoByName(name:String):Option[PlayerLogInfo]
  
  def store(player: PlayerLogInfo): Unit
  
}

class PlayerServiceImpl(playerRepository: PlayerRepository, playerIdFactory:IdFactory[PlayerId]) extends PlayerService {

  private lazy val changesChannel = ReplaySubject[StateChange[Iterable[Player], PlayerEvent]]

  lazy val changes: Observable[StateChange[Iterable[Player], PlayerEvent]] = changesChannel

  def logon(nm: String, password: String): PlayerError \/ Player =
    playerRepository.playerLogInfoByName(nm) match {
      case None => -\/(PlayerWithNameDoesNotExist(nm))
      case Some(PlayerLogInfo(pl, isLogged)) => {
        val encPassword = PasswordHelper.encryptPassword(password)
        if (encPassword == pl.password) {
          val plyrs = loggedPlayers
          
          playerRepository.store(PlayerLogInfo(pl, isLogged=true))
          
          notifyEvent(plyrs, PlayerLogOn(pl.id), plyrs.toSeq :+ pl)    
          \/-(pl)          
        } else {
          -\/(InvalidPassword(nm))
        }
      }
    }

  def notifyEvent(prev:Iterable[Player], ev:PlayerEvent, curr:Iterable[Player]) = {
    changesChannel.onNext(StateChange(prev, ev, curr))    
  }
  
  def createPlayer(nm: String, password: String): PlayerError \/ Player =
    if (playerRepository.byName(nm).isDefined) -\/(PlayerWithSameNameAlreadyExists(nm))
    else {
      val id = playerIdFactory.newId
      val encPassword = PasswordHelper.encryptPassword(password)
      val player = Player(id, nm, encPassword)
      val previousPlayers = playerRepository.loggedPlayers.toSeq
      playerRepository.store(PlayerLogInfo(player, isLogged=true))
          
      notifyEvent(previousPlayers, PlayerLogOn(id), previousPlayers :+ player)    

      \/-(player)
    }

  def playerById(id: PlayerId): Option[Player] = playerRepository.byId(id)
  def loggedPlayers: Iterable[Player] = playerRepository.loggedPlayers

}