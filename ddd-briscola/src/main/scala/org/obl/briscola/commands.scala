package org.obl.briscola

import org.obl.ddd._
import org.obl.briscola.player._

//object Commands {

//    sealed trait PlayerCommand extends Command
//    case class NewPlayer(name:String) extends PlayerCommand
  
    sealed trait BriscolaCommand extends Command
    
    case class StartGame(players:Set[PlayerId]) extends BriscolaCommand
    case class PlayCard(playerId:PlayerId, card:Card) extends BriscolaCommand

//}