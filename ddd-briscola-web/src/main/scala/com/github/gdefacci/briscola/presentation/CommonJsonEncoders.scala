package com.github.gdefacci.briscola.presentation

import argonaut._
import Argonaut._
import org.obl.raz.Path

import com.github.gdefacci.briscola.web.util.ArgonautHelper._
import com.github.gdefacci.briscola.game.Seed

object CommonJsonEncoders {
  
  class KindAdder[E <: Enumeration] {
    def apply[T <: ADT[E]](derived:EncodeJson[T])(implicit enumEncoder:EncodeJson[E#Value]):EncodeJson[T] = {
      EncodeJson[T] { v =>
        val j = derived(v)
        j.obj match {
          case None => j 
          case Some(obj) => Json.jObjectAssocList(obj.toList ::: List("kind" -> enumEncoder(v.kind)))
        }
      }
    }
  }
  
  def singletonADTEncoder[E <: Enumeration, T <: ADT[E]](implicit enumEncoder:EncodeJson[E#Value]) =
    EncodeJson[T] { v =>
      Json.obj("kind" -> enumEncoder(v.kind))
    }
  
  def withKind[E <: Enumeration] = new KindAdder[E]()
  
  import EncodeJson.derive
  
  implicit lazy val pathEncoder = jencode1((p: Path) => p.render)
  
  implicit def seqEncodeJson[T](implicit enc:EncodeJson[T]):EncodeJson[Seq[T]] = EncodeJson[Seq[T]] { v =>
    v.toList.asJson(ListEncodeJson(enc))
  }
  
  implicit def iterableEncodeJson[T](implicit enc:EncodeJson[T]):EncodeJson[Iterable[T]] = EncodeJson[Iterable[T]] { v =>
    v.toList.asJson(ListEncodeJson(enc))
  }

  implicit def collectionEncoder[T](implicit enc:EncodeJson[T]):EncodeJson[Collection[T]] =
    jencode1L((es: Collection[T]) => es.members)("members")
  
  implicit def eventAndState[E,S](implicit ee:EncodeJson[E], se:EncodeJson[S]) = 
    jencode2L((es: EventAndState[E,S]) => (es.event, es.state))("event", "state")
  
  /** Player */
//  implicit lazy val playerEncoder = 
//    jencode2L((pl: presentation.Player) => (pl.self, pl.name))("self", "name")
//  
//  lazy val privatePlayerEncoder = EncodeJson.derive[presentation.Player] 
//
//  implicit lazy val playerEventEncoder = {
//    
//    implicit lazy val playerEventKindEncoder = enumEncoder[presentation.PlayerEventKind.type]
//    
//    implicit lazy val playerLogOnEncoder = withKind[presentation.PlayerEventKind.type](derive[presentation.PlayerLogOn])  
//    implicit lazy val playerLogOffEncoder = withKind[presentation.PlayerEventKind.type](derive[presentation.PlayerLogOff])
//    
//    jencode1((p: presentation.PlayerEvent) => p match {
//      case c:presentation.PlayerLogOn => playerLogOnEncoder(c)
//      case c:presentation.PlayerLogOff => playerLogOffEncoder(c)
//    })  
//  }
  
  /** Game */
//  implicit lazy val seedEncoder = enumEncoder[Seed.type] 
//  implicit lazy val seedEncoder = EncodeJson[Seed] { v => Json.jString(v.toString) }
//      
//  implicit lazy val cardEncoder = {
//    implicit val byteEnc = EncodeJson.IntEncodeJson.contramap((b:Byte) => b.toInt)
//    EncodeJson.derive[presentation.Card]
//  }
//  
//  implicit lazy val playerScore = EncodeJson.derive[presentation.Score]
//  
//  implicit lazy val playerStateEncoder = EncodeJson.derive[presentation.PlayerState]
//
//  implicit lazy val playerFinalStateEncoder = EncodeJson.derive[presentation.PlayerFinalState]
//  
//  implicit lazy val moveEncoder = EncodeJson.derive[presentation.Move] 
//
//  implicit lazy val gameStateKindEncoder = enumEncoder[presentation.GameStateKind.type]
//  
//  implicit lazy val dropReasonEncoder = {
//    
//    implicit lazy val dropReasonKindEncoder = enumEncoder[presentation.DropReasonKind.type]
//    
//    lazy val playerLeftDropReasonEncoder:EncodeJson[presentation.PlayerLeft] = 
//      withKind[presentation.DropReasonKind.type]( EncodeJson.derive[presentation.PlayerLeft] )
//    
//    jencode1((p: presentation.DropReason) => p match {
//      case gm:presentation.PlayerLeft => playerLeftDropReasonEncoder(gm)
//    })
//  }
//  
//  implicit lazy val activeGameEncoder = withKind[presentation.GameStateKind.type](derive[presentation.ActiveGameState]) 
//  
//  implicit lazy val teamScore = derive[presentation.TeamScore]
//  
//  implicit lazy val gameResultEncoder:EncodeJson[presentation.GameResult] = {
//    jencode1((p: presentation.GameResult) => p match {
//      case tgmr:presentation.TeamsGameResult => derive[presentation.TeamsGameResult](tgmr)
//      case pgmr:presentation.PlayersGameResult => derive[presentation.PlayersGameResult](pgmr)
//    })    
//  }
//  
//  implicit lazy val gameStateEncoder = {
//    
//    lazy val emptyGameEncoder = singletonADTEncoder[presentation.GameStateKind.type, presentation.EmptyGameState.type]
//  
//    lazy val finalGameEncoder = withKind[presentation.GameStateKind.type](derive[presentation.FinalGameState]) 
//    
//    lazy val droppedGameEncoder = withKind[presentation.GameStateKind.type](derive[presentation.DroppedGameState]) 
//        
//    jencode1((p: presentation.GameState) => p match {
//      case presentation.EmptyGameState => emptyGameEncoder(presentation.EmptyGameState)
//      case gm:presentation.ActiveGameState => activeGameEncoder(gm)
//      case gm:presentation.DroppedGameState => droppedGameEncoder(gm)
//      case gm:presentation.FinalGameState => finalGameEncoder(gm)
//    })    
//      
//  }   
//  
//  implicit lazy val briscolaEventEncoder = {
//    implicit lazy val gameEventKindEncoder = enumEncoder[presentation.BriscolaEventKind.type]
//    
//    lazy val gameStartedEventEncoder = withKind[presentation.BriscolaEventKind.type](derive[presentation.GameStarted])
//    lazy val cardPlayedEventEncoder = withKind[presentation.BriscolaEventKind.type](derive[presentation.CardPlayed]) 
//    lazy val gameDroppedEventEncoder = withKind[presentation.BriscolaEventKind.type](derive[presentation.GameDropped]) 
//      
//    jencode1((p: presentation.BriscolaEvent) => p match {
//      case c:presentation.GameStarted => gameStartedEventEncoder(c)
//      case c:presentation.CardPlayed => cardPlayedEventEncoder(c)
//      case c:presentation.GameDropped => gameDroppedEventEncoder(c)
//    })  
//  }
  
  
  
  /** SiteMap */
  
  implicit lazy val siteMapEncoder = EncodeJson.derive[sitemap.SiteMap]
  
}