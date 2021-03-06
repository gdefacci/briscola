package com.github.gdefacci.briscola.game

import com.github.gdefacci.ddd._

import scalaz.{-\/, \/, \/-}

import com.github.gdefacci.briscola.player._

trait GameDecider extends Decider[GameState, BriscolaCommand, BriscolaEvent, BriscolaError] {
  
  def nextId:GameId
  def playerById(name:PlayerId):Option[Player]
  
  def apply(s:GameState, cmd:BriscolaCommand):BriscolaError \/ Seq[BriscolaEvent] = {
    (s, cmd) match {
      case (EmptyGameState, StartGame(gamePlayers)) =>

        GamePlayersValidator.withValidPlayersAndTeams(gamePlayers, playerById(_)) { (players, teams) =>
          val (deck, plyrs) = players.foldLeft(Deck.initial -> Seq.empty[PlayerState]) { (acc, player) =>
            val (deck, currPlayers) = acc
            val (cards, newDeck) = deck.takeCards(3)
            newDeck -> (currPlayers ++ Seq(PlayerState(player.id, cards, Score.empty)))
          }

          Seq(GameStarted(ActiveGameState(nextId, deck.briscolaCard(players.size), deck, Nil, plyrs, teams)))
        }
        
      case (EmptyGameState, _) => 
        -\/(GameNotStarted)
        
      case (gm:ActiveGameState, PlayCard(pid, card)) if gm.currentPlayer.id == pid && gm.currentPlayer.cards.contains(card) => 
        \/-(Seq(CardPlayed(pid, card)))
      
      case (gm:ActiveGameState, PlayCard(pid, card)) if gm.currentPlayer.id == pid && !gm.currentPlayer.cards.contains(card) => 
        -\/(PlayerDoesNotOwnCard(pid, card, gm.currentPlayer.cards))
      
      case (gm:ActiveGameState, PlayCard(pid, _)) if !(gm.players.map(_.id) contains pid) => 
        -\/(InvalidPlayer(pid))
      
      case (gm:ActiveGameState, PlayCard(pid, _)) if gm.currentPlayer.id != pid => 
        -\/(InvalidTurn(pid, gm.currentPlayer.id))
      
      case (_:ActiveGameState, StartGame(players)) => 
        -\/(GameAlreadyStarted)
      
      case (_:ActiveGameState, PlayerDropGame(player, reason)) => 
        \/-(Seq(GameDropped(PlayerLeft(player, reason))))
      
      case (_:DroppedGameState, _) => 
        -\/(GameAlreadyDropped)
      
      case (_:FinalGameState, _) => 
        -\/(GameAlreadyFinished)
      
    }
  }
  
}

trait GameEvolver extends Evolver[GameState, BriscolaEvent] {
 
  def playHand(moves:Seq[Move], gameSeed:Seed, deck:Deck):(Deck, Seq[PlayerState]) = {
    val movesWithGameSeed = moves.filter(_.card.seed == gameSeed)
    val winner = if (movesWithGameSeed.nonEmpty) {
      movesWithGameSeed.sortBy(_.card.points).last.player.id
    } else {
      val firstCardSeed = moves.head.card.seed
      moves.filter(_.card.seed == firstCardSeed).sortBy(_.card.points).last.player.id
    }
    val winnerCards = moves.map(_.card)
    val indexOfWinner = moves.indexWhere(_.player.id == winner)
    val len = moves.length
    val idxStates = moves.zipWithIndex.map { p =>
      val (mv, idx) = p
      val newIndex = if (idx < indexOfWinner) len + idx - indexOfWinner else idx - indexOfWinner
      val wonCards = if (idx == indexOfWinner) winnerCards else Nil
      newIndex -> PlayerState(mv.player.id, mv.player.cards.filter(_!=mv.card), mv.player.score.add(wonCards)) 
    }
    idxStates.sortBy(_._1).map(_._2).foldLeft(deck -> Seq.empty[PlayerState]) { (acc, playerState) =>
      val (deck, states) = acc
      val (card, newDeck) = deck.takeCards(1)
      newDeck -> (states ++ Seq(PlayerState(playerState.id, playerState.cards ++ card, playerState.score)))
    }
  }

  def apply(s:GameState, event:BriscolaEvent):GameState = {
    (s,event) match {
      case (EmptyGameState, GameStarted(state)) => 
        state
        
      case (gm @ ActiveGameState(id, briscolaCard, deck, moves, nextPlayers, team), CardPlayed(cardPlayerId, card)) =>
        val newMoves = gm.moves ++ Seq(Move(gm.currentPlayer.copy(cards = gm.currentPlayer.cards.filter(_ != card)), card))
        if (gm.isLastHandTurn) {
          
          val (newDeck, newPlayersState) = playHand(newMoves, briscolaCard.seed, gm.deck)
          
          if (gm.isLastGameTurn) {
            val nsts = newPlayersState.map(s => PlayerFinalState(s.id, s.score ))
            FinalGameState(gm.id, gm.briscolaCard, nsts, team) 
          } else {
            gm.copy(deck = newDeck, moves = Nil, nextPlayers = newPlayersState)
          }
          
        } else {
          gm.copy(moves = newMoves, nextPlayers = gm.nextPlayers.tail)
        }
        
      case (ActiveGameState(id, briscolaCard, deck, moves, nextPlayers, team), GameDropped(reason))  =>
        DroppedGameState(id, briscolaCard, deck, moves, nextPlayers, reason, team)
        
      case (s, cmd) => 
        throw new RuntimeException(s"forbidden condition state:$s event:$event")
    }
  }
}