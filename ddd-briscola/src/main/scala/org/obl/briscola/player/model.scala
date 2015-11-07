package org.obl.briscola.player

import org.obl.ddd._

final case class PlayerId(id: Long)
final case class Player(id: PlayerId, name: String, password:String)

case class TeamId(id:Long)
final case class Team(id:TeamId, name:String, players:Seq[PlayerId])
final case class Teams(teams:Seq[Team]) 