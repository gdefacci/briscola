package com.github.gdefacci.briscola.service.player.impl.simple

import com.github.gdefacci.briscola.player._
import com.github.gdefacci.briscola.service.player._

class SimplePlayerRepository extends PlayerRepository {

  protected val valuesMap = collection.concurrent.TrieMap.empty[PlayerId, PlayerLogInfo]

  def byId(id: PlayerId) = {
    valuesMap.get(id).flatMap(ifLoggedOn)
  }
  
  def ifLoggedOn:PlayerLogInfo => Option[Player] = {
      case PlayerLogInfo(player, true) => Some(player)
      case _ => None
    }

  def store(t: PlayerLogInfo): Unit = {
    valuesMap += (t.player.id -> t)
  }

  def byName(name: String) = playerLogInfoByName(name).flatMap(ifLoggedOn)

  def loggedPlayers: Iterable[Player] = allPlayers.map(ifLoggedOn).collect { case Some(x) => x }

  def allPlayers: Iterable[PlayerLogInfo] = valuesMap.values
  def playerLogInfoByName(name: String): Option[PlayerLogInfo] = valuesMap.values.find(p => p.player.name == name)

}
