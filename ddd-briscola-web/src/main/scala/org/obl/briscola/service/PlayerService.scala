package org.obl.briscola
package service

import org.obl.briscola.player._
import org.obl.briscola.competition._
import org.obl.briscola.web.util.Channels
import scalaz.{ -\/, \/, \/- }
import scalaz.stream.Process
import scalaz.concurrent.Task
import rx.lang.scala.Subject
import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject
import org.obl.ddd.StateChange
import org.obl.briscola.web.util.PasswordHelper
import rx.lang.scala.Observer
import org.obl.briscola.service.player._

/**
 * FIXME add logOn(name:String, password:String), logOff(pid:PlayerId)
 */
trait PlayerService {

  def logon(name: String, password: String): PlayerError \/ Player 
  def createPlayer(nm: String, password: String): PlayerError \/ Player

  def playerById(id: PlayerId): Option[Player]
  def allPlayers: Iterable[Player]

  def changes: Observable[StateChange[Iterable[Player], PlayerEvent]]
  
}

trait BasePlayerService extends PlayerService {

  protected def playerRepository: PlayerRepository

  private lazy val changesChannel = ReplaySubject[StateChange[Iterable[Player], PlayerEvent]]

  lazy val changes: Observable[StateChange[Iterable[Player], PlayerEvent]] = changesChannel

  def logon(nm: String, password: String): PlayerError \/ Player =
    playerRepository.byName(nm) match {
      case None => -\/(PlayerWithNameDoesNotExist(nm))
      case Some(pl) => {
        val encPassword = PasswordHelper.encryptPassword(password)
        if (encPassword == pl.password) {
          val plyrs = allPlayers
          
          notifyEvent(plyrs, PlayerLogOn(pl.id), plyrs)    
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
    if (playerRepository.containsName(nm)) -\/(PlayerWithSameNameAlredyExists(nm))
    else {
      val id = playerRepository.newId
      val encPassword = PasswordHelper.encryptPassword(password)
      val player = Player(id, nm, encPassword)
      val previousPlayers = playerRepository.all.toSeq
      playerRepository.put(id, player)
          
      notifyEvent(previousPlayers, PlayerLogOn(id), previousPlayers :+ player)    

      \/-(player)
    }

  def playerById(id: PlayerId): Option[Player] = playerRepository.get(id)
  def allPlayers: Iterable[Player] = playerRepository.all

}

