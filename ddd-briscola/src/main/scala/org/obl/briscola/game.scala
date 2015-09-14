package org.obl.briscola

import org.obl.ddd._

import scalaz.{-\/, \/, \/-}

import org.obl.briscola.player._

trait GameDecider extends Decider[GameState, BriscolaCommand, BriscolaEvent, BriscolaError] {
  
  def nextId:GameId
  def player(playerId:PlayerId):Option[Player]
  
  def apply(s:GameState, cmd:BriscolaCommand):BriscolaError \/ Seq[BriscolaEvent] = {
    (s, cmd) match {
      case (EmptyGameState, StartGame(players)) => {
        val playersNumber = players.size
        if (playersNumber > GameState.MAX_PLAYERS) -\/(TooManyPlayers(players, GameState.MAX_PLAYERS))
        else if (playersNumber < GameState.MIN_PLAYERS) -\/(TooFewPlayers(players, GameState.MIN_PLAYERS))
        else {
          val nonExistingPlayers = players.filter(player(_).isEmpty)
          if (nonExistingPlayers.nonEmpty) -\/(PlayerDoesNotExists(nonExistingPlayers))
          else {
            val (deck, plyrs) = players.foldLeft(Deck.initial -> Seq.empty[PlayerState]) { (acc, pid) =>
              val (deck, currPlayers) = acc
              val (cards, newDeck) = deck.takeCards(3)
              newDeck -> (currPlayers ++ Seq(PlayerState(pid, cards, Set.empty)))
            }
            
            \/-(Seq(GameStarted(ActiveGameState(nextId, deck.cards.last.seed, deck, Nil, plyrs))))
          }
        }
      }
      case (gm:ActiveGameState, PlayCard(pid, card)) if gm.currentPlayer.id == pid && gm.currentPlayer.cards.contains(card) => {
        \/-(Seq(CardPlayed(gm.id, pid, card)))
      }
      case (gm:ActiveGameState, PlayCard(pid, card)) if gm.currentPlayer.id == pid && !gm.currentPlayer.cards.contains(card) => {
        -\/(PlayerDoesNotOwnCard(pid, card, gm.currentPlayer.cards))
      }
      case (gm:ActiveGameState, PlayCard(pid, _)) if gm.currentPlayer.id != pid => {
        -\/(InvalidTurn(pid, gm.currentPlayer.id))
      }
      case (EmptyGameState, _) => {
        -\/(GameNotStarted)
      }
      case (_:ActiveGameState, StartGame(players)) => {
        -\/(GameAlreadyStarted)
      }
      case (_:FinalGameState, _) => {
        -\/(GameAlreadyFinished)
      }
    }
  }
  
}

trait GameEvolver extends Evolver[GameState, BriscolaEvent] {
 
  def playHand(moves:Seq[Move], gameSeed:Seed.Value, deck:Deck):(Deck, Seq[PlayerState]) = {
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
      newIndex -> PlayerState(mv.player.id, mv.player.cards.filter(_!=mv.card), mv.player.score ++ wonCards)
    }
    idxStates.sortBy(_._1).map(_._2).foldLeft(deck -> Seq.empty[PlayerState]) { (acc, playerState) =>
      val (deck, states) = acc
      val (card, newDeck) = deck.takeCards(1)
      newDeck -> (states ++ Seq(PlayerState(playerState.id, playerState.cards ++ card, playerState.score)))
    }
  }

  def apply(s:GameState, event:BriscolaEvent):GameState = {
    (s,event) match {
      case (_, GameStarted(state)) => state
      case (gm @ ActiveGameState(id, gameSeed, deck, moves, nextPlayers), CardPlayed(cardGameId, cardPlayerId, card)) => {
        val newMoves = gm.moves ++ Seq(Move(gm.currentPlayer.copy(cards = gm.currentPlayer.cards.filter(_ != card)), card))
        if (gm.isLastHandTurn) {
          
          val (newDeck, newPlayersState) = playHand(newMoves, gm.gameSeed, gm.deck)
          
          if (gm.isLastGameTurn) {
            val nsts = newPlayersState.map(s => PlayerFinalState(s.id, s.score.toSeq.map(_.points).sum, s.score ))
            FinalGameState(gm.id, gm.gameSeed, nsts) 
          } else {
            gm.copy(deck = newDeck, moves = Nil, nextPlayers = newPlayersState)
          }
          
        } else {
          gm.copy(moves = newMoves, nextPlayers = gm.nextPlayers.tail)
        }
      }
      case (s, cmd) => {
        throw new RuntimeException("forbidden condition state:${s} event:${cmd}")
      }
    }
  }
}