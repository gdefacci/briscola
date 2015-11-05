package org.obl.briscola.player

import org.obl.ddd._

final case class PlayerId(id: Long)
final case class Player(id: PlayerId, name: String, password:String)

case class TeamId(id:Long, name:String)
final case class Team(id:TeamId, players:Seq[PlayerId])

sealed trait Players
case class TeamPlayers(teams:Seq[Team]) extends Players
case class SinglePlayers(players:Seq[PlayerId]) extends Players

case class GamePlayer(pid:PlayerId, team:Option[TeamId])
case class GamePlayers(players:Seq[GamePlayer])