package com.github.gdefacci.briscola.player

sealed trait PlayerError 

final case class PlayerWithSameNameAlreadyExists(name:String) extends PlayerError 
final case class PlayerWithNameDoesNotExist(name:String) extends PlayerError 
final case class InvalidPassword(name:String) extends PlayerError
  
