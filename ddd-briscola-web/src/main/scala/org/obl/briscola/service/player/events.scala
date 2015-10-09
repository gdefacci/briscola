package org.obl.briscola
package service
package player

import org.obl.briscola.player.PlayerId

sealed trait PlayerEvent extends org.obl.ddd.Event

case class PlayerLogOn(playerId:PlayerId) extends PlayerEvent
case class PlayerLogOff(playerId:PlayerId) extends PlayerEvent