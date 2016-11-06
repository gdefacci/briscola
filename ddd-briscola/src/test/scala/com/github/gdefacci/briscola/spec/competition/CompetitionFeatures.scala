package com.github.gdefacci.briscola.spec.competition

import com.github.gdefacci.bdd._
import com.github.gdefacci.bdd.testkit.Predicates._

import CompetitionSteps._
import com.github.gdefacci.briscola.player.Player
import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.player.Players
import com.github.gdefacci.briscola.competition._
import com.github.gdefacci.briscola.game.TooFewPlayers
import com.github.gdefacci.briscola.game.GameState
import com.github.gdefacci.briscola.game.TooManyPlayers
import java.io.PrintWriter
import com.github.gdefacci.briscola.competition.CompetitionStartDeadline.OnPlayerCount


object CompetitionFeatures extends Features {
 
	lazy val `Can't start a competition with just 1 player` = {
    
    val player1 = Player(PlayerId(1), "player1", "player1")  
    
    scenario(
      `Given an empty competition with players`(player1)  
      When `issue the command`(CreateCompetition(player1.id, Players(Set.empty), SingleMatch, CompetitionStartDeadline.AllPlayers)) 
      Then `error is`( `equal to`(CompetioBriscolaError(TooFewPlayers(Set(player1.id), GameState.MIN_PLAYERS))) )
    )
  }
  
  lazy val `Can't start a competition with too many players` = {
    
    val players = 1.to(GameState.MAX_PLAYERS + 1).map( i=> Player(PlayerId(i), s"Player$i", s"Player$i") )  
    val playersIds = players.map(_.id).toSet
    val player1 = players.head
    
    scenario(
      `Given an empty competition with players`(players:_*)  
      When `issue the command`(CreateCompetition(player1.id, Players(playersIds), SingleMatch, CompetitionStartDeadline.AllPlayers)) 
      Then `error is`( `equal to`( CompetioBriscolaError(TooManyPlayers(playersIds + player1.id, GameState.MAX_PLAYERS))) ) 
    )
    
  }
  
  lazy val `Can create a valid competition` = {
    
    val players = 1.to(3).map( i=> Player(PlayerId(i), s"Player$i", s"Player$i") )  
    val playersIds = players.map(_.id).toSet
    val player1 = players.head
    
    lazy val `the correct CreatedCompetitonEvent`:Predicate[CompetitionEvent] = predicate {
      case CreatedCompetition(_, issuingPlayer, Competition(partecipants, SingleMatch, CompetitionStartDeadline.AllPlayers)) => 
        issuingPlayer == player1 && partecipants == Players(playersIds)
      case _ => false
    }
    
    scenario(
      `Given an empty competition with players`(players:_*)  
      When `issue the command`(CreateCompetition(player1.id, Players(playersIds), SingleMatch, CompetitionStartDeadline.AllPlayers)) 
      Then `events contain`( `the correct CreatedCompetitonEvent` ) 
    )
    
  }
  
  lazy val `Can accept a competition` = {
    val players = 1.to(3).map( i=> Player(PlayerId(i), s"Player$i", s"Player$i") )  
    val playersIds = players.map(_.id).toSet
    val player1 = players.head
    val player2 = players.tail.head
    
    lazy val `the correct CompetitionAccepted event`:Predicate[CompetitionEvent] = predicate {
      case CompetitionAccepted(player2Id) => player2Id == player2.id 
      case _ => false
    }
    
    scenario(
      `Given an empty competition with players`(players:_*)  
      When `issue the command`(CreateCompetition(player1.id, Players(playersIds), SingleMatch, CompetitionStartDeadline.AllPlayers)) 
      And `issue the command`(AcceptCompetition(PlayerId(2))) 
      Then `events contain`( `the correct CompetitionAccepted event` ) 
    )
  } 
  
  lazy val `Cant accept a competition more than once` = {
    val players = 1.to(3).map( i=> Player(PlayerId(i), s"Player$i", s"Player$i") )  
    val playersIds = players.map(_.id).toSet
    val player1 = players.head
    val player2 = players.tail.head
    
    scenario(
      `Given an empty competition with players`(players:_*)  
      When `issue the command`(CreateCompetition(player1.id, Players(playersIds), SingleMatch, CompetitionStartDeadline.AllPlayers)) 
      And `issue the command`(AcceptCompetition(PlayerId(2)))
      And `issue the command`(AcceptCompetition(PlayerId(2))) 
      Then  `error is`( `equal to`( CompetitionAlreadyAccepted ) ) 
    )
  } 
  
  lazy val `Can decline a competition` = {
    val players = 1.to(3).map( i=> Player(PlayerId(i), s"Player$i", s"Player$i") )  
    val playersIds = players.map(_.id).toSet
    val player1 = players.head
    val player2 = players.tail.head
    val reason = Some("reason")
    
    lazy val `the correct CompetitionDeclined event`:Predicate[CompetitionEvent] = predicate {
      case CompetitionDeclined(player2Id, declineReason) => 
        player2Id == player2.id && declineReason == reason 
      case _ => false
    }

    lazy val `is a dropped competition`:Predicate[CompetitionState] = predicate { 
      case DroppedCompetition(_,_,_,_) => true
      case _  => false
    }
    
    scenario(
      `Given an empty competition with players`(players:_*)  
      When `issue the command`(CreateCompetition(player1.id, Players(playersIds), SingleMatch, CompetitionStartDeadline.AllPlayers)) 
      And `issue the command`(DeclineCompetition(PlayerId(2), reason)) 
      Then `events contain`( `the correct CompetitionDeclined event` )
      And `the final state`(`is a dropped competition`)
    )
  } 
  
  lazy val `Cant decline a competition more than once` = {
    val players = 1.to(3).map( i=> Player(PlayerId(i), s"Player$i", s"Player$i") )  
    val playersIds = players.map(_.id).toSet
    val player1 = players.head
    val player2 = players.tail.head
    val reason = Some("reason")
    
    scenario(
      `Given an empty competition with players`(players:_*)  
      When `issue the command`(CreateCompetition(player1.id, Players(playersIds), SingleMatch, CompetitionStartDeadline.OnPlayerCount(2))) 
      And `issue the command`(DeclineCompetition(PlayerId(2), reason)) 
      And `issue the command`(DeclineCompetition(PlayerId(2), reason)) 
      Then  `error is`( `equal to`( CompetitionAlreadyDeclined ) ) 
    )
  }
  
  lazy val `When all players have accepted the competition the competition is fullfilled` = {
      
    val player1 = Player(PlayerId(1), s"Player1", s"Player1")
    val player2 = Player(PlayerId(2), s"Player2", s"Player2")
    val players = Seq(player1, player2)
    val playersIds = players.map(_.id).toSet
    
    lazy val `is a proper fullfilled Competition`:Predicate[CompetitionState] = predicate { state =>
      state match {
        case FullfilledCompetition(_,_,players,_) => players == playersIds
        case _ => false
      }
    }
    
    scenario(
      `Given an empty competition with players`(players:_*)  
      When `issue the command`(CreateCompetition(player1.id, Players(playersIds), SingleMatch, CompetitionStartDeadline.AllPlayers)) 
      And `issue the command`(AcceptCompetition(PlayerId(2))) 
      Then `the final state`( `is a proper fullfilled Competition` ) 
    )
  } 
  
  lazy val features = new Feature("Competition", 
      `Can't start a competition with just 1 player`, 
      `Can't start a competition with too many players`,
      `Can create a valid competition`,
      `Can accept a competition`,
      `Cant accept a competition more than once`,
		  `Can decline a competition`,
		  `Cant decline a competition more than once`,
		  `When all players have accepted the competition the competition is fullfilled`) :: Nil
  
  
}