package org.obl.briscola.player

import org.obl.ddd._

case class PlayerId(id: Long)
case class Player(id: PlayerId, name: String)

