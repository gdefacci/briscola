package org.obl.briscola

import org.obl.ddd._
import scalaz.{-\/, \/, \/-}
import org.obl.briscola.player._

class ReplGame(players:Set[PlayerId]) {
  private var state:GameState  = EmptyGameState
  
  def activeGameState:ActiveGameState  = state match {
    case gm:ActiveGameState => gm
    case _ => throw new RuntimeException(s"invalid state $state")
  }

  def finalGameState:FinalGameState  = state match {
    case gm:FinalGameState => gm
    case _ => throw new RuntimeException(s"invalid state $state")
  }

  val decider = new GameDecider {

    def nextId: GameId = GameId(1)
    def playerById(playerId: PlayerId) = Some(Player(playerId, playerId.id.toString))

  }

  val evolver = new GameEvolver {}
  
  lazy val runner = Runner(decider, evolver)
  
  def pushCommand(cmd:BriscolaCommand):BriscolaError \/ GameState = {
    val newState = runner(state, cmd).map(_._2)
    newState.foreach( state = _ )
    newState
  }
  
  def put(c:Card):String \/ GameState = {
    pushCommand(PlayCard(activeGameState.currentPlayer.id, c)).leftMap(_.toString)
  }
  
  def start() = {
    pushCommand(StartGame(players)).leftMap(_.toString)
  }
  
  def isFinished = state match {
    case fs:FinalGameState => true
    case _ => false
  }

}

object ReplGamePlay extends App {
  
  import scala.io.StdIn.readLine
    
  def parseCard(str:String):String \/ Card = {
    val seed = str.charAt(0).toUpper match {
      case 'B' => \/-(Seed.bastoni)
      case 'C' => \/-(Seed.coppe)
      case 'D' => \/-(Seed.denari)
      case 'S' => \/-(Seed.spade)
      case x => -\/(s"invalid seed $x")
    }
    val num = \/.fromTryCatchNonFatal( str.substring(1, str.length).trim.toByte ).leftMap(_.toString)
    for (n <- num; s<- seed) yield (Card(n,s))
  }

  val players = 1.to(3).map(PlayerId(_)).toSet
  val game = new ReplGame(players)

  game.start() match {
    case -\/(err) => println(s"Error: $err")
    case _ => {
      while (!game.isFinished) {
        val txt =  s"""
Seed ${game.activeGameState.gameSeed}
Player ${game.activeGameState.currentPlayer}
  NextPlayers :
${game.activeGameState.nextPlayers.tail.map(i => "   "+i.toString()).mkString("\n")}
OnTable ${game.activeGameState.moves.map(_.card).mkString(", ")}
          """        
        println(txt)
        val cardStr = readLine() 
        parseCard(cardStr) match {
          case -\/(err) => println(s"Error: $err")
          case \/-(card) => {
            game.put(card) match {
              case -\/(err) => println(s"Error: $err")
              case \/-(_) => println(s"played card: $card")
            }
          }
        }
      }
      println(game.finalGameState)
    }
  } 
  
  
  
  
}