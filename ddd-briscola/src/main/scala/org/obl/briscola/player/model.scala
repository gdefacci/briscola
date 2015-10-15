package org.obl.briscola.player

import org.obl.ddd._

final case class PlayerId(id: Long)
final case class Player(id: PlayerId, name: String, password:String)

