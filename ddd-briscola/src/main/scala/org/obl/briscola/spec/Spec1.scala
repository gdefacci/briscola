package org.obl.briscola
package spec

import org.obl.ddd._
import org.obl.ddd.spec._
import org.obl.briscola.player._

object Spec1 extends App with BriscolaSpec {

  val reporter = new PrintlnReporter[GameState, BriscolaEvent, BriscolaError]

//  uncomment to check erro text messages
//  {
//    val players = Set(PlayerId(1))
//    
//    check(
//      When(StartGame(Set(PlayerId(1)))) expect
//        ErrorIs(TooFewPlayers(players, GameState.MAX_PLAYERS)))
//  }
//
//  {
//    val players = 1.to(GameState.MAX_PLAYERS + 1).map(PlayerId(_)).toSet
//
//    check(s"are acepted at max ${GameState.MAX_PLAYERS} players",
//
//      When(StartGame(players)) expect
//        EventsAre(CardPlayed(GameId(1), PlayerId(1), Card(7, Seed.bastoni)))
//    ) 
//  }
  
  {
    val players = Set(PlayerId(1))
    
    check(
      When(StartGame(Set(PlayerId(1)))) expect
        ErrorIs(TooFewPlayers(players, GameState.MIN_PLAYERS)))
  }

  {
    val players = 1.to(GameState.MAX_PLAYERS + 1).map(PlayerId(_)).toSet

    check(s"are acepted at max ${GameState.MAX_PLAYERS} players",

      When(StartGame(players)) expect
        ErrorIs(TooManyPlayers(players, GameState.MAX_PLAYERS))
    ) 
  }
  
  {
    val players = Set(PlayerId(1), PlayerId(2))
    check(
      When(StartGame(players), StartGame(players)) expect ErrorIs(GameAlreadyStarted)
    )
  }

  {
    val players = Set(PlayerId(1), PlayerId(2))
    check(
      When(StartGame(players)) expect (
        EventsThat("include only GameStarted") {
          case Seq(GameStarted(_)) => true
          case _ => false
        } and
        StateThat("no move has been made") {
          case gm: ActiveGameState => gm.moves.isEmpty
          case _ => false
        } and
        StateThat("contains all players") {
          case gm: ActiveGameState => gm.nextPlayers.map(_.id).toSet == players
          case _ => false
        } ) andThenOnNewState[ActiveGameState] { newState =>
          
          val id = newState.id
          val pid = newState.currentPlayer.id
          val aCard = newState.currentPlayer.cards.toSeq(0)
          
          When(PlayCard(pid, aCard)) expect EventsAre(CardPlayed(id, pid, aCard))
          
        } andThenOnNewState[ActiveGameState] { newState =>
          
          val id = newState.id
          val pid = newState.currentPlayer.id
          val aCard = newState.currentPlayer.cards.toSeq(0)
          
          When(PlayCard(pid, aCard)) expect EventsAre(CardPlayed(id, pid, aCard))  

        }
    )
  }
  
  {
    val pid1 = PlayerId(1)
    val pid2 = PlayerId(2)
    val gid = GameId(1)
    val players = Set(pid1, pid2)
    val deck = Deck(Seq(Card(7, Seed.coppe), Card(8, Seed.coppe)))
    
    val state = ActiveGameState(gid, Seed.coppe, deck, Seq(Move(PlayerState(pid1, Set(Card(7, Seed.denari), Card(8, Seed.denari)), Set.empty), Card(2, Seed.coppe))), 
        Seq(PlayerState(pid2, Set(Card(7, Seed.spade), Card(8, Seed.spade), Card(9, Seed.spade)), Set.empty) ))
    
      check( OnState(state) and When(PlayCard(pid2, Card(7, Seed.spade))) expect( EventsAre(CardPlayed(gid, pid2, Card(7, Seed.spade)) ) )    
    )
  }

  {
    val pid1 = PlayerId(1)
    val pid2 = PlayerId(2)
    val gid = GameId(1)
    val players = Set(pid1, pid2)
    
    val state = ActiveGameState(gid, Seed.coppe, Deck.empty, Seq(Move(PlayerState(pid1, Set.empty, Set.empty), Card(2, Seed.coppe))), 
        Seq(PlayerState(pid2, Set(Card(7, Seed.spade)), Set.empty) ))
    
      check( 
          OnState(state) and When(PlayCard(pid2, Card(7, Seed.spade))) expect( 
            EventsAre(CardPlayed(gid, pid2, Card(7, Seed.spade)) ) and
            StateIs(FinalGameState(gid, Seed.coppe, Seq(
                PlayerFinalState(pid1, 0, Set(Card(7, Seed.spade), Card(2, Seed.coppe))),
                PlayerFinalState(pid2, 0, Set.empty 
                )))) and
            StateThatIs[FinalGameState]("player 1 is the winner")( s => s.winner.id == pid1 )
          )    
      )
  }

}  
  