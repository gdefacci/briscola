package com.github.gdefacci.briscola.player

import com.github.gdefacci.briscola.Event

sealed trait PlayerEvent extends Event

//final case class PlayerCreated(player:Player) extends PlayerEvent
final case class PlayerLogOn(playerId:PlayerId) extends PlayerEvent
final case class PlayerLogOff(playerId:PlayerId) extends PlayerEvent