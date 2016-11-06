package com.github.gdefacci.briscola.spec.player

import com.github.gdefacci.briscola.service.player.PlayerService
import com.github.gdefacci.bdd._
import scalaz.{-\/, \/, \/-}

case class PlayerTestState(service: PlayerService) 

class PlayerStep( serviceFactory:() => PlayerService) extends BDD[PlayerTestState, scalaz.Id.Id, String] {
  
  def `Given an initial PlayerService`: Source = source { () =>
    PlayerTestState(serviceFactory())
  }
  
  def `create player with`(name:String, password:String):Step = step { state =>
    state.service.createPlayer(name, password)
    state  
  }
  
  def `player sucessfully logon with`(name:String, password:String):Step = step { state =>
    val r = state.service.logon(name, password)
    r match {
      case -\/(err) => throw new RuntimeException(err.toString())
      case _ => ()
    }
    state  
  }
  
  
  
  
  
}