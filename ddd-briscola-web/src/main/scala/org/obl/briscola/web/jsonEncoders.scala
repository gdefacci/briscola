package org.obl.briscola
package web

import player._
import argonaut._
import Argonaut._
import org.obl.raz.Path
import org.obl.briscola.competition.Tournament
import org.obl.briscola.competition.TargetTournament
import org.obl.briscola.competition.MatchKind
import org.obl.briscola.competition.SingleMatch
import org.obl.briscola.competition.CompetitionStartDeadline

object jsonEncoders {
  
  implicit lazy val pathEncoder = jencode1((p: Path) => p.render)
  
  implicit def eventAndState[E,S](implicit ee:EncodeJson[E], se:EncodeJson[S]) = 
    jencode2L((es: Presentation.EventAndState[E,S]) => (es.event, es.state))("event", "state")
  
  
  /** Player */
  implicit lazy val playerEncoder = EncodeJson.derive[Presentation.Player] 

  /** Game */
  implicit lazy val seedEncoder = jencode1((p: Seed.Value) => p match {
    case Seed.bastoni => "bastoni"
    case Seed.coppe => "coppe"
    case Seed.denari => "denari"
    case Seed.spade => "spade"
  })
      
  implicit lazy val cardEncoder = {
    implicit val byteEnc = EncodeJson.IntEncodeJson.contramap((b:Byte) => b.toInt)
    EncodeJson.derive[Card]
  }
  
  implicit lazy val playerStateEncoder = EncodeJson.derive[Presentation.PlayerState]

  implicit lazy val playerFinalStateEncoder = EncodeJson.derive[Presentation.PlayerFinalState]
  
  implicit lazy val moveEncoder = EncodeJson.derive[Presentation.Move] 

  implicit lazy val gameStateKindEncoder = jencode1((p: Presentation.GameStateKind.Value) => p match {
      case Presentation.GameStateKind.active => "active"
      case Presentation.GameStateKind.finished => "finished"
    })
  
  implicit lazy val activeGameEncoder = EncodeJson.derive[Presentation.ActiveGameState]
  
  implicit lazy val gameStateEncoder = {    

    lazy val emptyGameEncoder = jencode1L((p: Presentation.EmptyGameState.type) => (
        p.gameStateKind))(
        "gameStateKind")
  
    lazy val finalGameEncoder = jencode5L((p: Presentation.FinalGameState) => (
        p.self, p.gameSeed, p.playersOrderByPoints, p.winner, p.gameStateKind))(
        "self", "gameSeed", "playersOrderByPoints", "winner", "gameStateKind")
        
    jencode1((p: Presentation.GameState) => p match {
      case Presentation.EmptyGameState => emptyGameEncoder(Presentation.EmptyGameState)
      case gm:Presentation.ActiveGameState => activeGameEncoder(gm)
      case gm:Presentation.FinalGameState => finalGameEncoder(gm)
    })    
      
  }   
  
  implicit lazy val briscolaEventEncoder = {
    implicit lazy val gameEventKindEncoder = jencode1((p: Presentation.BriscolaEventKind.Value) => p match {
      case Presentation.BriscolaEventKind.gameStarted => "game-started"
      case Presentation.BriscolaEventKind.cardPlayed => "card-played"
    })
    
    implicit lazy val gameStartedEventEncoder = jencode2L( (e:Presentation.GameStarted) => (e.game, e.kind) )("game", "kind")
    implicit lazy val cardPlayedEncoder = jencode4L((p: Presentation.CardPlayed) => (
      p.game, p.player, p.card, p.kind))("game", "player", "card", "kind")
      
    jencode1((p: Presentation.BriscolaEvent) => p match {
      case c:Presentation.GameStarted => gameStartedEventEncoder(c)
      case c:Presentation.CardPlayed => cardPlayedEncoder(c)
    })  
  }
  
  
  /** Competition */

  implicit lazy val competitionStateKindEncoder = jencode1((p: Presentation.CompetitionStateKind.Value) => p match {
    case Presentation.CompetitionStateKind.open => "open"
    case Presentation.CompetitionStateKind.dropped => "dropped"
    case Presentation.CompetitionStateKind.fullfilled => "fullfilled"
  })
  
  implicit lazy val matchKindEncoder = {
    
    lazy val tournamentEncoder = jencode1L((p: Tournament) => p.numberOfMatches)("numberOfMatches") 
    lazy val targetTournamentEncoder = jencode1L((p: TargetTournament) => p.winnerPoints)("winnerPoints") 
    
    jencode1((p: MatchKind) => p match {
      case SingleMatch => EncodeJson.StringEncodeJson("single-match")
      case t:Tournament => tournamentEncoder(t)
      case t:TargetTournament => targetTournamentEncoder(t)
    })
  }
  
  implicit lazy val competitionStartDeadlineEncoder = {
    
    lazy val onPlayerCountEncoder = jencode1L((p: CompetitionStartDeadline.OnPlayerCount) => p.count)("count") 
    
    jencode1((p: CompetitionStartDeadline) => p match {
      case CompetitionStartDeadline.AllPlayers => EncodeJson.StringEncodeJson("all-players")
      case t:CompetitionStartDeadline.OnPlayerCount => onPlayerCountEncoder(t)
    })
  }
      
  implicit lazy val competitionEncoder = EncodeJson.derive[Presentation.Competition]
  
  implicit lazy val competitionStateEncoder = EncodeJson.derive[Presentation.CompetitionState]

  implicit lazy val competitionEventEncoder = {
    
    implicit lazy val competitionEventKindEncoder = jencode1((p: Presentation.CompetitionEventKind.Value) => p match {
      case Presentation.CompetitionEventKind.createdCompetition => "created-competition"
      case Presentation.CompetitionEventKind.confirmedCompetition => "confirmed-competition"
      case Presentation.CompetitionEventKind.playerAccepted => "player-accepted"
      case Presentation.CompetitionEventKind.playerDeclined => "player-declined"
    })
  
    lazy val createdCompetitionEncoder = jencode3L((p: Presentation.CreatedCompetition) => (
        p.issuer, p.competition, p.kind))(
        "issuer", "competition", "kind")
  
    lazy val confirmedCompetitionEncoder = jencode2L((p: Presentation.ConfirmedCompetition) => (
        p.competition, p.kind))(
        "competition", "kind")
        
    lazy val competitionAcceptedEncoder = jencode3L((p: Presentation.CompetitionAccepted) => (
        p.player, p.competition, p.kind))(
        "player", "competition", "kind")
  
    lazy val competitionDeclinedEncoder = jencode4L((p: Presentation.CompetitionDeclined) => (
        p.player, p.competition, p.reason, p.kind))(
        "player", "competition", "reason", "kind")
        
     jencode1((p: Presentation.CompetitionEvent) => p match {
      case c:Presentation.CreatedCompetition => createdCompetitionEncoder(c)
      case c:Presentation.ConfirmedCompetition => confirmedCompetitionEncoder(c)
      case c:Presentation.CompetitionAccepted => competitionAcceptedEncoder(c)
      case c:Presentation.CompetitionDeclined => competitionDeclinedEncoder(c)
    })    
    
  }    
}