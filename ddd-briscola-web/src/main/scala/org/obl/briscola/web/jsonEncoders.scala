package org.obl.briscola
package web

import player._
import argonaut._
import Argonaut._
import org.obl.raz.Path

import org.obl.briscola.web.util.ArgonautHelper._

object jsonEncoders {
  
  class KindAdder[E <: Enumeration] {
    def apply[T <: Presentation.ADT[E]](derived:EncodeJson[T])(implicit enumEncoder:EncodeJson[E#Value]):EncodeJson[T] = {
      EncodeJson[T] { v =>
        val j = derived(v)
        j.obj match {
          case None => j 
          case Some(obj) => Json.jObjectAssocList(obj.toList ::: List("kind" -> enumEncoder(v.kind)))
        }
      }
    }
  }
  
  def singletonADTEncoder[E <: Enumeration, T <: Presentation.ADT[E]](implicit enumEncoder:EncodeJson[E#Value]) =
    EncodeJson[T] { v =>
      Json.obj("kind" -> enumEncoder(v.kind))
    }
  
  def withKind[E <: Enumeration] = new KindAdder[E]()
  
  import EncodeJson.derive
  
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
    
    implicit lazy val playerLogOnEncoder = withKind[Presentation.PlayerEventKind.type](derive[Presentation.PlayerLogOn])  
    implicit lazy val playerLogOffEncoder = withKind[Presentation.PlayerEventKind.type](derive[Presentation.PlayerLogOff])
    
    jencode1((p: Presentation.PlayerEvent) => p match {
      case c:Presentation.PlayerLogOn => playerLogOnEncoder(c)
      case c:Presentation.PlayerLogOff => playerLogOffEncoder(c)
    })  
  }
  
  /** Game */
  implicit lazy val seedEncoder = enumEncoder[Seed.type] 
      
  implicit lazy val cardEncoder = {
    implicit val byteEnc = EncodeJson.IntEncodeJson.contramap((b:Byte) => b.toInt)
    EncodeJson.derive[Presentation.Card]
  }
  
  implicit lazy val playerScore = EncodeJson.derive[Presentation.PlayerScore]
  
  implicit lazy val playerStateEncoder = EncodeJson.derive[Presentation.PlayerState]

  implicit lazy val playerFinalStateEncoder = EncodeJson.derive[Presentation.PlayerFinalState]
  
  implicit lazy val moveEncoder = EncodeJson.derive[Presentation.Move] 

  implicit lazy val gameStateKindEncoder = enumEncoder[Presentation.GameStateKind.type]
  
  implicit lazy val dropReasonEncoder = {
    
    implicit lazy val dropReasonKindEncoder = enumEncoder[Presentation.DropReasonKind.type]
    
    lazy val playerLeftDropReasonEncoder:EncodeJson[Presentation.PlayerLeft] = 
      withKind[Presentation.DropReasonKind.type]( EncodeJson.derive[Presentation.PlayerLeft] )
    
    jencode1((p: Presentation.DropReason) => p match {
      case gm:Presentation.PlayerLeft => playerLeftDropReasonEncoder(gm)
    })
  }
  
  implicit lazy val activeGameEncoder = withKind[Presentation.GameStateKind.type](derive[Presentation.ActiveGameState]) 
  
  implicit lazy val gameStateEncoder = {
    
    lazy val emptyGameEncoder = singletonADTEncoder[Presentation.GameStateKind.type, Presentation.EmptyGameState.type]
  
    lazy val finalGameEncoder = withKind[Presentation.GameStateKind.type](derive[Presentation.FinalGameState]) 
    
    lazy val droppedGameEncoder = withKind[Presentation.GameStateKind.type](derive[Presentation.DroppedGameState]) 
        
    jencode1((p: Presentation.GameState) => p match {
      case Presentation.EmptyGameState => emptyGameEncoder(Presentation.EmptyGameState)
      case gm:Presentation.ActiveGameState => activeGameEncoder(gm)
      case gm:Presentation.DroppedGameState => droppedGameEncoder(gm)
      case gm:Presentation.FinalGameState => finalGameEncoder(gm)
    })    
      
  }   
  
  implicit lazy val briscolaEventEncoder = {
    implicit lazy val gameEventKindEncoder = enumEncoder[Presentation.BriscolaEventKind.type]
    
    lazy val gameStartedEventEncoder = withKind[Presentation.BriscolaEventKind.type](derive[Presentation.GameStarted])
    lazy val cardPlayedEventEncoder = withKind[Presentation.BriscolaEventKind.type](derive[Presentation.CardPlayed]) 
    lazy val gameDroppedEventEncoder = withKind[Presentation.BriscolaEventKind.type](derive[Presentation.GameDropped]) 
      
    jencode1((p: Presentation.BriscolaEvent) => p match {
      case c:Presentation.GameStarted => gameStartedEventEncoder(c)
      case c:Presentation.CardPlayed => cardPlayedEventEncoder(c)
      case c:Presentation.GameDropped => gameDroppedEventEncoder(c)
    })  
  }
  
  
  /** Competition */

  implicit lazy val competitionStateKindEncoder = enumEncoder[Presentation.CompetitionStateKind.type] 
  
  implicit lazy val matchKindEncoder = {
  
    implicit lazy val matchKindKindEncoder = enumEncoder[Presentation.MatchKindKind.type] 
    
    lazy val singleMatch = singletonADTEncoder[Presentation.MatchKindKind.type, Presentation.SingleMatch.type]
    lazy val numberOfGamesMatchKindEncoder = withKind[Presentation.MatchKindKind.type](derive[Presentation.NumberOfGamesMatchKind])
    lazy val targetPointsMatchKindEncoder = withKind[Presentation.MatchKindKind.type](derive[Presentation.TargetPointsMatchKind]) 
    
    jencode1((p: Presentation.MatchKind) => p match {
      case t @ Presentation.SingleMatch => singleMatch(t)
      case t:Presentation.NumberOfGamesMatchKind => numberOfGamesMatchKindEncoder(t)
      case t:Presentation.TargetPointsMatchKind => targetPointsMatchKindEncoder(t)
    })
  }
  
  implicit lazy val competitionStartDeadlineEncoder = {
    
    implicit lazy val competitionStartDeadlineKindEncoder = enumEncoder[Presentation.CompetitionStartDeadlineKind.type]
    
    lazy val allPlayers = singletonADTEncoder[Presentation.CompetitionStartDeadlineKind.type, Presentation.AllPlayers.type] 
    lazy val onPlayerCountEncoder = withKind[Presentation.CompetitionStartDeadlineKind.type](derive[Presentation.OnPlayerCount]) 
    
    jencode1((p: Presentation.CompetitionStartDeadline) => p match {
      case t @ Presentation.AllPlayers => allPlayers(t)
      case t:Presentation.OnPlayerCount => onPlayerCountEncoder(t)
    })
  }
      
  implicit lazy val competitionEncoder = EncodeJson.derive[Presentation.Competition]
  
  implicit lazy val competitionStateEncoder = EncodeJson.derive[Presentation.CompetitionState]

  implicit lazy val competitionEventEncoder = {
    
    implicit lazy val competitionEventKindEncoder = enumEncoder[Presentation.CompetitionEventKind.type]
    
    lazy val createdCompetitionEncoder = withKind[Presentation.CompetitionEventKind.type](derive[Presentation.CreatedCompetition]) 
    lazy val competitionAcceptedEncoder = withKind[Presentation.CompetitionEventKind.type](derive[Presentation.CompetitionAccepted])
    lazy val competitionDeclinedEncoder = withKind[Presentation.CompetitionEventKind.type](derive[Presentation.CompetitionDeclined]) 

     jencode1((p: Presentation.CompetitionEvent) => p match {
      case c:Presentation.CreatedCompetition => createdCompetitionEncoder(c)
      case c:Presentation.CompetitionAccepted => competitionAcceptedEncoder(c)
      case c:Presentation.CompetitionDeclined => competitionDeclinedEncoder(c)
    })    
    
  } 
  
  /** SiteMap */
  
  implicit lazy val siteMapEncoder = EncodeJson.derive[Presentation.SiteMap]
  
}