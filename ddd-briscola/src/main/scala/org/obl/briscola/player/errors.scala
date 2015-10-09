package org.obl.briscola.player

import org.obl.ddd.DomainError

sealed trait PlayerError extends DomainError

case class PlayerWithSameNameAlredyExists(name:String) extends PlayerError 
case class PlayerWithNameDoesNotExist(name:String) extends PlayerError 
case class InvalidPassword(name:String) extends PlayerError
  
