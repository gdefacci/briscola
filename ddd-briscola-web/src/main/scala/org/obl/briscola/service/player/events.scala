package org.obl.briscola
package service
package player

import org.obl.briscola.player.PlayerId

sealed trait PlayerEvent extends org.obl.ddd.Event

final case class PlayerLogOn(playerId:PlayerId) extends PlayerEvent
final case class PlayerLogOff(playerId:PlayerId) extends PlayerEvent