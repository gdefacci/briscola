package org.obl.brisola.integration
package tests

import org.obl.free._
import org.obl.briscola.presentation.CreatedCompetition
import org.obl.briscola.presentation.EventAndState
import org.obl.briscola.presentation.CompetitionAccepted
import org.obl.briscola.presentation.CompetitionState
import scalaz.{-\/, \/, \/-}
import org.obl.briscola.presentation.CompetitionDeclined
import org.scalatest.FunSuite
import org.obl.briscola.presentation.GameState
import org.obl.briscola.presentation.GameStarted

class PlayerTest extends FunSuite with PlayersIntegrationTest[Unit] with ScalaTestReporter {

  import stepFactory._
  import CompetionEventDecoders._  

  lazy val background = Backgrounds.void
  
  lazy val scenarios = Seq(
    Scenario("a player can register", for {
      _ <- createNewPlayer("pippo", "password")
    } yield ()),

    Scenario("cant create a player with duplicate name", for {
      _ <- createNewPlayer("pippo", "password")
      _ <- createNewPlayerFails("pippo", "password")
    } yield ()),

    Scenario("a player can register and logon", for {
      player <- createNewPlayer("pippo", "password")
      logPlayer <- playerLogin(player.name, "password")
    } yield ()),

    Scenario("a player with invalid credentials cant logon", for {
      player <- createNewPlayer("pippo", "password")
      logPlayer <- playerLoginFails(player.name, "wrong password")
    } yield ()),

    Scenario("a player that does not exists cant logon", for {
      logPlayer <- playerLoginFails("pippo", "password")
    } yield ()),
    
    Scenario("a logged player can start a competion", for {
      player1 <- createNewPlayer("pippo", "password")
      player2 <- createNewPlayer("pluto", "pass")
      comp <- player1.createCompetion(Seq(player2))
      evState1 <- player2.events.getFirstOf[EventAndState[CreatedCompetition, CompetitionState]]("CreatedCompetition")
      _ <- check( evState1.event.issuer == player1.self ,               "competition issuer is player1" )
      _ <- check( evState1.state.acceptingPlayers == Set(player1.self), "player1 is the only accepting player" )
    } yield ()),
    
    Scenario("a player invited to join a competion can accept and the other players are notified", for {
      player1 <- createNewPlayer("pippo", "password")
      player2 <- createNewPlayer("pluto", "pass")
      player3 <- createNewPlayer("qui", "qpas")
      
      comp <- player1.createCompetion(Seq(player2, player3))

      compCreated2 <- player2.events.getFirstOf[EventAndState[CreatedCompetition, CompetitionState]]("CreatedCompetition") 
      _ <- player2.acceptCompetition(compCreated2.state)

      compAccepted1 <- player1.events.getFirstOf[EventAndState[CompetitionAccepted, CompetitionState]]("CompetitionAccepted")
      _ <- check( compAccepted1.event.player == player2.self , s"the player who accepted the competition is ${player2.name}" )
      
      compAccepted3 <- player3.events.getFirstOf[EventAndState[CompetitionAccepted, CompetitionState]]("CompetitionAccepted")
      _ <- check( compAccepted3.event.player == player2.self , s"the player who accepted the competition is ${player2.name}" )
    } yield ())

    
  )

  verify(testResults)  
    
}


class ThreePlayersTest extends FunSuite with PlayersIntegrationTest[CompetionWith3Players] with ScalaTestReporter {
  
  import stepFactory._
  import CompetionEventDecoders._  
  import GameEventDecoders._  

  lazy val background =  Backgrounds.given3PlayersAndPlayer1CreateACompetion
  
  lazy val scenarios = Seq(
    Scenario("a player can accept a competition", for {
      state <- initialState
      
      compCreated2 <- state.player2.events.getFirstOf[EventAndState[CreatedCompetition, CompetitionState]]("CreatedCompetition") 
      _ <- state.player2.acceptCompetition(compCreated2.state)

      compAccepted1 <- state.player1.events.getFirstOf[EventAndState[CompetitionAccepted, CompetitionState]]("CompetitionAccepted")
      _ <- check( compAccepted1.event.player == state.player2.self , "player2 is the player who accepted the competition" )
      
      compAccepted3 <- state.player3.events.getFirstOf[EventAndState[CompetitionAccepted, CompetitionState]]("CompetitionAccepted")
      _ <- check( compAccepted3.event.player == state.player2.self , "player2 is the player who accepted the competition" )
      
    } yield ()),
    
    Scenario("a player can decline a competition", for {
      state <- initialState

      compCreated2 <- state.player2.events.getFirstOf[EventAndState[CreatedCompetition, CompetitionState]]("CreatedCompetition") 
      _ <- state.player2.declineCompetition(compCreated2.state)
      
      compDeclined1 <- state.player1.events.getFirstOf[EventAndState[CompetitionDeclined, CompetitionState]]("CompetitionDeclined")
      _ <- check( compDeclined1.event.player == state.player2.self , "player2 is the player who declined the competition" )
      
      compDeclined3 <- state.player3.events.getFirstOf[EventAndState[CompetitionDeclined, CompetitionState]]("CompetitionDeclined")
      _ <- check( compDeclined3.event.player == state.player2.self , "player2 is the player who declined the competition" )
      
    } yield ()),
    
    Scenario("if every player accept the competition the game starts", for {
      state <- initialState
      
      _ <- check(state.competition.acceptingPlayers.contains(state.player1.self), s"${state.player1.name} is an accepting player")
      
      compCreated2 <- state.player2.events.getFirstOf[EventAndState[CreatedCompetition, CompetitionState]]("CreatedCompetition")
      _ <- state.player2.acceptCompetition(compCreated2.state)
      
      compCreated3 <- state.player3.events.getFirstOf[EventAndState[CreatedCompetition, CompetitionState]]("CreatedCompetition")
      _ <- state.player3.acceptCompetition(compCreated3.state)
      
      gameStarted1 <- state.player1.events.getFirstOf[EventAndState[GameStarted, GameState]]("GameStarted")
      gameStarted2 <- state.player2.events.getFirstOf[EventAndState[GameStarted, GameState]]("GameStarted")
      gameStarted3 <- state.player3.events.getFirstOf[EventAndState[GameStarted, GameState]]("GameStarted")
      
    } yield ()),
    
    Scenario("if at least a player decline the competition the game is not started", for {
      state <- initialState
      compCreated2 <- state.player2.events.getFirstOf[EventAndState[CreatedCompetition, CompetitionState]]("CreatedCompetition")
      _ <- state.player2.acceptCompetition(compCreated2.state)
      
      compCreated3 <- state.player3.events.getFirstOf[EventAndState[CreatedCompetition, CompetitionState]]("CreatedCompetition")
      _ <- state.player3.declineCompetition(compCreated3.state)
      
      gameStarted1 <- state.player1.events.allOf[EventAndState[GameStarted, GameState]]
      _ <- check(gameStarted1.isEmpty, "player1 did not received GameStartedEvent")
      gameStarted2 <- state.player2.events.allOf[EventAndState[GameStarted, GameState]]
      _ <- check(gameStarted2.isEmpty, "player2 did not received GameStartedEvent")
      gameStarted3 <- state.player3.events.allOf[EventAndState[GameStarted, GameState]]
      _ <- check(gameStarted3.isEmpty, "player3 did not received GameStartedEvent")
      
    } yield ())
    
  )
  
  verify(testResults)
}




