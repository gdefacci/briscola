package org.obl.briscola.player

import org.obl.ddd.DomainError

sealed trait PlayerError extends DomainError

final case class PlayerWithSameNameAlreadyExists(name:String) extends PlayerError 
final case class PlayerWithNameDoesNotExist(name:String) extends PlayerError 
final case class InvalidPassword(name:String) extends PlayerError
  
