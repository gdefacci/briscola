package org.obl.briscola
package web

import player._
import argonaut._
import Argonaut._
import org.obl.raz.Path

import org.obl.briscola.web.util.ArgonautHelper._

object jsonEncoders {
  
  implicit lazy val pathEncoder = jencode1((p: Path) => p.render)

  implicit def collectionEncoder[T](implicit enc:EncodeJson[T]):EncodeJson[Presentation.Collection[T]] =
    jencode1L((es: Presentation.Collection[T]) => es.members)("members")
  
  implicit def eventAndState[E,S](implicit ee:EncodeJson[E], se:EncodeJson[S]) = 
    jencode2L((es: Presentation.EventAndState[E,S]) => (es.event, es.state))("event", "state")
  
  /** Player */
  implicit lazy val playerEncoder = 
    jencode2L((pl: Presentation.Player) => (pl.self, pl.name))("self", "name")
  
  lazy val privatePlayerEncoder = EncodeJson.derive[Presentation.Player] 

  implicit lazy val playerEventEncoder = {
    
    implicit lazy val playerEventKindEncoder = enumEncoder[Presentation.PlayerEventKind.type]
    implicit lazy val playerLogOnEncoder = jencode2L( (e:Presentation.PlayerLogOn) => (e.player, e.kind) )("player", "kind")
    implicit lazy val playerLogOffEncoder = jencode2L( (e:Presentation.PlayerLogOff) => (e.player, e.kind) )("player", "kind")
    
    jencode1((p: Presentation.PlayerEvent) => p match {
      case c:Presentation.PlayerLogOn => playerLogOnEncoder(c)
      case c:Presentation.PlayerLogOff => playerLogOffEncoder(c)
    })  
  }
  
  /** Game */
  implicit lazy val seedEncoder = enumEncoder[Seed.type] 
      
  implicit lazy val cardEncoder = {
    implicit val byteEnc = EncodeJson.IntEncodeJson.contramap((b:Byte) => b.toInt)
    EncodeJson.derive[Card]
  }
  
  implicit lazy val playerStateEncoder = EncodeJson.derive[Presentation.PlayerState]

  implicit lazy val playerFinalStateEncoder = EncodeJson.derive[Presentation.PlayerFinalState]
  
  implicit lazy val moveEncoder = EncodeJson.derive[Presentation.Move] 

  implicit lazy val gameStateKindEncoder = enumEncoder[Presentation.GameStateKind.type] 
  
  implicit lazy val activeGameEncoder = jencode11L((p: Presentation.ActiveGameState) => (
        p.self, p.briscolaCard, p.moves, p.nextPlayers, p.currentPlayer, p.isLastHandTurn, p.isLastGameTurn, p.players, p.playerState, p.deckCardsNumber, p.kind))(
        "self", "briscolaCard", "moves", "nextPlayers", "currentPlayer", "isLastHandTurn", "isLastGameTurn", "players", "playerState", "deckCardsNumber", "kind")
  
  implicit lazy val gameStateEncoder = {    

    lazy val emptyGameEncoder = jencode1L((p: Presentation.EmptyGameState.type) => p.kind)("kind")
  
    lazy val finalGameEncoder = jencode5L((p: Presentation.FinalGameState) => (
        p.self, p.briscolaCard, p.playersOrderByPoints, p.winner, p.kind))(
        "self", "briscolaCard", "playersOrderByPoints", "winner", "kind")
        
    jencode1((p: Presentation.GameState) => p match {
      case Presentation.EmptyGameState => emptyGameEncoder(Presentation.EmptyGameState)
      case gm:Presentation.ActiveGameState => activeGameEncoder(gm)
      case gm:Presentation.FinalGameState => finalGameEncoder(gm)
    })    
      
  }   
  
  implicit lazy val briscolaEventEncoder = {
    implicit lazy val gameEventKindEncoder = enumEncoder[Presentation.BriscolaEventKind.type]
    
    implicit lazy val gameStartedEventEncoder = jencode2L( (e:Presentation.GameStarted) => (e.game, e.kind) )("game", "kind")
    implicit lazy val cardPlayedEncoder = jencode4L((p: Presentation.CardPlayed) => (
      p.game, p.player, p.card, p.kind))("game", "player", "card", "kind")
      
    jencode1((p: Presentation.BriscolaEvent) => p match {
      case c:Presentation.GameStarted => gameStartedEventEncoder(c)
      case c:Presentation.CardPlayed => cardPlayedEncoder(c)
    })  
  }
  
  
  /** Competition */

  implicit lazy val competitionStateKindEncoder = enumEncoder[Presentation.CompetitionStateKind.type] 
  
  implicit lazy val matchKindEncoder = {
  
    implicit lazy val matchKindKindEncoder = enumEncoder[Presentation.MatchKindKind.type] 
    
    lazy val numberOfGamesMatchKindEncoder = jencode2L((p: Presentation.NumberOfGamesMatchKind) => (p.numberOfMatches, p.kind))("numberOfMatches", "kind") 
    lazy val targetPointsMatchKindEncoder = jencode2L((p: Presentation.TargetPointsMatchKind) => (p.winnerPoints, p.kind))("winnerPoints", "kind") 
    
    jencode1((p: Presentation.MatchKind) => p match {
      case Presentation.SingleMatch => EncodeJson.StringEncodeJson("single-match")
      case t:Presentation.NumberOfGamesMatchKind => numberOfGamesMatchKindEncoder(t)
      case t:Presentation.TargetPointsMatchKind => targetPointsMatchKindEncoder(t)
    })
  }
  
  implicit lazy val competitionStartDeadlineEncoder = {
    
    implicit lazy val competitionStartDeadlineKindEncoder = enumEncoder[Presentation.CompetitionStartDeadlineKind.type]
    
    lazy val onPlayerCountEncoder = jencode2L((p: Presentation.OnPlayerCount) => (p.count, p.kind) )("count", "kind") 
    
    jencode1((p: Presentation.CompetitionStartDeadline) => p match {
      case Presentation.AllPlayers => EncodeJson.StringEncodeJson("all-players")
      case t:Presentation.OnPlayerCount => onPlayerCountEncoder(t)
    })
  }
      
  implicit lazy val competitionEncoder = EncodeJson.derive[Presentation.Competition]
  
  implicit lazy val competitionStateEncoder = EncodeJson.derive[Presentation.CompetitionState]

  implicit lazy val competitionEventEncoder = {
    
    implicit lazy val competitionEventKindEncoder = enumEncoder[Presentation.CompetitionEventKind.type] 
  
    lazy val createdCompetitionEncoder = jencode3L((p: Presentation.CreatedCompetition) => (
        p.issuer, p.competition, p.kind))(
        "issuer", "competition", "kind")
  
    lazy val competitionAcceptedEncoder = jencode3L((p: Presentation.CompetitionAccepted) => (
        p.player, p.competition, p.kind))(
        "player", "competition", "kind")
  
    lazy val competitionDeclinedEncoder = jencode4L((p: Presentation.CompetitionDeclined) => (
        p.player, p.competition, p.reason, p.kind))(
        "player", "competition", "reason", "kind")
        
     jencode1((p: Presentation.CompetitionEvent) => p match {
      case c:Presentation.CreatedCompetition => createdCompetitionEncoder(c)
      case c:Presentation.CompetitionAccepted => competitionAcceptedEncoder(c)
      case c:Presentation.CompetitionDeclined => competitionDeclinedEncoder(c)
    })    
    
  } 
  
  /** SiteMap */
  
  implicit lazy val siteMapEncoder = EncodeJson.derive[Presentation.SiteMap]
  
}